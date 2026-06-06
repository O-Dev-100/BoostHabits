package com.boosthabits.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.databinding.FragmentCreateHabitBinding
import com.google.firebase.auth.FirebaseAuth

class CreateHabitoFragment : Fragment() {

    private var _binding: FragmentCreateHabitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitosViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.inputNombre.text.toString().trim()
            val objetivo = binding.inputObjetivo.text.toString().toIntOrNull() ?: 1
            val icono = binding.tvHabitIconDisplay.text.toString().trim()
            val dificultad = binding.sliderDificultad.value.toInt()

            if (nombre.isNotEmpty()) {
                // Cálculo de recompensa basado en dificultad para hábitos manuales
                val puntosBase = when (dificultad) {
                    1 -> 50
                    2 -> 100
                    3 -> 200
                    else -> 50
                }

                val habito = HabitoEntity(
                    idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    nombre = nombre,
                    dificultad = dificultad,
                    icono = icono.ifEmpty { "⭐" },
                    objetivoDiario = objetivo,
                    valorRecompensa = puntosBase
                )
                viewModel.crearHabito(habito)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
