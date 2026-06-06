package com.boosthabits.ui.auth

import com.google.firebase.auth.FirebaseUser

/**
 * Estado simple de UI para pantallas de autenticación.
 *
 * Es intencionalmente sencillo para el TFG: loading, error (texto) y usuario (si lo hay).
 */
data class AuthUiState(
    val cargando: Boolean = false,
    val usuario: FirebaseUser? = null,
    val mensajeError: String? = null,
    val mensajeInfo: String? = null
)

