package com.boosthabits.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.BitmapFactory
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.health.connect.client.PermissionController
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.boosthabits.R
import com.boosthabits.data.local.entity.RecompensaTipo
import com.boosthabits.databinding.FragmentHomeBinding
import com.boosthabits.ui.perfil.CosmeticoManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by activityViewModels()
    
    private lateinit var habitsAdapter: HomeHabitosAdapter
    private lateinit var waitingAdapter: HomeHabitosAdapter
    private lateinit var statsAdapter: HomeStatsAdapter
    private val auth = FirebaseAuth.getInstance()

    // este es el launcher para permisos de Health Connect
    private val requestPermissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        val requested = viewModel.eventoSolicitudPermisosSalud.value ?: emptySet()
        if (grantedPermissions.containsAll(requested)) {
            Snackbar.make(binding.root, "¡Conectado! Pulsa 'Completar' de nuevo para empezar.", Snackbar.LENGTH_SHORT).show()
        } else {
            // si el usuario no aceptó todo en la ventana de Health Connect
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Acceso incompleto")
                .setMessage("Para que BoostHabits pueda leer tus pasos o ejercicios, debes activar los interruptores dentro de la configuración de Salud.")
                .setPositiveButton("Ir a Ajustes") { _, _ ->
                    startActivity(viewModel.obtenerIntentDeAjustesHealthConnect())
                }
                .setNegativeButton("Entendido", null)
                .show()
        }
        viewModel.limpiarSolicitudPermisosSalud()
    }

    // para el permiso de notificaciones (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Snackbar.make(binding.root, "Las notificaciones están desactivadas. No recibirás avisos de progreso.", Snackbar.LENGTH_LONG).show()
        }
    }

    // Para el permiso de Actividad Física (el diálogo "profesional" simple)
    private val requestActivityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // después de este diálogo, pasamos al flujo de Health Connect si es necesario
        viewModel.eventoSolicitudPermisosSalud.value?.let { permissions ->
            manejarFlujoHealthConnect(permissions)
        }
    }


    private var lastClickedRewardView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarInfoUsuario()
        configurarCarruselEstadisticas()
        configurarListas()
        configurarHabitosRecomendados()
        observarEstado()
        
        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // lanzamos la notificacion de bienvenida si es la primera vez en el dia
        if (savedInstanceState == null) {
            viewModel.lanzarNotificacionBienvenida()
        }
    }

    private fun configurarHabitosRecomendados() {
        aplicarAnimacionClick(binding.btnVerCategorias) { navegarAPredeterminado(4, "Salud Mental") }
        aplicarAnimacionClick(binding.cardSugeridaHome) { navegarAPredeterminado(4, "Salud Mental") }
        
        aplicarAnimacionClick(binding.btnFocusMental) {
            // solo representativo en sugerencias
        }

        aplicarAnimacionClick(binding.btnVerNutricion) { navegarAPredeterminado(3, "Nutrición") }
        aplicarAnimacionClick(binding.cardSugeridaHome2) { navegarAPredeterminado(3, "Nutrición") }

        aplicarAnimacionClick(binding.btnVerHogar) { navegarAPredeterminado(5, "Hogar") }
        aplicarAnimacionClick(binding.cardSugeridaHome3) { navegarAPredeterminado(5, "Hogar") }

        aplicarAnimacionFlotante(binding.cardSugeridaHome, 0L)
        aplicarAnimacionFlotante(binding.cardSugeridaHome2, 400L)
        aplicarAnimacionFlotante(binding.cardSugeridaHome3, 800L)
    }

    private fun navegarAPredeterminado(categoryId: Int, title: String) {
        val bundle = Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryTitle", title)
        }
        findNavController().navigate(R.id.presetListFragment, bundle)
    }

    private fun aplicarAnimacionClick(view: View, action: () -> Unit) {
        view.setOnClickListener {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.95f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.95f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
                duration = 150
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) { action() }
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                start()
            }
        }
    }

    private fun aplicarAnimacionFlotante(view: View, delay: Long) {
        val floatUp = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -15f)
        floatUp.duration = 1500
        floatUp.repeatCount = android.animation.ValueAnimator.INFINITE
        floatUp.repeatMode = android.animation.ValueAnimator.REVERSE
        floatUp.startDelay = delay
        floatUp.start()
    }

    private fun configurarInfoUsuario() {
        val user = auth.currentUser
        if (user != null) {
            val name = user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
            binding.tvSaludo.text = getString(R.string.home_welcome_personalized, name)
            
            // reaplicar efecto de nombre si existe
            viewModel.estadisticasUsuario.value?.let { stats ->
                if (stats.idNombreEquipado == "3") CosmeticoManager.applyLegendaryNameEffect(binding.tvSaludo)
            }

            val stats = viewModel.estadisticasUsuario.value
            if (stats?.idFotoPerfilEquipada != null) {
                val pfpResName = when(stats.idFotoPerfilEquipada) {
                    "9" -> "pfp_agua"
                    "10" -> "pfp_yoga"
                    "11" -> "pfp_apple"
                    "12" -> "pfp_libro"
                    "13" -> "pfp_salad"
                    "14" -> "pfp_shoes"
                    else -> null
                }
                if (pfpResName != null) {
                    val resId = resources.getIdentifier(pfpResName, "mipmap", requireContext().packageName)
                    if (resId != 0) binding.avatar.setImageResource(resId)
                }
            } else {
                user.photoUrl?.let {
                    binding.avatar.load(it) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher_round)
                        transformations(CircleCropTransformation())
                    }
                } ?: binding.avatar.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }

    private fun configurarCarruselEstadisticas() {
        statsAdapter = HomeStatsAdapter(
            alPulsarAnadirHabito = {
                findNavController().navigate(R.id.categorySelectorFragment)
            }
        )
        binding.vpStatsCards.adapter = statsAdapter
        
        // conectar el ViewPager2 con el TabLayout (indicador de puntos)
        TabLayoutMediator(binding.tlStatsIndicator, binding.vpStatsCards) { _, _ -> }.attach()
    }

    private fun configurarListas() {
        habitsAdapter = HomeHabitosAdapter(
            alPulsarCompletar = { habito, view -> 
                lastClickedRewardView = view
                viewModel.intentarCompletar(habito) 
            },
            alPulsarEliminar = { habito ->
                mostrarConfirmacionEliminar(habito)
            },
            alPulsarLargo = { // detalle / },
            alPulsarFocus = { habito ->
                val bundle = Bundle().apply {
                    putLong("habitId", habito.id)
                    putString("habitName", habito.nombre)
                    putInt("durationMinutes", if (habito.duracionMinutos > 0) habito.duracionMinutos else 15)
                    putBoolean("isSaludMental", habito.categoria == com.boosthabits.data.local.entity.HabitCategory.SALUD_MENTAL)
                }
                findNavController().navigate(R.id.focusFragment, bundle)
            }
        )
        binding.rvHabitosHoy.adapter = habitsAdapter

        waitingAdapter = HomeHabitosAdapter(
            alPulsarCompletar = { habito, view -> 
                lastClickedRewardView = view
                viewModel.intentarCompletar(habito) 
            },
            alPulsarLargo = {},
            esSeccionEspera = true
        )
        binding.rvHabitosEspera.adapter = waitingAdapter
    }

    private fun mostrarConfirmacionEliminar(habito: com.boosthabits.data.local.entity.HabitoEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.home_delete_title)
            .setMessage(getString(R.string.home_delete_message, habito.nombre))
            .setPositiveButton(R.string.home_delete_positive) { _, _ ->
                viewModel.eliminarHabito(habito.id)
                Snackbar.make(binding.root, R.string.home_delete_success, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.home_delete_negative, null)
            .show()
    }

    private fun observarEstado() {
        viewModel.estadisticasUsuario.observe(viewLifecycleOwner) { stats ->
            CosmeticoManager.applyCosmetics(
                context = requireContext(),
                stats = stats,
                nameTextView = binding.tvSaludo,
                avatarView = null,
                backgroundView = binding.root
            )
            
            // para el Home, el avatar no es CustomAvatarView, es ShapeableImageView. se podría cambiar
            configurarInfoUsuario() 
        }

        viewModel.totalMonedas.observe(viewLifecycleOwner) { monedas ->
            if (binding.tvCoinsValue.text != monedas.toString()) {
                binding.tvCoinsValue.text = monedas.toString()
                actualizarAdaptadorEstadisticas()
                animarCambioValor(binding.coinsChip)
            }
        }
        viewModel.totalGemas.observe(viewLifecycleOwner) { gemas ->
            if (binding.tvGemsValue.text != gemas.toString()) {
                binding.tvGemsValue.text = gemas.toString()
                actualizarAdaptadorEstadisticas()
                animarCambioValor(binding.gemsChip)
            }
        }
        viewModel.completadosHoy.observe(viewLifecycleOwner) { completados ->
            binding.tvHabitosConteo.text = "✅ $completados"
        }
        viewModel.habitosPendientesHoy.observe(viewLifecycleOwner) { habitos ->
            habitsAdapter.submitList(habitos)
            if (habitos.isNotEmpty()) binding.rvHabitosHoy.scheduleLayoutAnimation()
            actualizarVisibilidadPlaceholder(habitos, viewModel.habitosEnEspera.value.orEmpty())
        }

        viewModel.habitosEnEspera.observe(viewLifecycleOwner) { listaEspera ->
            waitingAdapter.submitList(listaEspera)
            if (listaEspera.isNotEmpty()) binding.rvHabitosEspera.scheduleLayoutAnimation()
            val hayEspera = listaEspera.isNotEmpty()
            binding.tituloEnEspera.visibility = if (hayEspera) View.VISIBLE else View.GONE
            binding.rvHabitosEspera.visibility = if (hayEspera) View.VISIBLE else View.GONE
            actualizarVisibilidadPlaceholder(viewModel.habitosPendientesHoy.value.orEmpty(), listaEspera)
        }

        viewModel.progresoActividad.observe(viewLifecycleOwner) { mapaProgreso ->
            val valoresRaw = viewModel.valoresRawActividad.value ?: emptyMap()
            waitingAdapter.actualizarProgresoActividad(mapaProgreso, valoresRaw)
            habitsAdapter.actualizarProgresoActividad(mapaProgreso, valoresRaw)
        }

        viewModel.valoresRawActividad.observe(viewLifecycleOwner) { mapaValoresRaw ->
            val mapaProgreso = viewModel.progresoActividad.value ?: emptyMap()
            waitingAdapter.actualizarProgresoActividad(mapaProgreso, mapaValoresRaw)
            habitsAdapter.actualizarProgresoActividad(mapaProgreso, mapaValoresRaw)
        }

        viewModel.mensajeMotivacional.observe(viewLifecycleOwner) { mensaje ->
            binding.tvSubSaludo.text = mensaje
        }
        
        viewModel.eventoRecompensa.observe(viewLifecycleOwner) { tipo ->
            tipo?.let {
                ejecutarFeedbackRecompensa(it)
                viewModel.limpiarEventoRecompensa()
            }
        }

        viewModel.eventoError.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.limpiarError()
            }
        }

        viewModel.eventoSolicitudPermisosSalud.observe(viewLifecycleOwner) { permisos ->
            permisos?.let {
                manejarRequerimientoSalud(it)
            }
        }

        viewModel.eventoHabitoCreado.observe(viewLifecycleOwner) { creado ->
            if (creado == true) {
                reproducirAnimacionExito("MONEDAS")
                viewModel.limpiarEventoHabitoCreado()
            }
        }
    }

    private fun manejarRequerimientoSalud(permisos: Set<String>) {
        val permissionActivity = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Manifest.permission.ACTIVITY_RECOGNITION
        } else {
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permissionActivity) != PackageManager.PERMISSION_GRANTED) {
            // ete es el diálogo "profesional" simple que pide acceso a la actividad física
            requestActivityRecognitionLauncher.launch(permissionActivity)
        } else {
            manejarFlujoHealthConnect(permisos)
        }
    }

    private fun manejarFlujoHealthConnect(permisos: Set<String>) {
        if (!viewModel.esHealthConnectDisponible()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Health Connect necesario")
                .setMessage("Tu teléfono necesita la app Health Connect de Google para sincronizar pasos y ejercicios.")
                .setPositiveButton("Instalar") { _, _ ->
                    startActivity(viewModel.obtenerIntentDeInstalacion())
                }
                .setNegativeButton("Cancelar", null)
                .show()
            viewModel.limpiarSolicitudPermisosSalud()
            return
        }

        // una explicación antes de lanzar la ventana de permisos de HC
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sincronizar con Salud")
            .setMessage("Ahora se abrirá la configuración de Salud. Por favor, activa los permisos para que BoostHabits pueda leer tu actividad.")
            .setPositiveButton("Continuar") { _, _ ->
                requestPermissionLauncher.launch(permisos)
            }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    private fun actualizarAdaptadorEstadisticas() {
        val monedas = viewModel.totalMonedas.value ?: 0
        val gemas = viewModel.totalGemas.value ?: 0
        statsAdapter.actualizarValores(monedas, gemas)
    }

    private fun actualizarVisibilidadPlaceholder(hoy: List<com.boosthabits.data.local.entity.HabitoEntity>, espera: List<com.boosthabits.data.local.entity.HabitoEntity>) {
        val hoyVacio = hoy.isEmpty()
        val esperaVacio = espera.isEmpty()
        
        binding.layoutEmptyHabits.visibility = if (hoyVacio && esperaVacio) View.VISIBLE else View.GONE
        
        // Mostrar/ocultar sección de hoy
        binding.tituloTusHabitos.visibility = if (hoyVacio) View.GONE else View.VISIBLE
        binding.rvHabitosHoy.visibility = if (hoyVacio) View.GONE else View.VISIBLE

        // Sección de espera ya se maneja en su propio observador, pero por consistencia:
        val Espera = espera.isNotEmpty()
        binding.tituloEnEspera.visibility = if (Espera) View.VISIBLE else View.GONE
        binding.rvHabitosEspera.visibility = if (Espera) View.VISIBLE else View.GONE
    }

    private fun ejecutarFeedbackRecompensa(tipo: RecompensaTipo) {

        vibrar(tipo)
        
        // animación de vuelo
        lastClickedRewardView?.let { startView ->
            val targetView = if (tipo == RecompensaTipo.GEMAS) binding.gemsChip else binding.coinsChip
            animarVueloRecompensa(startView, targetView, tipo)
        }

        reproducirAnimacionExito(tipo.name)
    }

    private fun vibrar(tipo: RecompensaTipo) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (tipo == RecompensaTipo.GEMAS) {
            // vibración doble para gemas, para indicar que es premium
            val timings = longArrayOf(0, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            // vibración simple para monedas
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }


    private fun animarVueloRecompensa(startView: View, targetView: View, tipo: RecompensaTipo) {
        val rootLayout = binding.root as ViewGroup
        

        val iconoVolante = ImageView(requireContext()).apply {
            setImageResource(if (tipo == RecompensaTipo.GEMAS) R.drawable.ic_gemas else R.drawable.ic_monedas)
            layoutParams = ViewGroup.LayoutParams(startView.width, startView.height)
        }
        rootLayout.addView(iconoVolante)


        val startLoc = IntArray(2)
        startView.getLocationInWindow(startLoc)
        val targetLoc = IntArray(2)
        targetView.getLocationInWindow(targetLoc)

        val rootLoc = IntArray(2)
        rootLayout.getLocationInWindow(rootLoc)
        
        val startX = (startLoc[0] - rootLoc[0]).toFloat()
        val startY = (startLoc[1] - rootLoc[1]).toFloat()
        val targetX = (targetLoc[0] - rootLoc[0]).toFloat()
        val targetY = (targetLoc[1] - rootLoc[1]).toFloat()

        iconoVolante.x = startX
        iconoVolante.y = startY


        val animX = ObjectAnimator.ofFloat(iconoVolante, View.X, targetX)
        val animY = ObjectAnimator.ofFloat(iconoVolante, View.Y, targetY)
        val scaleX = ObjectAnimator.ofFloat(iconoVolante, View.SCALE_X, 1f, 1.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(iconoVolante, View.SCALE_Y, 1f, 1.5f, 1f)
        val alpha = ObjectAnimator.ofFloat(iconoVolante, View.ALPHA, 1f, 0.5f)

        AnimatorSet().apply {
            playTogether(animX, animY, scaleX, scaleY, alpha)
            interpolator = AccelerateInterpolator()
            duration = 800
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.removeView(iconoVolante)
                }
                override fun onAnimationCancel(animation: Animator) {
                    rootLayout.removeView(iconoVolante)
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    private fun reproducirAnimacionExito(tipoRecompensa: String) {
        val animationRes = if (tipoRecompensa == "GEMAS") R.raw.success_gems else R.raw.success_coins
        
        binding.lottieOverlay.visibility = View.VISIBLE


        binding.lottieSuccessMain.setImageAssetDelegate { asset ->
            val drawableRes = when (asset.id) {
                "gem_id" -> R.drawable.ic_gemas
                "coin_id" -> R.drawable.ic_monedas
                else -> null
            }
            drawableRes?.let {
                BitmapFactory.decodeResource(resources, it)
            }
        }

        binding.lottieSuccessMain.setAnimation(animationRes)
        binding.lottieSuccessMain.playAnimation()

        binding.lottieSuccessMain.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                binding.lottieOverlay.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {
                binding.lottieOverlay.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun animarCambioValor(view: View) {
        val scaleUp = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f)
        val scaleUpY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
        ObjectAnimator.ofPropertyValuesHolder(view, scaleUp, scaleUpY).apply {
            duration = 200
            repeatCount = 1
            repeatMode = android.animation.ValueAnimator.REVERSE
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
