package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rachas")
data class RachaEntity(
    @PrimaryKey val idHabito: Long,
    val rachaActual: Int = 0,
    val mejorRacha: Int = 0,
    val ultimaFecha: Long = 0
)
