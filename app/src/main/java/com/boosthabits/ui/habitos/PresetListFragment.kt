package com.boosthabits.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.boosthabits.R
import com.boosthabits.data.HabitoPreset
import com.boosthabits.data.HabitoPresetsData
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.RecompensaTipo
import com.boosthabits.databinding.DialogCustomizeHabitBinding
import com.boosthabits.databinding.FragmentPresetListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class PresetListFragment : Fragment() {

    private var _binding: FragmentPresetListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPresetListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = arguments?.getInt("categoryId") ?: -1
        val categoryTitle = arguments?.getString("categoryTitle") ?: "Hábitos"

        binding.tvCategoryName.text = categoryTitle
        binding.toolbarPresets.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnGoogleFit.visibility = View.GONE

        val adapter = PresetAdapter { preset ->
            mostrarDialogoPersonalizacion(preset)
        }

        binding.rvPresets.adapter = adapter
        
        val filteredPresets = HabitoPresetsData.presets.filter { it.categoriaId == categoryId }
        adapter.submitList(filteredPresets)

        viewModel.eventoError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.limpiarError()
            }
        }
    }

    private fun mostrarDialogoPersonalizacion(preset: HabitoPreset) {
        val dialogBinding = DialogCustomizeHabitBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val context = requireContext()
        val habitName = context.getString(preset.nombre)

        dialogBinding.tvHabitIconLarge.text = preset.icon
        dialogBinding.tvHabitNameTitle.text = habitName
        
        val esPasos = habitName.contains("pasos", ignoreCase = true) || habitName.contains("steps", ignoreCase = true) || habitName.contains("Caminar", ignoreCase = true) || habitName.contains("Walk", ignoreCase = true)
        
        dialogBinding.chipOption1.visibility = View.VISIBLE
        dialogBinding.chipOption2.visibility = View.VISIBLE
        dialogBinding.chipOption3.visibility = View.VISIBLE
        dialogBinding.chipOption4.visibility = View.GONE
        
        if (esPasos) {
            dialogBinding.chipOption1.text = "5,000"
            dialogBinding.chipOption2.text = "10,000"
            dialogBinding.chipOption3.text = "15,000"
            dialogBinding.tvHabitDescription.text = getString(R.string.preset_select_steps)
        } else if (preset.timeOptions != null) {
            val options = preset.timeOptions
            dialogBinding.chipOption1.text = options.getOrNull(0) ?: ""
            
            if (options.size > 1) {
                dialogBinding.chipOption2.text = options.getOrNull(1) ?: ""
            } else {
                dialogBinding.chipOption2.visibility = View.GONE
            }
            
            if (options.size > 2) {
                dialogBinding.chipOption3.text = options.getOrNull(2) ?: ""
            } else {
                dialogBinding.chipOption3.visibility = View.GONE
            }

            if (options.size > 3) {
                dialogBinding.chipOption4.text = options.getOrNull(3) ?: ""
                dialogBinding.chipOption4.visibility = View.VISIBLE
            }
        } else {
            dialogBinding.chipOption1.text = "5 min"
            dialogBinding.chipOption2.text = "15 min"
            dialogBinding.chipOption3.text = "30 min"
        }

        dialogBinding.ivRewardIconDialog.setImageResource(
            if (preset.recompensaTipo == RecompensaTipo.GEMAS) R.drawable.ic_gemas else R.drawable.ic_monedas
        )

        fun calcularPuntosActuales(): Int {
            val selectedChipId = dialogBinding.cgGoalOptions.checkedChipId
            val multiplicador = when (selectedChipId) {
                dialogBinding.chipOption1.id -> 1
                dialogBinding.chipOption2.id -> 2
                dialogBinding.chipOption3.id -> 3
                dialogBinding.chipOption4.id -> 4
                else -> 1
            }
            return preset.puntosBase * multiplicador * 5
        }

        fun updatePoints() {
            val totalPoints = calcularPuntosActuales()
            val rewardText = if (preset.recompensaTipo == RecompensaTipo.GEMAS) context.getString(R.string.reward_type_gems).lowercase() else "pts"
            dialogBinding.tvRewardPointsDialog.text = "+$totalPoints $rewardText"
        }

        dialogBinding.cgGoalOptions.setOnCheckedChangeListener { _, _ -> updatePoints() }
        dialogBinding.cgGoalOptions.check(dialogBinding.chipOption2.id) 
        updatePoints()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnConfirm.setOnClickListener {
            val valorSeleccionado = when (dialogBinding.cgGoalOptions.checkedChipId) {
                dialogBinding.chipOption1.id -> {
                    if (esPasos) 5000f else preset.timeValues?.getOrNull(0) ?: 5f
                }
                dialogBinding.chipOption2.id -> {
                    if (esPasos) 10000f else preset.timeValues?.getOrNull(1) ?: 15f
                }
                dialogBinding.chipOption3.id -> {
                    if (esPasos) 15000f else preset.timeValues?.getOrNull(2) ?: 30f
                }
                dialogBinding.chipOption4.id -> {
                    preset.timeValues?.getOrNull(3) ?: 60f
                }
                else -> 10f
            }

            val puntos = calcularPuntosActuales()
            crearHabitoDefinitivo(preset, valorSeleccionado, puntos)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun crearHabitoDefinitivo(preset: HabitoPreset, objetivo: Float, recompensaCalculada: Int) {
        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val context = requireContext()
        val nombreHabito = context.getString(preset.nombre)
        
        val yaExiste = viewModel.habitosActivos.value?.any { 
            it.nombre.equals(nombreHabito, ignoreCase = true) && !it.completadoHoy 
        } ?: false

        if (yaExiste) {
            val message = getString(R.string.preset_already_exists, nombreHabito)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            return
        }

        val tipoRecompensa = if (preset.categoriaId == 1) RecompensaTipo.GEMAS else RecompensaTipo.MONEDAS
        val duracion = if (preset.verificable) 0 else objetivo.toInt()

        val nuevoHabito = HabitoEntity(
            idUsuario = idUsuario,
            nombre = nombreHabito,
            dificultad = preset.dificultad,
            icono = preset.icon,
            objetivoDiario = 1, 
            esVerificable = preset.verificable,
            tipoHealthConnect = preset.healthConnectType,
            categoria = preset.categoriaEnum,
            tipoRecompensa = tipoRecompensa,
            valorObjetivo = objetivo,
            duracionMinutos = duracion,
            valorRecompensa = recompensaCalculada
        )

        viewModel.crearHabitoConResultado(nuevoHabito) { createdHabitId ->
            findNavController().popBackStack(R.id.navigation_home, false)
            val hostView = requireActivity().findViewById<View>(android.R.id.content)
            val successMsg = getString(R.string.preset_accepted, nombreHabito)
            Snackbar.make(hostView, successMsg, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottomAppBar)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
