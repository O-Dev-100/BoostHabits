package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cupones_usuario")
data class CuponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idUsuario: String,
    val idRecompensa: String,
    val titulo: String,
    val codigo: String,
    val coste: Int = 0,
    val canjeadoEn: Long = System.currentTimeMillis()
)
