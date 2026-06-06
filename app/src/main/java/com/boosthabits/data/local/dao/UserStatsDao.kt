package com.boosthabits.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.boosthabits.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(estadisticasUsuario: UserStatsEntity)

    @Query("SELECT * FROM estadisticas_usuario WHERE idUsuario = :idUsuario LIMIT 1")
    fun obtenerEstadisticasUsuario(idUsuario: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM estadisticas_usuario WHERE idUsuario = :idUsuario LIMIT 1")
    suspend fun obtenerEstadisticasUsuarioUnaVez(idUsuario: String): UserStatsEntity?

    @Query("UPDATE estadisticas_usuario SET monedas = monedas + :delta WHERE idUsuario = :idUsuario")
    suspend fun incrementarMonedas(idUsuario: String, delta: Int): Int

    @Query("UPDATE estadisticas_usuario SET gemas = gemas + :delta WHERE idUsuario = :idUsuario")
    suspend fun incrementarGemas(idUsuario: String, delta: Int): Int

    @Query("UPDATE estadisticas_usuario SET idNombreEquipado = :idNombre WHERE idUsuario = :idUsuario")
    suspend fun equiparNombre(idUsuario: String, idNombre: String?)

    @Query("UPDATE estadisticas_usuario SET idMarcoAvatarEquipado = :idMarco WHERE idUsuario = :idUsuario")
    suspend fun equiparMarcoAvatar(idUsuario: String, idMarco: String?)

    @Query("UPDATE estadisticas_usuario SET idFondoPantallaEquipado = :idFondo WHERE idUsuario = :idUsuario")
    suspend fun equiparFondoPantalla(idUsuario: String, idFondo: String?)

    @Query("UPDATE estadisticas_usuario SET idFotoPerfilEquipada = :idFoto WHERE idUsuario = :idUsuario")
    suspend fun equiparFotoPerfil(idUsuario: String, idFoto: String?)

    @Transaction
    suspend fun sumarMonedas(idUsuario: String, delta: Int) {
        val filasActualizadas = incrementarMonedas(idUsuario, delta)
        if (filasActualizadas == 0) upsert(UserStatsEntity(idUsuario = idUsuario, gemas = 0, monedas = delta))
    }

    @Transaction
    suspend fun sumarGemas(idUsuario: String, delta: Int) {
        val filasActualizadas = incrementarGemas(idUsuario, delta)
        if (filasActualizadas == 0) upsert(UserStatsEntity(idUsuario = idUsuario, gemas = delta, monedas = 0))
    }

    @Query("DELETE FROM estadisticas_usuario WHERE idUsuario != :idUsuario")
    suspend fun eliminarOtrosUsuarios(idUsuario: String)
}
