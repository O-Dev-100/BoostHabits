package com.boosthabits.data.local.dao

import androidx.room.*
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.data.local.entity.CuponEntity
import com.boosthabits.data.local.entity.PersonalizacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM recompensas WHERE estaActivo = 1")
    fun obtenerTodasLasRecompensas(): Flow<List<RecompensaEntity>>

    @Query("""
        SELECT * FROM recompensas 
        WHERE tipoMoneda = :tipoMoneda 
        AND estaActivo = 1
        AND id NOT IN (SELECT idRecompensa FROM cosmeticos_desbloqueados WHERE idUsuario = :idUsuario)
        AND id NOT IN (SELECT idRecompensa FROM cupones_usuario WHERE idUsuario = :idUsuario)
    """)
    fun obtenerRecompensasDisponiblesPorMoneda(tipoMoneda: CurrencyType, idUsuario: String): Flow<List<RecompensaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRecompensas(recompensas: List<RecompensaEntity>)

    @Insert
    suspend fun insertarCupon(cupon: CuponEntity)

    @Insert
    suspend fun insertarCosmeticoDesbloqueado(cosmetico: PersonalizacionEntity)
    
    @Query("SELECT * FROM cupones_usuario WHERE idUsuario = :idUsuario")
    fun obtenerCuponesUsuario(idUsuario: String): Flow<List<CuponEntity>>

    @Query("SELECT * FROM cosmeticos_desbloqueados WHERE idUsuario = :idUsuario")
    fun obtenerCosmeticosDesbloqueados(idUsuario: String): Flow<List<PersonalizacionEntity>>

    @Query("""
        SELECT r.* FROM recompensas r
        INNER JOIN cosmeticos_desbloqueados u ON u.idRecompensa = r.id
        WHERE u.idUsuario = :idUsuario
    """)
    fun obtenerRecompensasDesbloqueadas(idUsuario: String): Flow<List<RecompensaEntity>>
}
