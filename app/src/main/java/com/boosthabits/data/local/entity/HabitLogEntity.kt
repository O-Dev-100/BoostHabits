package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros_habitos",
    foreignKeys = [
        ForeignKey(
            entity = HabitoEntity::class,
            parentColumns = ["id"],
            childColumns = ["idHabito"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idHabito")]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idHabito: Long = 0,
    val fechaCompletado: Long = 0,
    val completado: Boolean = true,
    val puntosGanados: Int = 0,
    val estaSincronizado: Boolean = false
)
