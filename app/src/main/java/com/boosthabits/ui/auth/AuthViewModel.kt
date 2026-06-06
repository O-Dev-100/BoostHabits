package com.boosthabits.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.repository.AuthRepository
import com.boosthabits.data.repository.HabitoRepository
import com.boosthabits.data.repository.EconomiaRepository
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

// viewmodel para login y registro
class AuthViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val repo = AuthRepository()
    private val db = AppDatabase.getDatabase(aplicacion.applicationContext)
    private val economiaRepository = EconomiaRepository(db)
    private val habitoRepository = HabitoRepository(db, aplicacion.applicationContext)

    private val _estado = MutableLiveData(AuthUiState())
    val estado: LiveData<AuthUiState> = _estado

    fun limpiarMensajes() {
        _estado.value = _estado.value?.copy(mensajeError = null, mensajeInfo = null)
    }

    // descarga habitos y economia tras loguearse
    private suspend fun sincronizarTodo(usuarioId: String) {
        economiaRepository.sincronizarEstadisticasUsuarioSeguro(usuarioId)
        habitoRepository.sincronizarTodoDesdeFirestore(usuarioId)
    }

    fun iniciarSesionEmail(correo: String, contrasena: String) {
        _estado.value = AuthUiState(cargando = true)
        repo.iniciarSesionConEmail(
            correo = correo,
            contrasena = contrasena,
            onOk = { usuario ->
                viewModelScope.launch {
                    sincronizarTodo(usuario.uid)
                    _estado.postValue(AuthUiState(cargando = false, usuario = usuario))
                }
            },
            onError = { ex -> _estado.postValue(AuthUiState(cargando = false, mensajeError = ex.message)) }
        )
    }

    fun registrarUsuarioEmail(correo: String, contrasena: String, nombrePantalla: String) {
        _estado.value = AuthUiState(cargando = true)
        repo.registrarConEmail(
            correo = correo,
            contrasena = contrasena,
            nombrePantalla = nombrePantalla,
            onOk = { usuario ->
                viewModelScope.launch {
                    sincronizarTodo(usuario.uid)
                    _estado.postValue(AuthUiState(cargando = false, usuario = usuario))
                }
            },
            onError = { ex -> _estado.postValue(AuthUiState(cargando = false, mensajeError = ex.message)) }
        ) }


    fun recuperarContrasena(correo: String) {
        _estado.value = AuthUiState(cargando = true)
        repo.enviarRecuperacionPassword(
            correo = correo,
            onOk = {
                _estado.postValue(
                    AuthUiState(
                        cargando = false,
                        mensajeInfo = "Email enviado (si existe una cuenta asociada)."
                    )
                )
            },
            onError = { ex ->
                _estado.postValue(AuthUiState(cargando = false, mensajeError = ex.message))
            }
        )}

    fun iniciarSesionGoogleConIdToken(idToken: String) {
        _estado.value = AuthUiState(cargando = true)
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        repo.iniciarSesionConCredenciales(
            credential = credencial,
            onOk = { usuario ->
                viewModelScope.launch {
                    sincronizarTodo(usuario.uid)
                    _estado.postValue(AuthUiState(cargando = false, usuario = usuario))
                }
            },
            onError = { ex -> _estado.postValue(AuthUiState(cargando = false, mensajeError = ex.message)) }
        )
    }
}
