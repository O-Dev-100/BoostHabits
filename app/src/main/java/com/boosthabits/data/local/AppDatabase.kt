package com.boosthabits.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boosthabits.data.local.dao.HabitoDao
import com.boosthabits.data.local.dao.HabitLogDao
import com.boosthabits.data.local.dao.RewardDao
import com.boosthabits.data.local.dao.RachaDao
import com.boosthabits.data.local.dao.UserStatsDao
import com.boosthabits.data.local.entity.*

@Database(
    entities = [
        HabitoEntity::class,
        HabitLogEntity::class,
        RachaEntity::class,
        UserStatsEntity::class,
        RecompensaEntity::class,
        RedemptionEntity::class,
        CuponEntity::class,
        PersonalizacionEntity::class
    ],
    version = 19,
    exportSchema = false
)
@TypeConverters(Converters::class)

// la base de datos local de room
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitoDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun streakDao(): RachaDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "boosthabits_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
