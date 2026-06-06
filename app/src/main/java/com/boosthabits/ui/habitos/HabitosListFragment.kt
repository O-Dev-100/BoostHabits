package com.boosthabits.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boosthabits.R
import com.boosthabits.databinding.FragmentHabitsListBinding

class HabitosListFragment : Fragment() {

    private var _binding: FragmentHabitsListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitosViewModel by viewModels()
    private lateinit var adapter: HabitosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHabitsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HabitosAdapter()
        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        adapter.setOnItemClickListener { habito ->
            val action = HabitsListFragmentDirections.actionHabitsListFragmentToHabitDetailFragment(habito.id)
            findNavController().navigate(action)
        }

        adapter.setOnFocusClickListener { habito ->
            val bundle = Bundle().apply {
                putLong("habitId", habito.id)
                putString("habitName", habito.nombre)
                putInt("durationMinutes", if (habito.duracionMinutos > 0) habito.duracionMinutos else 15)
                putBoolean("isSaludMental", habito.categoria == com.boosthabits.data.local.entity.HabitCategory.SALUD_MENTAL)
            }
            findNavController().navigate(R.id.focusFragment, bundle)
        }

        viewModel.habitosActivos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        binding.fabNuevoHabito.setOnClickListener {
            findNavController().navigate(R.id.action_habitsListFragment_to_createHabitFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
