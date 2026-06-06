package com.boosthabits.ui.recompensas

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boosthabits.R
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.databinding.FragmentRewardsBinding
import com.boosthabits.ui.perfil.CosmeticoManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Paint
import android.graphics.Typeface
import coil.load
import coil.transform.CircleCropTransformation

// fragmento principal de la pantalla de recompensas con pestañas
class RecompensasFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecompensasViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RewardsPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.rewards_tab_gems)
                1 -> getString(R.string.rewards_tab_coins)
                else -> null
            }
        }.attach()

        configurarVistaPreviaUsuario()
        observarEstado()
        observarVistaPrevia()

        // botón para cerrar la vista previa y volver a la lista
        binding.btnClosePreview.setOnClickListener {
            viewModel.setPreviewReward(null)
        }
    }

    private fun observarEstado() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            binding.tvBalanceMonedas.text = (stats?.monedas ?: 0).toString()
            binding.tvBalanceGemas.text = (stats?.gemas ?: 0).toString()
        }
    }

    private fun configurarVistaPreviaUsuario() {
        val user = auth.currentUser
        if (user != null) {
            binding.previewName.text = user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
            user.photoUrl?.let {
                binding.previewAvatar.imageView.load(it) {
                    crossfade(true)
                    placeholder(R.mipmap.ic_launcher_round)
                    transformations(CircleCropTransformation())
                }
            } ?: run {
                binding.previewAvatar.imageView.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }

    private fun observarVistaPrevia() {
        // observar vista previa
        viewModel.previewReward.observe(viewLifecycleOwner) { recompensa ->
            if (recompensa != null) {
                binding.cardPreview.visibility = View.VISIBLE
                
                // Primero se aplica el estado ACTUAL y luego se sobreesribe con la vista previa del ítem seleccionado
                restablecerAEstadoActual()


                when {
                    recompensa.id.startsWith("style_") -> {
                        when (recompensa.id) {
                            "style_bold" -> binding.previewName.typeface = Typeface.DEFAULT_BOLD
                            "style_italic" -> binding.previewName.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                            "style_bold_italic" -> binding.previewName.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                            "style_monospace" -> binding.previewName.typeface = Typeface.MONOSPACE
                            "style_underline" -> binding.previewName.paintFlags = binding.previewName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        }
                    }
                    recompensa.id.startsWith("color_") -> {
                        when (recompensa.id) {
                            "color_red" -> binding.previewName.setTextColor(Color.parseColor("#E53935"))
                            "color_blue" -> binding.previewName.setTextColor(Color.parseColor("#1E88E5"))
                            "color_green" -> binding.previewName.setTextColor(Color.parseColor("#43A047"))
                            "color_purple" -> binding.previewName.setTextColor(Color.parseColor("#8E24AA"))
                        }
                    }
                    recompensa.titulo.contains("Nombre", ignoreCase = true) -> {
                        when (recompensa.id) {
                            "3" -> CosmeticoManager.applyLegendaryNameEffect(binding.previewName, intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFD700")), Color.parseColor("#FFEE58"))
                            "4" -> CosmeticoManager.applyLegendaryNameEffect(binding.previewName, intArrayOf(Color.parseColor("#FFC107"), Color.parseColor("#FFD700"), Color.parseColor("#FFC107")), Color.parseColor("#FFD700"))
                            "5" -> CosmeticoManager.applyLegendaryNameEffect(binding.previewName, intArrayOf(Color.parseColor("#FF8F00"), Color.parseColor("#FFB300"), Color.parseColor("#FF8F00")), Color.parseColor("#FFA000"))
                            else -> CosmeticoManager.applyLegendaryNameEffect(binding.previewName)
                        }
                    }
                    recompensa.titulo.contains("Fondo", ignoreCase = true) -> {
                        val resId = resources.getIdentifier(recompensa.urlImagen, "drawable", requireContext().packageName)
                        if (resId != 0) binding.cardPreview.findViewById<View>(R.id.preview_container).setBackgroundResource(resId)
                    }
                    recompensa.titulo.contains("Perfil", ignoreCase = true) -> {
                        val resId = resources.getIdentifier(recompensa.urlImagen, "mipmap", requireContext().packageName)
                        if (resId != 0) binding.previewAvatar.imageView.setImageResource(resId)
                    }
                }
            } else {
                binding.cardPreview.visibility = View.GONE
            }
        }

        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            if (viewModel.previewReward.value == null) {
                restablecerAEstadoActual()
            }
        }
    }

    private fun restablecerAEstadoActual() {
        val stats = viewModel.userStats.value
        CosmeticoManager.applyCosmetics(
            context = requireContext(),
            stats = stats,
            nameTextView = binding.previewName,
            avatarView = binding.previewAvatar,
            backgroundView = binding.cardPreview.findViewById(R.id.preview_container)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class RewardsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RecompensaListFragment.newInstance(CurrencyType.GEMA)
                else -> RecompensaListFragment.newInstance(CurrencyType.MONEDA)
            }
        }
    }
}
