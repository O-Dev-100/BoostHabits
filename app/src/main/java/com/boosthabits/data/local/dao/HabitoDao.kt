package com.boosthabits.data.local.dao

import androidx.room.*
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitoDao {

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1")
    fun obtenerHabitosActivos(idUsuario: String): Flow<List<HabitoEntity>>

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1")
    suspend fun obtenerHabitosActivosUnaVez(idUsuario: String): List<HabitoEntity>

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1 AND esVerificable = 1 AND completadoHoy = 0")
    suspend fun obtenerHabitosVerificablesPendientes(idUsuario: String): List<HabitoEntity>

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1 AND tiempoInicioEspera = 0")
    fun obtenerHabitosActivosDisponibles(idUsuario: String): Flow<List<HabitoEntity>>

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1 AND tiempoInicioEspera > 0")
    fun obtenerHabitosEnEspera(idUsuario: String): Flow<List<HabitoEntity>>

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND activo = 1 AND tiempoInicioEspera > 0")
    suspend fun obtenerHabitosEnEsperaUnaVez(idUsuario: String): List<HabitoEntity>

    @Query("""
        SELECT h.* FROM habitos h 
        LEFT JOIN registros_habitos l ON h.id = l.idHabito AND l.fechaCompletado = :hoy 
        WHERE h.idUsuario = :idUsuario AND h.activo = 1 AND l.id IS NULL AND h.tiempoInicioEspera = 0
    """)
    fun obtenerHabitosPendientesHoy(idUsuario: String, hoy: Long): Flow<List<HabitoEntity>>

    @Query("""
        SELECT h.* FROM habitos h 
        LEFT JOIN registros_habitos l ON h.id = l.idHabito AND l.fechaCompletado = :hoy 
        WHERE h.idUsuario = :idUsuario AND h.activo = 1 AND l.id IS NULL AND h.tiempoInicioEspera = 0
    """)
    suspend fun obtenerHabitosPendientesHoyUnaVez(idUsuario: String, hoy: Long): List<HabitoEntity>

    @Query("SELECT * FROM habitos WHERE id = :id AND idUsuario = :idUsuario")
    suspend fun obtenerHabitoPorId(id: Long, idUsuario: String): HabitoEntity?

    @Query("SELECT * FROM habitos WHERE id = :id")
    suspend fun obtenerHabitoPorIdSimple(id: Long): HabitoEntity?

    @Query("SELECT * FROM habitos WHERE idUsuario = :idUsuario AND nombre = :nombre AND activo = 1 LIMIT 1")
    suspend fun obtenerHabitoPorNombre(idUsuario: String, nombre: String): HabitoEntity?

    @Query("SELECT COUNT(*) FROM habitos WHERE idUsuario = :idUsuario")
    suspend fun contarHabitos(idUsuario: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHabito(habito: HabitoEntity): Long

    @Update
    suspend fun actualizarHabito(habito: HabitoEntity)

    @Delete
    suspend fun eliminarHabito(habito: HabitoEntity)

    @Query("SELECT * FROM registros_habitos WHERE idHabito = :idHabito AND fechaCompletado = :hoy")
    suspend fun obtenerRegistroHoy(idHabito: Long, hoy: Long): HabitLogEntity?
}
