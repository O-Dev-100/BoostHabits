package com.boosthabits.data.repository

import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.HabitLogEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class HabitLogRepository(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val logDao = db.habitLogDao()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun insertarLog(log: HabitLogEntity) {
        logDao.insertarRegistro(log)

        // sincronización local-first → firestore
        if (userId.isNotEmpty()) {
            firestore.collection("userHabits")
                .document(userId)
                .collection("logs")
                .document(log.fechaCompletado.toString())
                .set(log)
        }
    }

    fun getLogsByHabit(habitId: Long): Flow<List<HabitLogEntity>> {
        return logDao.obtenerRegistrosPorHabito(habitId)
    }

    suspend fun getLogHoy(habitId: Long, hoy: Long): HabitLogEntity? {
        return db.habitDao().obtenerRegistroHoy(habitId, hoy)
    }
}