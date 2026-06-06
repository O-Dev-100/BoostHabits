package com.boosthabits.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.boosthabits.R
import com.boosthabits.data.HabitoPresetsData
import com.boosthabits.databinding.FragmentCategorySelectorBinding

class CategoriaSelectorFragment : Fragment() {

    private var _binding: FragmentCategorySelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategorySelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adaptador = CategoriaAdapter { categoria ->
            val bundle = Bundle().apply {
                putInt("categoryId", categoria.id)
                putString("categoryTitle", requireContext().getString(categoria.titulo))
            }
            // Navegamos a la lista de presets
            findNavController().navigate(R.id.action_categorySelectorFragment_to_presetListFragment, bundle)
        }

        val administradorDiseno = GridLayoutManager(requireContext(), 2)
        administradorDiseno.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(posicion: Int): Int {
                // El ID 1 (Actividad Física) ocupa 2 columnas, el resto 1
                return if (adaptador.getCategoria(posicion).id == 1) 2 else 1
            }
        }

        binding.rvCategories.layoutManager = administradorDiseno
        binding.rvCategories.adapter = adaptador
        adaptador.submitList(HabitoPresetsData.categorias)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
