package com.boosthabits.data.repository

import android.util.Log
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.UserStatsEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// gestiona divisas y sincronizacion de estadisticas
class EconomiaRepository(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tag = "EconomyRepository"

    fun obtenerEstadisticasUsuario(idUsuario: String) = database.userStatsDao().obtenerEstadisticasUsuario(idUsuario)

    // baja las monedas y gemas desde la nube
    suspend fun sincronizarEstadisticasUsuario(idUsuario: String) {
        val snapshot = firestore.collection("usuarios").document(idUsuario).get().await()
        if (!snapshot.exists()) return

        val gemas = snapshot.getLong("gemas_totales")?.toInt() ?: 0
        val monedas = snapshot.getLong("monedas_totales")?.toInt() ?: 0
        
        val idNombreEquipado = snapshot.getString("id_nombre_equipado")
        val idMarcoAvatarEquipado = snapshot.getString("id_marco_equipado")
        val idFondoPantallaEquipado = snapshot.getString("id_fondo_equipado")
        val idFotoPerfilEquipada = snapshot.getString("id_foto_perfil_equipada")

        database.userStatsDao().upsert(
            UserStatsEntity(
                idUsuario = idUsuario,
                gemas = gemas,
                monedas = monedas,
                idNombreEquipado = idNombreEquipado,
                idMarcoAvatarEquipado = idMarcoAvatarEquipado,
                idFondoPantallaEquipado = idFondoPantallaEquipado,
                idFotoPerfilEquipada = idFotoPerfilEquipada
            )
        )
        database.userStatsDao().eliminarOtrosUsuarios(idUsuario)
        
        sincronizarOfertasCompradas(idUsuario)
    }

    private suspend fun sincronizarOfertasCompradas(idUsuario: String) {
        try {
            val snapshot = firestore.collection("usuarios").document(idUsuario)
                .collection("ofertas_compradas").get().await()
            
            snapshot.documents.forEach { doc ->
                val rewardId = doc.getString("id_oferta") ?: return@forEach
                val title = doc.getString("titulo") ?: "Oferta"
                val code = doc.id
                val cost = doc.getLong("coste")?.toInt() ?: 0
                val timestamp = doc.getTimestamp("comprado_el")?.toDate()?.time ?: System.currentTimeMillis()
                
                database.rewardDao().insertarCupon(
                    com.boosthabits.data.local.entity.CuponEntity(
                        idUsuario = idUsuario,
                        idRecompensa = rewardId,
                        titulo = title,
                        codigo = code,
                        coste = cost,
                        canjeadoEn = timestamp
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "error sincronizando cupones", e)
        }
    }

    suspend fun sincronizarEstadisticasUsuarioSeguro(idUsuario: String) {
        runCatching { sincronizarEstadisticasUsuario(idUsuario) }
            .onFailure { ex -> Log.e(tag, "Error sincronizando economía para $idUsuario", ex) }
    }

    suspend fun sumarMonedasYSincronizar(idUsuario: String, delta: Int) {
        if (delta == 0) return
        database.userStatsDao().sumarMonedas(idUsuario, delta)
        sincronizarLocalAFirebase(idUsuario)
    }

    suspend fun sumarGemasYSincronizar(idUsuario: String, delta: Int) {
        if (delta == 0) return
        database.userStatsDao().sumarGemas(idUsuario, delta)
        sincronizarLocalAFirebase(idUsuario)
    }

    suspend fun guardarDatosUsuario(idUsuario: String) {
        sincronizarLocalAFirebase(idUsuario)
    }

    // sube los datos locales a firestore
    suspend fun sincronizarLocalAFirebase(idUsuario: String) {
        val estadisticasLocales = database.userStatsDao().obtenerEstadisticasUsuarioUnaVez(idUsuario) ?: return
        firestore.collection("usuarios").document(idUsuario)
            .set(
                mapOf(
                    "gemas_totales" to estadisticasLocales.gemas,
                    "monedas_totales" to estadisticasLocales.monedas,
                    "id_nombre_equipado" to estadisticasLocales.idNombreEquipado,
                    "id_marco_equipado" to estadisticasLocales.idMarcoAvatarEquipado,
                    "id_fondo_equipado" to estadisticasLocales.idFondoPantallaEquipado,
                    "id_foto_perfil_equipada" to estadisticasLocales.idFotoPerfilEquipada,
                    "actualizado_el" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun sincronizarLocalAFirebaseSeguro(idUsuario: String) {
        runCatching { sincronizarLocalAFirebase(idUsuario) }
            .onFailure { ex -> Log.e(tag, "Error subiendo economía local para $idUsuario", ex) }
    }
}
