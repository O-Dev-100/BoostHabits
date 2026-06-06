package com.boosthabits.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boosthabits.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// sube registros de actividad a firestore
class SyncLogWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val logId = inputData.getLong("log_id", -1L)
        if (logId == -1L) return Result.failure()

        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.retry()
        val database = AppDatabase.getDatabase(applicationContext)
        val registro = database.habitLogDao().obtenerRegistroPorId(logId) ?: return Result.failure()

        return try {
            FirebaseFirestore.getInstance()
                .collection("usuarios").document(idUsuario)
                .collection("registros_actividad").document(registro.id.toString())
                .set(registro.copy(estaSincronizado = true))
                .await()
            
            database.habitLogDao().actualizarRegistro(registro.copy(estaSincronizado = true))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
