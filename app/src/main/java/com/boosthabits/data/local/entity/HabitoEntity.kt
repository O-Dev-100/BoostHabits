package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitos")
data class HabitoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idUsuario: String = "",
    val nombre: String = "",
    val dificultad: Int = 1,
    val icono: String = "",
    val objetivoDiario: Int = 0,
    val duracionMinutos: Int = 0,
    val esVerificable: Boolean = false,
    val tipoHealthConnect: Int? = null,
    val categoria: HabitCategory = HabitCategory.OTROS,
    val tipoRecompensa: RecompensaTipo = RecompensaTipo.MONEDAS,
    val rachaActual: Int = 0,
    val mejorRacha: Int = 0,
    val estaCongelado: Boolean = false,
    val tiempoInicioEspera: Long = 0,
    val activo: Boolean = true,
    val creadoEn: Long = System.currentTimeMillis(),
    val valorObjetivo: Float = 0f,
    val valorActual: Float = 0f,
    val completadoHoy: Boolean = false,
    val estaSincronizado: Boolean = true,
    val valorRecompensa: Int = 0
) {
    fun calcularPuntosBase(): Int {
        if (valorRecompensa > 0) return valorRecompensa
        return when (dificultad) {
            1 -> 50
            2 -> 100
            3 -> 200
            else -> 50
        }
    }

    fun tieneMultiplicadorEspecial(): Boolean {
        return categoria == HabitCategory.DEPORTE || esVerificable || 
               nombre.contains("Correr", ignoreCase = true) || 
               nombre.contains("pasos", ignoreCase = true)
    }

    fun obtenerPuntosFinales(): Int {
        if (valorRecompensa > 0) return valorRecompensa
        val base = calcularPuntosBase()
        return if (tieneMultiplicadorEspecial()) base * 7 else base
    }
}
