package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estadisticas_usuario")
data class UserStatsEntity(
    @PrimaryKey val idUsuario: String,
    val gemas: Int,
    val monedas: Int,
    val idNombreEquipado: String? = null,
    val idMarcoAvatarEquipado: String? = null,
    val idFondoPantallaEquipado: String? = null,
    val idFotoPerfilEquipada: String? = null
)
