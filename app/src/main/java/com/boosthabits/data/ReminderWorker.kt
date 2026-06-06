package com.boosthabits.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.utils.NotificacionHelper
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.failure()
        
        val db = AppDatabase.getDatabase(applicationContext)
        val hoy = LocalDate.now().toEpochDay()
        
        // comprueba si hay hábitos pendientes hoy

        val habitosPendientes = db.habitDao().obtenerHabitosPendientesHoyUnaVez(userId, hoy)
        
        if (habitosPendientes.isNotEmpty()) {
            val notificacionHelper = NotificacionHelper(applicationContext)
            notificacionHelper.mostrarNotificacionRecordatorio()
        }
        
        return Result.success()
    }
}