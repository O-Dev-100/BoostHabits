package com.boosthabits.ui.perfil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.data.local.entity.UserStatsEntity
import com.boosthabits.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val auth = FirebaseAuth.getInstance()
    private val authRepo = AuthRepository()
    
    val userStats: LiveData<UserStatsEntity?> = obtenerFlujoEstadisticasUsuario().asLiveData()
    val unlockedRewards: LiveData<List<RecompensaEntity>> = obtenerFlujoRecompensasDesbloqueadas().asLiveData()

    private val _profileUpdateResult = MutableLiveData<Result<Unit>?>()
    val profileUpdateResult: LiveData<Result<Unit>?> = _profileUpdateResult

    private val _passwordUpdateResult = MutableLiveData<Result<Unit>?>()
    val passwordUpdateResult: LiveData<Result<Unit>?> = _passwordUpdateResult

    private fun obtenerFlujoEstadisticasUsuario(): Flow<UserStatsEntity?> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return database.userStatsDao().obtenerEstadisticasUsuario(idUsuario)
    }

    private fun obtenerFlujoRecompensasDesbloqueadas(): Flow<List<RecompensaEntity>> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return database.rewardDao().obtenerRecompensasDesbloqueadas(idUsuario)
    }

    fun actualizarNombre(nuevoNombre: String) {
        authRepo.actualizarPerfil(
            nombrePantalla = nuevoNombre,
            onOk = { _profileUpdateResult.postValue(Result.success(Unit)) },
            onError = { ex -> _profileUpdateResult.postValue(Result.failure<Unit>(ex)) }
        )
    }

    fun cambiarPassword(nuevaContrasena: String) {
        authRepo.cambiarPassword(
            nuevaContrasena = nuevaContrasena,
            onOk = { _passwordUpdateResult.postValue(Result.success(Unit)) },
            onError = { ex -> _passwordUpdateResult.postValue(Result.failure<Unit>(ex)) }
        )
    }

    fun limpiarResultados() {
        _profileUpdateResult.value = null
        _passwordUpdateResult.value = null
    }

    fun alternarCosmetico(recompensa: RecompensaEntity) {
        val idUsuario = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            when {
                recompensa.titulo.contains("Nombre", ignoreCase = true) -> {
                    val actual = userStats.value?.idNombreEquipado
                    val siguiente = if (actual == recompensa.id) null else recompensa.id
                    database.userStatsDao().equiparNombre(idUsuario, siguiente)
                }
                recompensa.titulo.contains("Marco", ignoreCase = true) -> {
                    val actual = userStats.value?.idMarcoAvatarEquipado
                    val siguiente = if (actual == recompensa.id) null else recompensa.id
                    database.userStatsDao().equiparMarcoAvatar(idUsuario, siguiente)
                }
                recompensa.titulo.contains("Fondo", ignoreCase = true) -> {
                    val actual = userStats.value?.idFondoPantallaEquipado
                    val siguiente = if (actual == recompensa.id) null else recompensa.id
                    database.userStatsDao().equiparFondoPantalla(idUsuario, siguiente)
                }
                recompensa.titulo.contains("Perfil", ignoreCase = true) -> {
                    val actual = userStats.value?.idFotoPerfilEquipada
                    val siguiente = if (actual == recompensa.id) null else recompensa.id
                    database.userStatsDao().equiparFotoPerfil(idUsuario, siguiente)
                }
            }
        }
    }
}
