package com.boosthabits.ui.recompensas

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import coil.load
import com.boosthabits.R
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.databinding.LayoutOfferDetailBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// panel inferior que muestra el detalle de una oferta y permite canjearla
class OfertaDetalleHoja : BottomSheetDialogFragment() {

    private var _binding: LayoutOfferDetailBottomSheetBinding? = null
    private val binding get() = _binding!!
    
    // usamos el viewmodel compartido a nivel de actividad para asegurar consistencia
    private val viewModel: RecompensasViewModel by activityViewModels()
    private var oferta: RecompensaEntity? = null
    private var gemasUsuario: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // recuperamos la oferta y el saldo de los argumentos de forma segura
        oferta = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_OFFER, RecompensaEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_OFFER) as? RecompensaEntity
        }
        gemasUsuario = arguments?.getInt(ARG_GEMS) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutOfferDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ofertaActual = oferta ?: return

        // Limpiamos resultados anteriores al abrir el detalle
        viewModel.clearRedeemResult()

        // Cargar imagen: detectamos si es un recurso local o una URL
        if (ofertaActual.urlImagen.startsWith("http")) {
            binding.ivDetailLogo.load(ofertaActual.urlImagen)
        } else {
            val resId = resources.getIdentifier(ofertaActual.urlImagen, "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivDetailLogo.setImageResource(resId)
            } else {
                binding.ivDetailLogo.load(ofertaActual.urlImagen) // Fallback por si acaso
            }
        }

        binding.tvDetailTitle.text = if (ofertaActual.resTitulo != 0) getString(ofertaActual.resTitulo) else ofertaActual.titulo
        binding.tvDetailDescription.text = if (ofertaActual.resDescripcion != 0) getString(ofertaActual.resDescripcion) else ofertaActual.descripcion
        binding.tvDetailTerms.text = ofertaActual.terminosYCondiciones ?: getString(R.string.rewards_terms_default)

        updateActionButton()

        binding.btnAction.setOnClickListener {
            // llamamos al canje en el viewmodel
            viewModel.redeemMarketplaceOffer(ofertaActual)
        }
        
        // observamos el resultado del canje especifico para el marketplace
        viewModel.marketplaceRedeemResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess { code ->
                showSuccess(code)
            }
        }
    }

    private fun updateActionButton() {
        val ofertaActual = oferta ?: return
        // actualizamos el boton segun si el usuario tiene gemas suficientes
        if (gemasUsuario < ofertaActual.coste) {
            binding.btnAction.isEnabled = false
            binding.btnAction.text = getString(R.string.rewards_btn_needed, ofertaActual.coste - gemasUsuario)
        } else {
            binding.btnAction.isEnabled = true
            binding.btnAction.text = getString(R.string.rewards_btn_redeem, ofertaActual.coste)
        }
    }

    fun showSuccess(voucherCode: String?) {
        // ocultamos el boton y mostramos la animacion de confeti
        binding.btnAction.isVisible = false
        binding.lottieConfetti.isVisible = true
        
        // Cargamos la animación de forma programática y segura
        try {
            binding.lottieConfetti.setAnimation(R.raw.confetti)
            binding.lottieConfetti.playAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla la animación, al menos el usuario ve el éxito
        }

        // revelamos el codigo y el boton de ir a la tienda
        binding.layoutSuccessAction.isVisible = true
        if (voucherCode != null) {
            binding.tvVoucherCode.text = voucherCode
            binding.tvVoucherCode.isVisible = true
            
            // Botón de copiar
            binding.btnCopyCode.setOnClickListener {
                copyToClipboard(voucherCode)
            }

            // Botón de compartir
            binding.btnShareCode.setOnClickListener {
                shareCode(voucherCode)
            }
            
            // También mantenemos el click en el texto por si acaso
            binding.tvVoucherCode.setOnClickListener {
                copyToClipboard(voucherCode)
            }
        } else {
            binding.tvVoucherCode.isVisible = false
        }

        binding.btnGoToStore.setOnClickListener {
            // abrimos la url externa en un custom tab
            oferta?.urlExterna?.let { url ->
                val intent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                intent.launchUrl(requireContext(), android.net.Uri.parse(url))
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Voucher Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), getString(R.string.rewards_copy_success), Toast.LENGTH_SHORT).show()
    }

    private fun shareCode(code: String) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            val message = getString(R.string.rewards_share_message, code)
            putExtra(android.content.Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.rewards_share_title)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "OfferDetailBottomSheet"
        private const val ARG_OFFER = "arg_offer"
        private const val ARG_GEMS = "arg_gems"

        fun newInstance(offer: RecompensaEntity, userGems: Int) = OfertaDetalleHoja().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_OFFER, offer)
                putInt(ARG_GEMS, userGems)
            }
        }
    }
}
