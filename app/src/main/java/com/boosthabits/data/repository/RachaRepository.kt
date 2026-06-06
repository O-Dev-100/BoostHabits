package com.boosthabits.data.repository

import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.RachaEntity
import com.google.firebase.firestore.FirebaseFirestore

class RachaRepository(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val streakDao = db.streakDao()
    private val idUsuario = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun actualizarRacha(
        idHabito: Long,
        nuevaRacha: Int,
        mejorRacha: Int,
        ultimaFecha: Long
    ) {
        val racha = RachaEntity(
            idHabito = idHabito,
            rachaActual = nuevaRacha,
            mejorRacha = mejorRacha,
            ultimaFecha = ultimaFecha
        )
        streakDao.insertarOActualizar(racha)

        if (idUsuario.isNotEmpty()) {
            firestore.collection("habitos_usuarios")
                .document(idUsuario)
                .collection("rachas")
                .document(idHabito.toString())
                .set(racha)
        }
    }
}
