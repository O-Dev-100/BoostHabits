package com.boosthabits.data.local.dao

import androidx.room.*
import com.boosthabits.data.local.entity.RachaEntity

@Dao
interface RachaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(racha: RachaEntity)

    @Query("SELECT * FROM rachas WHERE idHabito = :idHabito")
    suspend fun obtenerRachaPorHabito(idHabito: Long): RachaEntity?

    @Query("SELECT SUM(mejorRacha) FROM rachas")
    suspend fun obtenerSumaMejoresRachas(): Int?
}
