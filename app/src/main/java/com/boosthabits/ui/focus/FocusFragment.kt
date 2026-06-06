package com.boosthabits.ui.focus

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.boosthabits.R
import com.boosthabits.databinding.FragmentFocusBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class FocusFragment : Fragment() {

    private var _binding: FragmentFocusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FocusViewModel by viewModels()
    private var reproductorMedios: MediaPlayer? = null
    private val nombresRecursosSonido = mapOf(
        "Silencio Zen" to "focus_zen",
        "Lluvia Profunda" to "focus_lluvia",
        "Bosque Nocturno" to "focus_bosque",
        "Ruido Blanco" to "focus_sonido"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val idHabito = arguments?.getLong("habitId") ?: -1L
        val nombreHabito = arguments?.getString("habitName") ?: "Hábito"
        val duracion = arguments?.getInt("durationMinutes") ?: 15
        val esSaludMental = arguments?.getBoolean("isSaludMental") ?: false

        viewModel.iniciarFocus(idHabito, esSaludMental)

        binding.tvHabitName.text = "$nombreHabito: $duracion min"
        
        mostrarDialogoInstrucciones()
        configurarRetroceso()
        configurarSelectorSonido()
        configurarOyentes(duracion)
        observarViewModel()
    }

    private fun configurarSelectorSonido() {
        val sonidos = nombresRecursosSonido.keys.toList()
        val adaptador = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, sonidos) {
            override fun getView(posicion: Int, convertView: View?, padre: ViewGroup): View {
                val vista = super.getView(posicion, convertView, padre) as TextView
                val resIcono = when (getItem(posicion)) {
                    "Silencio Zen" -> R.drawable.icono_zen
                    "Lluvia Profunda" -> R.drawable.ic_rain
                    "Bosque Nocturno" -> R.drawable.ic_bosque
                    "Ruido Blanco" -> R.drawable.ic_sonido
                    else -> 0
                }
                vista.setCompoundDrawablesWithIntrinsicBounds(resIcono, 0, 0, 0)
                vista.compoundDrawablePadding = 16
                return vista
            }
        }
        binding.actvSoundSelector.setAdapter(adaptador)
        binding.actvSoundSelector.setText(sonidos[0], false)
        binding.tilSoundSelector.setStartIconDrawable(R.drawable.icono_zen)

        binding.actvSoundSelector.onItemClickListener = AdapterView.OnItemClickListener { _, _, posicion, _ ->
            val seleccionado = sonidos[posicion]
            val resIcono = when (seleccionado) {
                "Silencio Zen" -> R.drawable.icono_zen
                "Lluvia Profunda" -> R.drawable.ic_rain
                "Bosque Nocturno" -> R.drawable.ic_bosque
                "Ruido Blanco" -> R.drawable.ic_sonido
                else -> 0
            }
            binding.tilSoundSelector.setStartIconDrawable(resIcono)
            
            if (viewModel.estadoTemporizador.value == FocusViewModel.EstadoTemporizador.EnProgreso) {
                reproducirSonidoSeleccionado()
            }
        }
    }

    private fun mostrarDialogoInstrucciones() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modo Focus Activo")
            .setMessage("Bienvenido al Nexo de Concentración.\n\n" +
                    "• Mantén la app abierta para asegurar tu bonus.\n" +
                    "• Si sales o cambias de app, perderás el multiplicador x5.\n" +
                    "• Al terminar, recibirás tu recompensa potenciada.\n\n" +
                    "¿Estás listo para concentrarte?")
            .setPositiveButton("¡Listo!") { _, _ -> }
            .show()
    }

    private fun configurarRetroceso() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.estadoTemporizador.value == FocusViewModel.EstadoTemporizador.EnProgreso) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("¿Abandonar Enfoque?")
                        .setMessage("Si sales ahora, perderás el progreso actual y el bonus de recompensa.")
                        .setPositiveButton("Salir") { _, _ -> 
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .setNegativeButton("Quedarme", null)
                        .show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun reproducirSonidoSeleccionado() {
        detenerSonido()
        val seleccionado = binding.actvSoundSelector.text.toString()
        val nombreRes = nombresRecursosSonido[seleccionado] ?: return
        val idRes = resources.getIdentifier(nombreRes, "raw", requireContext().packageName)
        
        if (idRes == 0) return

        try {
            reproductorMedios = MediaPlayer.create(requireContext(), idRes)
            reproductorMedios?.let {
                it.isLooping = true
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun detenerSonido() {
        reproductorMedios?.stop()
        reproductorMedios?.release()
        reproductorMedios = null
    }

    private fun configurarOyentes(duracion: Int) {
        binding.btnStartPause.setOnClickListener {
            when (viewModel.estadoTemporizador.value) {
                FocusViewModel.EstadoTemporizador.Inactivo, FocusViewModel.EstadoTemporizador.Pausado, FocusViewModel.EstadoTemporizador.DistraidoPenalizado -> {
                    viewModel.iniciarTemporizador(duracion)
                }
                FocusViewModel.EstadoTemporizador.EnProgreso -> {
                    viewModel.pausarTemporizadorManual()
                }
                else -> {}
            }
        }
    }

    private fun observarViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.estadoTemporizador.collect { estado ->
                        actualizarUIPorEstado(estado)
                    }
                }
                launch {
                    viewModel.cadenaTiempoActual.collect { tiempo ->
                        binding.tvTimeLeft.text = tiempo
                    }
                }
                launch {
                    viewModel.porcentajeProgreso.collect { progreso ->
                        binding.circularProgress.setProgress(progreso, true)
                    }
                }
                launch {
                    viewModel.multiplicadorRecompensa.collect { multiplicador ->
                        val textoBonus = if (multiplicador > 1.0f) "Bonus: ${multiplicador}x" else "Sin Bonus (Distraído)"
                        binding.tvFocusTitle.text = "Enfoque ($textoBonus)"
                    }
                }
                launch {
                    viewModel.habitoCompletado.collect { completado ->
                        if (completado) {
                            reproducirAnimacionExito()
                            mostrarDialogoExito()
                        }
                    }
                }
            }
        }
    }

    private fun reproducirAnimacionExito() {
        binding.lottieOverlay.visibility = View.VISIBLE
        binding.lottieSuccess.playAnimation()
    }

    private fun actualizarUIPorEstado(estado: FocusViewModel.EstadoTemporizador) {
        when (estado) {
            FocusViewModel.EstadoTemporizador.EnProgreso -> {
                binding.btnStartPause.text = "Pausar"
                binding.btnStartPause.setIconResource(android.R.drawable.ic_media_pause)
                reproducirSonidoSeleccionado()
            }
            FocusViewModel.EstadoTemporizador.Pausado -> {
                binding.btnStartPause.text = "Reanudar"
                binding.btnStartPause.setIconResource(android.R.drawable.ic_media_play)
                reproductorMedios?.pause()
            }
            FocusViewModel.EstadoTemporizador.Inactivo -> {
                binding.btnStartPause.text = "Iniciar Enfoque"
                binding.btnStartPause.setIconResource(android.R.drawable.ic_media_play)
                detenerSonido()
            }
            FocusViewModel.EstadoTemporizador.DistraidoPenalizado -> {
                binding.btnStartPause.text = "Reiniciar (Penalizado)"
                binding.btnStartPause.setIconResource(android.R.drawable.ic_media_play)
                detenerSonido()
            }
            FocusViewModel.EstadoTemporizador.Finalizado -> {
                binding.btnStartPause.isEnabled = false
                binding.btnStartPause.text = "¡Completado!"
                detenerSonido()
            }
        }
    }

    private fun mostrarDialogoExito() {
        val multiplicador = viewModel.multiplicadorRecompensa.value
        val mensajeRecompensa = if (multiplicador > 1.0f) {
            "¡Has ganado un bonus de ${multiplicador}x por tu concentración!"
        } else {
            "Hábito completado, pero te has distraído un poco. ¡Sigue intentándolo!"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¡Misión Cumplida!")
            .setMessage("Has mantenido el enfoque y completado tu hábito.\n\n$mensajeRecompensa")
            .setCancelable(false)
            .setPositiveButton("Recoger Recompensa") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // re-sincronizar el cronómetro si estaba corriendo
        if (viewModel.estadoTemporizador.value == FocusViewModel.EstadoTemporizador.EnProgreso) {
            reproducirSonidoSeleccionado()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _binding = null
    }
}
