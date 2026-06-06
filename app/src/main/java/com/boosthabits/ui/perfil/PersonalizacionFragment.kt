package com.boosthabits.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boosthabits.databinding.FragmentPersonalizationBinding

class PersonalizacionFragment : Fragment() {

    private var _binding: FragmentPersonalizationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var adapter: CosmeticoAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = CosmeticoAdapter(
            estadisticasUsuario = viewModel.userStats.value,
            alAlternar = { recompensa ->
                viewModel.alternarCosmetico(recompensa)
            }
        )
        binding.rvPersonalizations.adapter = adapter
    }

    private fun observeData() {
        viewModel.userStats.observe(viewLifecycleOwner) { estadisticas ->
            adapter?.actualizarEstadisticas(estadisticas)
        }

        viewModel.unlockedRewards.observe(viewLifecycleOwner) { recompensas ->
            if (recompensas.isEmpty()) {
                binding.tvEmptyPersonalization.visibility = View.VISIBLE
                binding.rvPersonalizations.visibility = View.GONE
            } else {
                binding.tvEmptyPersonalization.visibility = View.GONE
                binding.rvPersonalizations.visibility = View.VISIBLE
                adapter?.submitList(recompensas)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
