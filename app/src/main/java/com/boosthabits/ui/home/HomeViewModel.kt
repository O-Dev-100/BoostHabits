package com.boosthabits.ui.home

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.boosthabits.R
import com.boosthabits.service.TrackingService
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.HabitCategory
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.RecompensaTipo
import com.boosthabits.data.local.entity.UserStatsEntity
import com.boosthabits.data.repository.HabitoRepository
import com.boosthabits.utils.NotificacionHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random
import com.boosthabits.ui.home.state.HomeUiState

// logica principal de la pantalla de inicio
class HomeViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val repositorio: HabitoRepository
    private val baseDatos = AppDatabase.getDatabase(aplicacion)
    private val ayudanteNotificaciones = NotificacionHelper(aplicacion)
    
    private val _estadoUI = MutableStateFlow(HomeUiState())
    val estadoUI: StateFlow<HomeUiState> = _estadoUI.asStateFlow()

    val habitosPendientesHoy: LiveData<List<HabitoEntity>> = estadoUI.map { it.habitosPendientes }.asLiveData()
    val habitosEnEspera: LiveData<List<HabitoEntity>> = estadoUI.map { it.habitosEnEspera }.asLiveData()
    val estadisticasUsuario: LiveData<UserStatsEntity?> = estadoUI.map { it.estadisticasUsuario }.asLiveData()
    val totalMonedas: LiveData<Int> = estadoUI.map { it.totalMonedas }.asLiveData()
    val totalGemas: LiveData<Int> = estadoUI.map { it.totalGemas }.asLiveData()
    val completadosHoy: LiveData<Int> = estadoUI.map { it.completadosHoy }.asLiveData()
    val mensajeMotivacional: LiveData<String> = estadoUI.map { it.motivationalMessage }.asLiveData()

    private val _eventoError = MutableLiveData<String?>()
    val eventoError: LiveData<String?> = _eventoError

    private val _eventoSolicitudPermisosSalud = MutableLiveData<Set<String>?>()
    val eventoSolicitudPermisosSalud: LiveData<Set<String>?> = _eventoSolicitudPermisosSalud

    private val _eventoRecompensa = MutableLiveData<RecompensaTipo?>()
    val eventoRecompensa: LiveData<RecompensaTipo?> = _eventoRecompensa

    private val _eventoHabitoCreado = MutableLiveData<Boolean>()
    val eventoHabitoCreado: LiveData<Boolean> = _eventoHabitoCreado

    private val _progresoActividad = MutableLiveData<Map<Long, Int>>(emptyMap())
    val progresoActividad: LiveData<Map<Long, Int>> = _progresoActividad

    private val _valoresRawActividad = MutableLiveData<Map<Long, String>>(emptyMap())
    val valoresRawActividad: LiveData<Map<Long, String>> = _valoresRawActividad

    private var estaMonitoreandoSalud = false

    private val mensajes = listOf<Int>(
        R.string.motivational_1, R.string.motivational_2, R.string.motivational_3,
        R.string.motivational_4, R.string.motivational_5, R.string.motivational_6,
        R.string.motivational_7, R.string.motivational_8, R.string.motivational_9,
        R.string.motivational_10
    )

    init {
        repositorio = HabitoRepository(baseDatos, aplicacion)
        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val hoy = LocalDate.now().toEpochDay()

        viewModelScope.launch {
            combine(
                baseDatos.habitDao().obtenerHabitosPendientesHoy(idUsuario, hoy),
                baseDatos.habitDao().obtenerHabitosEnEspera(idUsuario),
                baseDatos.userStatsDao().obtenerEstadisticasUsuario(idUsuario),
                repositorio.obtenerCompletadosHoy(hoy)
            ) { pendientes, espera, estadisticas, completados ->
                HomeUiState(
                    habitosPendientes = pendientes,
                    habitosEnEspera = espera,
                    estadisticasUsuario = estadisticas,
                    totalMonedas = estadisticas?.monedas ?: 0,
                    totalGemas = estadisticas?.gemas ?: 0,
                    completadosHoy = completados,
                    motivationalMessage = _estadoUI.value.motivationalMessage.ifEmpty { 
                        aplicacion.getString(mensajes[Random.nextInt(mensajes.size)]) 
                    }
                )
            }.collect { nuevoEstado ->
                _estadoUI.value = nuevoEstado
            }
        }
        
        iniciarMonitoreoSalud()
    }

    private fun iniciarMonitoreoSalud() {
        if (estaMonitoreandoSalud) return
        estaMonitoreandoSalud = true
        viewModelScope.launch {
            while (estaMonitoreandoSalud) {
                actualizarTodoElProgresoVerificable()
                
                val espera = _estadoUI.value.habitosEnEspera
                if (espera.any { it.esVerificable }) {
                    val intent = Intent(getApplication(), TrackingService::class.java)
                    ContextCompat.startForegroundService(getApplication(), intent)
                }
                delay(10000)
            }
        }
    }

    // calcula el progreso de pasos y ejercicio usando health connect
    private suspend fun actualizarTodoElProgresoVerificable() {
        val espera = _estadoUI.value.habitosEnEspera
        val verificablesEnEspera = espera.filter { it.esVerificable }
        val pendientes = _estadoUI.value.habitosPendientes
        val verificablesPendientes = pendientes.filter { it.esVerificable }
        
        val todosLosVerificables = (verificablesEnEspera + verificablesPendientes).distinctBy { it.id }

        if (todosLosVerificables.isNotEmpty()) {
            val mapaProgreso = _progresoActividad.value.orEmpty().toMutableMap()
            val mapaValoresRaw = _valoresRawActividad.value.orEmpty().toMutableMap()
            
            todosLosVerificables.forEach { habito ->
                val progreso = repositorio.obtenerProgresoHabito(habito)
                val progresoPrevio = mapaProgreso[habito.id] ?: 0
                val nombre = habito.nombre.lowercase()
                val esPasos = nombre.contains("pasos") || nombre.contains("steps") || 
                              nombre.contains("caminar") || nombre.contains("walk")
                
                val valorRaw = when {
                    esPasos -> {
                        val pasos = repositorio.healthConnectManager.obtenerPasosHoy()
                        val sufijo = getApplication<Application>().getString(R.string.stats_steps)
                        "${pasos.toInt()} / ${habito.valorObjetivo.toInt()} $sufijo"
                    }
                    habito.tipoHealthConnect != null -> {
                        val duracion = repositorio.healthConnectManager.obtenerDuracionEjercicioMinutosPorTipo(habito.tipoHealthConnect!!)
                        "${duracion.toInt()} / ${habito.valorObjetivo.toInt()} min"
                    }
                    else -> "${habito.valorActual.toInt()} / ${habito.valorObjetivo.toInt()}"
                }
                
                mapaProgreso[habito.id] = progreso
                mapaValoresRaw[habito.id] = valorRaw
                
                if (progreso.toFloat() != habito.valorActual) {
                    viewModelScope.launch {
                        repositorio.actualizarHabito(habito.copy(valorActual = progreso.toFloat()))
                    }
                }
                
                if (progreso != progresoPrevio && espera.any { it.id == habito.id }) {
                    if (progreso >= 100 && progresoPrevio < 100) {
                        ayudanteNotificaciones.mostrarNotificacionCompletado(habito.id, habito.nombre)
                    } else if (progreso < 100) {
                        ayudanteNotificaciones.mostrarNotificacionProgreso(habito.id, habito.nombre, progreso, valorRaw)
                    }
                }
            }
            _progresoActividad.postValue(mapaProgreso)
            _valoresRawActividad.postValue(mapaValoresRaw)
        }
    }

    fun intentarCompletar(habito: HabitoEntity) {
        if (habito.tiempoInicioEspera == 0L) {
            viewModelScope.launch {
                if (habito.esVerificable) {
                    val requeridos = repositorio.healthConnectManager.obtenerPermisosRequeridosParaHabito(habito)
                    if (!repositorio.healthConnectManager.tienePermisos(requeridos)) {
                        _eventoSolicitudPermisosSalud.postValue(requeridos)
                        return@launch
                    }
                    actualizarTodoElProgresoVerificable()
                    val progresoActual = _progresoActividad.value?.get(habito.id) ?: 0
                    if (progresoActual >= 100) {
                        verificarYFinalizar(habito.copy(tiempoInicioEspera = 1L))
                        return@launch
                    }
                }
                val habitoActualizado = habito.copy(
                    tiempoInicioEspera = System.currentTimeMillis(),
                    completadoHoy = false
                )
                repositorio.actualizarHabito(habitoActualizado)
                _eventoError.postValue(getApplication<Application>().getString(R.string.home_status_starting, habito.nombre))
                
                if (habito.esVerificable) {
                    val intent = Intent(getApplication(), TrackingService::class.java)
                    ContextCompat.startForegroundService(getApplication(), intent)
                    actualizarTodoElProgresoVerificable()
                }
            }
        } else {
            verificarYFinalizar(habito)
        }
    }

    private fun verificarYFinalizar(habito: HabitoEntity) {
        viewModelScope.launch {
            if (habito.esVerificable) {
                val progreso = repositorio.obtenerProgresoHabito(habito)
                if (progreso < 100) {
                    _eventoError.postValue(getApplication<Application>().getString(R.string.home_status_not_reached, progreso))
                    return@launch
                }
            } else {
                val duracionEfectiva = if (habito.duracionMinutos > 0) habito.duracionMinutos else 2
                val milisTranscurridos = System.currentTimeMillis() - habito.tiempoInicioEspera
                val milisRequeridos = duracionEfectiva * 60 * 1000L

                if (milisTranscurridos < milisRequeridos) {
                    val restante = (milisRequeridos - milisTranscurridos) / 1000
                    _eventoError.postValue(getApplication<Application>().getString(R.string.home_status_wait, restante / 60, restante % 60))
                    return@launch
                }
            }

            repositorio.marcarComoCompletado(habito.id)
                .onSuccess {
                    ayudanteNotificaciones.cancelarNotificacion(habito.id)
                    val tRecompensa = if (habito.categoria == HabitCategory.DEPORTE || habito.tipoRecompensa == RecompensaTipo.GEMAS)
                                    RecompensaTipo.GEMAS else RecompensaTipo.MONEDAS
                    _eventoRecompensa.postValue(tRecompensa)
                }
                .onFailure { error ->
                    _eventoError.postValue(error.message)
                }
        }
    }

    fun eliminarHabito(idHabito: Long) {
        viewModelScope.launch { repositorio.eliminarHabito(idHabito) }
    }

    fun notificarHabitoCreado() { _eventoHabitoCreado.value = true }
    fun limpiarError() { _eventoError.value = null }
    fun limpiarEventoRecompensa() { _eventoRecompensa.value = null }
    fun limpiarEventoHabitoCreado() { _eventoHabitoCreado.value = false }
    fun limpiarSolicitudPermisosSalud() { _eventoSolicitudPermisosSalud.value = null }

    fun lanzarNotificacionBienvenida() {
        val preferencias = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hoy = LocalDate.now().toString()
        if (preferencias.getString("last_welcome_notif", "") != hoy) {
            val usuario = FirebaseAuth.getInstance().currentUser
            val nombre = usuario?.displayName ?: usuario?.email?.substringBefore("@") ?: "Usuario"
            ayudanteNotificaciones.mostrarNotificacionBienvenida(nombre)
            preferencias.edit().putString("last_welcome_notif", hoy).apply()
        }
    }

    fun obtenerIntentDeAjustesHealthConnect(): Intent = repositorio.healthConnectManager.obtenerIntentDeAjustesHealthConnect()
    fun esGoogleFitInstalado(): Boolean = repositorio.healthConnectManager.estaGoogleFitInstalado()
    fun obtenerIntentDeInstalacionGoogleFit(): Intent = repositorio.healthConnectManager.obtenerIntentDeInstalacionGoogleFit()
    fun esHealthConnectDisponible(): Boolean = repositorio.healthConnectManager.estaHealthConnectDisponible()
    fun obtenerIntentDeInstalacion(): Intent = repositorio.healthConnectManager.obtenerIntentDeInstalacion()

    override fun onCleared() {
        super.onCleared()
        estaMonitoreandoSalud = false
    }
}
