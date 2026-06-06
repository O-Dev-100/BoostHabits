package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cosmeticos_desbloqueados")
data class PersonalizacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idUsuario: String,
    val idRecompensa: String,
    val desbloqueadoEn: Long = System.currentTimeMillis()
)
