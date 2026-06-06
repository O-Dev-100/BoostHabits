package com.boosthabits.data.local

import androidx.room.TypeConverter
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.data.local.entity.HabitCategory
import com.boosthabits.data.local.entity.RewardItemType
import com.boosthabits.data.local.entity.RecompensaTipo

class Converters {
    @TypeConverter
    fun fromHabitCategory(value: HabitCategory): String {
        return value.name
    }

    @TypeConverter
    fun toHabitCategory(value: String): HabitCategory {
        return HabitCategory.valueOf(value)
    }

    @TypeConverter
    fun fromRewardType(value: RecompensaTipo): String {
        return value.name
    }

    @TypeConverter
    fun toRewardType(value: String): RecompensaTipo {
        return RecompensaTipo.valueOf(value)
    }

    @TypeConverter
    fun fromCurrencyType(value: CurrencyType): String {
        return value.name
    }

    @TypeConverter
    fun toCurrencyType(value: String): CurrencyType {
        return CurrencyType.valueOf(value)
    }

    @TypeConverter
    fun fromRewardItemType(value: RewardItemType): String {
        return value.name
    }

    @TypeConverter
    fun toRewardItemType(value: String): RewardItemType {
        return RewardItemType.valueOf(value)
    }
}