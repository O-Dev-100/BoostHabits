package com.boosthabits.data.model

data class LeaderboardUser(
    val id: String,
    val rango: Int,
    val nombreUsuario: String,
    val emojiBandera: String,
    val gemasGastadas: Int,
    val nombreRecursoAvatar: String? = null,
    val nombreRecursoMarcoAvatar: String = "",
    val idEfectoNombre: String? = null
)
