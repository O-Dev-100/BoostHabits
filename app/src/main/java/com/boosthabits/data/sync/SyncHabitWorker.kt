package com.boosthabits.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boosthabits.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// sube cambios de habitos a firestore
class SyncHabitWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong("habit_id", -1L)
        if (habitId == -1L) return Result.failure()

        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.retry()
        val db = AppDatabase.getDatabase(applicationContext)
        val habito = db.habitDao().obtenerHabitoPorIdSimple(habitId) ?: return Result.failure()

        return try {
            FirebaseFirestore.getInstance()
                .collection("usuarios").document(idUsuario)
                .collection("habitos").document(habito.id.toString())
                .set(habito.copy(estaSincronizado = true))
                .await()
            
            db.habitDao().actualizarHabito(habito.copy(estaSincronizado = true))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
