package com.boosthabits.data.repository

import android.content.Context
import com.boosthabits.data.health.HealthConnectManager
import com.boosthabits.data.local.entity.HabitoEntity

class HealthRepository(context: Context) {
    private val healthConnectManager = HealthConnectManager(context)

    fun obtenerManager() = healthConnectManager

    suspend fun obtenerProgresoHabito(habito: HabitoEntity): Int {
        if (!habito.esVerificable) return 0
        return try {
            val nombre = habito.nombre.lowercase()
            val esPasos = nombre.contains("pasos") || nombre.contains("steps") || 
                          nombre.contains("caminar") || nombre.contains("walk")
            
            val progreso = when {
                esPasos -> {
                    val pasos = healthConnectManager.obtenerPasosHoy()
                    if (habito.valorObjetivo > 0) (pasos.toFloat() / habito.valorObjetivo * 100).toInt() else 0
                }
                habito.tipoHealthConnect != null -> {
                    val duracion = healthConnectManager.obtenerDuracionEjercicioMinutosPorTipo(habito.tipoHealthConnect)
                    if (habito.valorObjetivo > 0) (duracion.toFloat() / habito.valorObjetivo * 100).toInt() else 0
                }
                else -> {
                    if (habito.valorObjetivo > 0) (habito.valorActual / habito.valorObjetivo * 100).toInt() else 0
                }
            }
            progreso.coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    suspend fun leerPasos() = healthConnectManager.obtenerPasosHoy()
    suspend fun leerCalorias() = healthConnectManager.obtenerCaloriasHoy()
    suspend fun sincronizarDatosDiarios() { /* Implementación conceptual */ }
}
