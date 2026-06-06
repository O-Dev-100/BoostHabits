package com.boosthabits.ui.habitos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.repository.HabitoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class HabitosViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val repositorio: HabitoRepository
    
    val habitosActivos: LiveData<List<HabitoEntity>>
    val habitosPendientesHoy: LiveData<List<HabitoEntity>>
    val totalPuntos: LiveData<Int?>
    val completadosHoy: LiveData<Int>

    private val _eventoError = MutableLiveData<String?>()
    val eventoError: LiveData<String?> = _eventoError

    init {
        val db = AppDatabase.getDatabase(aplicacion)
        repositorio = HabitoRepository(db, aplicacion)
        
        habitosActivos = repositorio.obtenerHabitosActivos().asLiveData()
        habitosPendientesHoy = repositorio.obtenerHabitosPendientesHoy().asLiveData()
        totalPuntos = repositorio.obtenerTotalPuntos().asLiveData()
        completadosHoy = repositorio.obtenerCompletadosHoy(LocalDate.now().toEpochDay()).asLiveData()
    }

    fun cargarHabitos() {

    }

    fun crearHabito(habito: HabitoEntity) {
        viewModelScope.launch {
            repositorio.insertarHabito(habito).onFailure { error ->
                _eventoError.postValue(error.message)
            }
        }
    }

    fun crearHabitoConResultado(habito: HabitoEntity, alCrear: (Long) -> Unit) {
        viewModelScope.launch {
            repositorio.insertarHabito(habito).onSuccess { idHabito ->
                withContext(Dispatchers.Main) {
                    alCrear(idHabito)
                }
            }.onFailure { error ->
                _eventoError.postValue(error.message)
            }
        }}

    fun limpiarError() {
        _eventoError.value = null
    }

    fun actualizarHabito(habito: HabitoEntity) {
        viewModelScope.launch {
            repositorio.actualizarHabito(habito)
        }
    }

    fun eliminarHabito(idHabito: Long) {
        viewModelScope.launch {
            repositorio.eliminarHabito(idHabito)
        }
    }

    fun insertarHabitosPredeterminados() {
        viewModelScope.launch {
            repositorio.insertarHabitosPredeterminados()
        }
    }

    fun marcarComoCompletado(idHabito: Long) {
        viewModelScope.launch {
            repositorio.marcarComoCompletado(idHabito)
        }
    }
}
