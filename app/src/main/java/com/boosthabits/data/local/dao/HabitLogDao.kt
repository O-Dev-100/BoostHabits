package com.boosthabits.data.local.dao

import androidx.room.*
import com.boosthabits.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: HabitLogEntity): Long

    @Update
    suspend fun actualizarRegistro(registro: HabitLogEntity)

    @Query("SELECT * FROM registros_habitos WHERE id = :id")
    suspend fun obtenerRegistroPorId(id: Long): HabitLogEntity?

    @Query("SELECT * FROM registros_habitos WHERE idHabito = :idHabito ORDER BY fechaCompletado DESC")
    fun obtenerRegistrosPorHabito(idHabito: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT fechaCompletado FROM registros_habitos WHERE idHabito = :idHabito ORDER BY fechaCompletado DESC")
    suspend fun obtenerFechasCompletado(idHabito: Long): List<Long>

    @Query("""
        SELECT SUM(l.puntosGanados) 
        FROM registros_habitos l 
        INNER JOIN habitos h ON l.idHabito = h.id 
        WHERE h.idUsuario = :idUsuario
    """)
    fun obtenerTotalPuntos(idUsuario: String): Flow<Int?>

    @Query("""
        SELECT COUNT(l.id) 
        FROM registros_habitos l 
        INNER JOIN habitos h ON l.idHabito = h.id 
        WHERE h.idUsuario = :idUsuario AND l.fechaCompletado = :hoy
    """)
    fun obtenerHabitosCompletadosHoy(idUsuario: String, hoy: Long): Flow<Int>

    @Query("""
        SELECT COUNT(l.id) 
        FROM registros_habitos l 
        INNER JOIN habitos h ON l.idHabito = h.id 
        WHERE h.idUsuario = :idUsuario AND l.fechaCompletado = :hoy AND h.esVerificable = 0
    """)
    fun obtenerConteoRegistrosManualesHoy(idUsuario: String, hoy: Long): Flow<Int>

    @Query("""
        SELECT l.fechaCompletado as fecha, SUM(l.puntosGanados) as puntos
        FROM registros_habitos l
        INNER JOIN habitos h ON l.idHabito = h.id
        WHERE h.idUsuario = :idUsuario AND l.fechaCompletado >= :desde
        GROUP BY l.fechaCompletado
        ORDER BY l.fechaCompletado ASC
    """)
    fun obtenerPuntosPorDia(idUsuario: String, desde: Long): Flow<List<PuntosPorDia>>

    @Query("""
        SELECT l.fechaCompletado as fecha, COUNT(l.id) as conteo
        FROM registros_habitos l
        INNER JOIN habitos h ON l.idHabito = h.id
        WHERE h.idUsuario = :idUsuario AND l.fechaCompletado >= :desde
        GROUP BY l.fechaCompletado
    """)
    fun obtenerCompletadosPorDia(idUsuario: String, desde: Long): Flow<List<ConteoPorDia>>

    @Query("""
        SELECT l.fechaCompletado as fecha, COUNT(l.id) as conteo
        FROM registros_habitos l
        INNER JOIN habitos h ON l.idHabito = h.id
        WHERE h.idUsuario = :idUsuario AND l.fechaCompletado >= :desde AND l.fechaCompletado <= :hasta
        GROUP BY l.fechaCompletado
    """)
    fun obtenerRegistrosPorRango(idUsuario: String, desde: Long, hasta: Long): Flow<List<ConteoPorDia>>
}

data class PuntosPorDia(
    val fecha: Long,
    val puntos: Int
)

data class ConteoPorDia(
    val fecha: Long,
    val conteo: Int
)
