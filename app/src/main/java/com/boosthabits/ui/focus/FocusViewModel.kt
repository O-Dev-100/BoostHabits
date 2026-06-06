package com.boosthabits.ui.focus

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.repository.HabitoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val repositorio = HabitoRepository(AppDatabase.getDatabase(aplicacion), aplicacion)

    private var temporizador: CountDownTimer? = null
    private var segundosTotales: Long = 0
    private var segundosRestantes: Long = 0
    private var tiempoFin: Long = 0

    private val _estadoTemporizador = MutableStateFlow<EstadoTemporizador>(EstadoTemporizador.Inactivo)
    val estadoTemporizador: StateFlow<EstadoTemporizador> = _estadoTemporizador.asStateFlow()

    private val _cadenaTiempoActual = MutableStateFlow("00:00")
    val cadenaTiempoActual: StateFlow<String> = _cadenaTiempoActual.asStateFlow()

    private val _porcentajeProgreso = MutableStateFlow(100)
    val porcentajeProgreso: StateFlow<Int> = _porcentajeProgreso.asStateFlow()

    private val _multiplicadorRecompensa = MutableStateFlow(1.0f)
    val multiplicadorRecompensa: StateFlow<Float> = _multiplicadorRecompensa.asStateFlow()

    private val _habitoCompletado = MutableStateFlow(false)
    val habitoCompletado: StateFlow<Boolean> = _habitoCompletado.asStateFlow()

    private var idHabitoActual: Long = -1
    private var esSaludMental: Boolean = false

    sealed class EstadoTemporizador {
        object Inactivo : EstadoTemporizador()
        object EnProgreso : EstadoTemporizador()
        object Pausado : EstadoTemporizador()
        object DistraidoPenalizado : EstadoTemporizador()
        object Finalizado : EstadoTemporizador()
    }

    fun iniciarFocus(idHabito: Long, esMental: Boolean) {
        idHabitoActual = idHabito
        esSaludMental = esMental
        _multiplicadorRecompensa.value = if (esSaludMental) 5.0f else 1.5f
    }

    fun iniciarTemporizador(minutos: Int) {
        if (_estadoTemporizador.value == EstadoTemporizador.Inactivo || _estadoTemporizador.value == EstadoTemporizador.DistraidoPenalizado) {
            segundosTotales = minutos * 60L
            segundosRestantes = segundosTotales
            tiempoFin = System.currentTimeMillis() + (segundosRestantes * 1000)
            _multiplicadorRecompensa.value = if (esSaludMental) 5.0f else 1.5f
        } else if (_estadoTemporizador.value == EstadoTemporizador.Pausado) {
            tiempoFin = System.currentTimeMillis() + (segundosRestantes * 1000)
        }

        _estadoTemporizador.value = EstadoTemporizador.EnProgreso

        temporizador?.cancel()
        temporizador = object : CountDownTimer(segundosRestantes * 1000, 1000) {
            override fun onTick(milisHastaFin: Long) {
                // Sincronizar con el tiempo real para evitar deriva y soportar bloqueos breves
                segundosRestantes = (tiempoFin - System.currentTimeMillis()) / 1000
                if (segundosRestantes < 0) segundosRestantes = 0
                actualizarDatosUI()
            }

            override fun onFinish() {
                _estadoTemporizador.value = EstadoTemporizador.Finalizado
                segundosRestantes = 0
                actualizarDatosUI()
                completarHabito()
            }
        }.start()
    }

    fun pausarTemporizadorManual() {
        temporizador?.cancel()
        // Guardamos cuánto quedaba para poder reanudar basándonos en tiempo absoluto
        segundosRestantes = (tiempoFin - System.currentTimeMillis()) / 1000
        if (segundosRestantes < 0) segundosRestantes = 0
        _estadoTemporizador.value = EstadoTemporizador.Pausado
    }

    fun activarPenalizacionPorDistraccion() {
        if (_estadoTemporizador.value == EstadoTemporizador.EnProgreso) {
            temporizador?.cancel()
            segundosRestantes = (tiempoFin - System.currentTimeMillis()) / 1000
            if (segundosRestantes < 0) segundosRestantes = 0
            _estadoTemporizador.value = EstadoTemporizador.DistraidoPenalizado
            _multiplicadorRecompensa.value = 1.0f // Pierde el bonus
        }
    }

    fun reiniciarTemporizador() {
        temporizador?.cancel()
        _estadoTemporizador.value = EstadoTemporizador.Inactivo
        segundosRestantes = 0
        actualizarDatosUI()
    }

    private fun actualizarDatosUI() {
        val minutos = segundosRestantes / 60
        val segundos = segundosRestantes % 60
        _cadenaTiempoActual.value = String.format("%02d:%02d", minutos, segundos)

        _porcentajeProgreso.value = if (segundosTotales > 0) {
            ((segundosRestantes.toFloat() / segundosTotales.toFloat()) * 100).toInt()
        } else 100
    }

    private fun completarHabito() {
        if (idHabitoActual == -1L) return
        viewModelScope.launch {
            repositorio.marcarComoCompletado(idHabitoActual, _multiplicadorRecompensa.value)
                .onSuccess {
                    _habitoCompletado.value = true
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        temporizador?.cancel()
    }
}
