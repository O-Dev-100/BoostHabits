package com.boosthabits.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.databinding.FragmentHabitDetailBinding
import com.google.firebase.auth.FirebaseAuth

class HabitoDetalleFragment : Fragment() {

    private var _binding: FragmentHabitDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitosViewModel by viewModels()
    private val args: HabitDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHabitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.habitosActivos.observe(viewLifecycleOwner) { lista ->
            val habito = lista.find { it.id == args.habitId }
            habito?.let { cargarDatos(it) }
        }

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.inputNombre.text.toString().trim()
            val objetivo = binding.inputObjetivo.text.toString().toIntOrNull() ?: 1
            val dificultad = binding.sliderDificultad.value.toInt()

            val habitoActual = viewModel.habitosActivos.value?.find { it.id == args.habitId }
            
            val habitoActualizado = habitoActual?.copy(
                nombre = nombre,
                dificultad = dificultad,
                icono = binding.tvIconoGrande.text.toString(),
                objetivoDiario = objetivo
            ) ?: HabitoEntity(
                id = args.habitId,
                idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                nombre = nombre,
                dificultad = dificultad,
                icono = binding.tvIconoGrande.text.toString(),
                objetivoDiario = objetivo
            )
            
            viewModel.actualizarHabito(habitoActualizado)
            findNavController().popBackStack()
        }

        binding.btnEliminar.setOnClickListener {
            viewModel.eliminarHabito(args.habitId)
            findNavController().popBackStack()
        }
    }

    private fun cargarDatos(habito: HabitoEntity) {
        binding.tvIconoGrande.text = habito.icono
        binding.inputNombre.setText(habito.nombre)
        binding.inputObjetivo.setText(habito.objetivoDiario.toString())
        binding.sliderDificultad.value = habito.dificultad.toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
