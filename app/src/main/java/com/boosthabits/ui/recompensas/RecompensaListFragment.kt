package com.boosthabits.ui.recompensas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.databinding.FragmentRewardListBinding

// fragmento que muestra la lista de recompensas (gemas o monedas)
class RecompensaListFragment : Fragment() {

    private var _binding: FragmentRewardListBinding? = null
    // binding para acceder a las vistas del layout
    private val binding get() = _binding!!
    // viewmodel compartido a nivel de actividad para asegurar consistencia
    private val viewModel: RecompensasViewModel by activityViewModels()

    private var tipoMoneda: CurrencyType = CurrencyType.GEMA

    companion object {
        private const val ARG_CURRENCY_TYPE = "currency_type"

        fun newInstance(type: CurrencyType) = RecompensaListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_CURRENCY_TYPE, type)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tipoMoneda = arguments?.getSerializable(ARG_CURRENCY_TYPE) as? CurrencyType ?: CurrencyType.GEMA
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilters()

        // observamos las estadisticas para saber el saldo actual
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            val saldo = if (tipoMoneda == CurrencyType.GEMA) stats?.gemas ?: 0 else stats?.monedas ?: 0
            
            // si son gemas mostramos el marketplace, si no, los cosmeticos normales
            if (tipoMoneda == CurrencyType.GEMA) {
                setupMarketplaceUI(saldo)
            } else {
                setupStandardRewardsUI(saldo)
            }
        }

        observeRedeemResults()
    }

    private fun setupFilters() {
        if (tipoMoneda == CurrencyType.MONEDA) {
            binding.filterContainer.visibility = View.VISIBLE
            
            // Localizar chips
            binding.chipAll.text = getString(com.boosthabits.R.string.filter_all)
            binding.chipPfp.text = getString(com.boosthabits.R.string.personalization_type_pfp)
            binding.chipNames.text = getString(com.boosthabits.R.string.personalizacion_type_name)
            binding.chipWallpapers.text = getString(com.boosthabits.R.string.personalization_type_wallpaper)

            binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
                val filter = when (checkedIds.firstOrNull()) {
                    binding.chipPfp.id -> "pfp"
                    binding.chipNames.id -> "names"
                    binding.chipWallpapers.id -> "wallpapers"
                    else -> "all"
                }
                viewModel.setCoinFilter(filter)
            }
        } else {
            binding.filterContainer.visibility = View.GONE
        }
    }

    private fun setupMarketplaceUI(balance: Int) {
        val adapter = MarketplaceAdapter(
            onItemClick = { oferta ->
                showOfferDetail(oferta, balance)
            }
        )
        binding.rvRewards.adapter = adapter
        viewModel.getRewards(CurrencyType.GEMA).observe(viewLifecycleOwner) { rewards ->
            adapter.submitList(rewards)
        }
    }

    private fun setupStandardRewardsUI(balance: Int) {
        val adapter = RecompensasAdapter(
            userBalance = balance,
            onRedeemClick = { reward ->
                viewModel.redeemReward(reward)
            },
            onItemClick = { reward ->
                viewModel.setPreviewReward(reward)
            }
        )
        binding.rvRewards.adapter = adapter
        
        viewModel.getRewards(CurrencyType.MONEDA).observe(viewLifecycleOwner) { rewards ->
            viewModel.coinFilter.observe(viewLifecycleOwner) { filter ->
                val filteredList = when (filter) {
                    "pfp" -> rewards.filter { it.titulo.contains("Perfil", ignoreCase = true) || it.id.toIntOrNull() in 9..14 }
                    "names" -> rewards.filter { it.titulo.contains("Nombre", ignoreCase = true) || it.id.toIntOrNull() in 3..5 || it.id.startsWith("style_") || it.id.startsWith("color_") }
                    "wallpapers" -> rewards.filter { it.titulo.contains("Fondo", ignoreCase = true) || it.id.toIntOrNull() in 7..8 }
                    else -> rewards
                }
                adapter.submitList(filteredList)
            }
        }
    }

    private var currentBottomSheet: OfertaDetalleHoja? = null

    private fun showOfferDetail(oferta: RecompensaEntity, gemasUsuario: Int) {
        // mostramos el bottom sheet con los detalles de la oferta usando newInstance
        currentBottomSheet = OfertaDetalleHoja.newInstance(oferta, gemasUsuario)
        currentBottomSheet?.show(childFragmentManager, OfertaDetalleHoja.TAG)
    }

    private fun observeRedeemResults() {
        viewModel.redeemResult.observe(viewLifecycleOwner) { result ->
            binding.pbLoading.visibility = View.GONE
            result?.onSuccess { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                viewModel.clearRedeemResult()
            }?.onFailure { excepcion ->
                Toast.makeText(requireContext(), excepcion.message, Toast.LENGTH_SHORT).show()
                viewModel.clearRedeemResult()
            }
        }

        viewModel.marketplaceRedeemResult.observe(viewLifecycleOwner) { result ->
            binding.pbLoading.visibility = View.GONE
            result?.onSuccess { codigo ->
                currentBottomSheet?.showSuccess(codigo)
                viewModel.clearRedeemResult()
            }?.onFailure { excepcion ->
                Toast.makeText(requireContext(), excepcion.message, Toast.LENGTH_SHORT).show()
                viewModel.clearRedeemResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
