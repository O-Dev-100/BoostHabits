package com.boosthabits.data.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.repository.HabitoRepository

class HealthSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val bd = AppDatabase.getDatabase(applicationContext)
        val repository = HabitoRepository(bd, applicationContext)
        val healthConnectManager = HealthConnectManager(applicationContext)

        if (!healthConnectManager.tieneTodosLosPermisos()) {
            return Result.retry()
        }

        val idUsuario = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val habitos = bd.habitDao().obtenerHabitosVerificablesPendientes(idUsuario)

        val pasos = healthConnectManager.obtenerPasosHoy()
        val distancia = healthConnectManager.obtenerDistanciaHoy()
        val calorias = healthConnectManager.obtenerCaloriasHoy()

        for (habito in habitos) {
            var nuevoValor = 0f
            
            // 1. verificar por tipo de ejercicio específico (ej. fútbol)
            val tipo = habito.tipoHealthConnect
            if (tipo != null) {
                nuevoValor = healthConnectManager.obtenerDuracionEjercicioMinutosPorTipo(tipo).toFloat()
            } 
            // 2. verificar por métricas generales (si no es un tipo de ejercicio específico)
            else {
                when {
                    habito.nombre.contains("pasos", ignoreCase = true) -> nuevoValor = pasos.toFloat()
                    habito.nombre.contains("m", ignoreCase = true) || habito.nombre.contains("distancia", ignoreCase = true) -> nuevoValor = distancia.toFloat()
                    habito.nombre.contains("calorías", ignoreCase = true) || habito.nombre.contains("kcal", ignoreCase = true) -> nuevoValor = calorias.toFloat()
                }
            }

            if (nuevoValor != habito.valorActual) {
                val habitoActualizado = habito.copy(valorActual = nuevoValor)
                
                if (habitoActualizado.valorActual >= habitoActualizado.valorObjetivo && habitoActualizado.valorObjetivo > 0) {
                    repository.marcarComoCompletado(habito.id)
                } else {
                    repository.actualizarHabito(habitoActualizado)
                }
            }}

        return Result.success()
    }
}
