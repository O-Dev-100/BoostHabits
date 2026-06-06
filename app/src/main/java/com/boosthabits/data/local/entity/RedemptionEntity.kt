package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "canjes")
data class RedemptionEntity(
    @PrimaryKey val id: String,
    val idRecompensa: String,
    val idUsuario: String,
    val fecha: Long,
    val codigoVoucher: String?,
    val estado: String = "pendiente"
)
