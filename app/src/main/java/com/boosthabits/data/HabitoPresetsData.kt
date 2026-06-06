package com.boosthabits.data

import androidx.health.connect.client.records.ExerciseSessionRecord
import com.boosthabits.R
import com.boosthabits.data.local.entity.HabitCategory as HabitCategoryEnum
import com.boosthabits.data.local.entity.RecompensaTipo

data class HabitoCategoria(
    val id: Int,
    val titulo: Int,
    val icon: String
)

data class HabitoPreset(
    val categoriaId: Int,
    val nombre: Int,
    val icon: String,
    val dificultad: Int,
    val puntosBase: Int,
    val verificable: Boolean = false,
    val healthConnectType: Int? = null,
    val recompensaTipo: RecompensaTipo = RecompensaTipo.MONEDAS,
    val categoriaEnum: HabitCategoryEnum = HabitCategoryEnum.OTROS,
    val timeOptions: List<String>? = null,
    val timeValues: List<Float>? = null
)

object HabitoPresetsData {
    val categorias = listOf(
        HabitoCategoria(1, R.string.category_physical, "🏃"),
        HabitoCategoria(2, R.string.category_productivity, "✍️"),
        HabitoCategoria(3, R.string.category_nutrition, "🍎"),
        HabitoCategoria(4, R.string.category_mental_health, "🧠"),
        HabitoCategoria(5, R.string.category_home, "🏠")
    )

    val presets = listOf(
        // --- Actividad Física (Gemas & Verificables) --- (ID 1)
        HabitoPreset(1, R.string.habit_run, "🏃", 2, 10, true, ExerciseSessionRecord.EXERCISE_TYPE_RUNNING, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_walk, "👟", 1, 5, true, ExerciseSessionRecord.EXERCISE_TYPE_WALKING, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_bike, "🚲", 2, 15, true, ExerciseSessionRecord.EXERCISE_TYPE_BIKING, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_hike, "🥾", 2, 15, true, ExerciseSessionRecord.EXERCISE_TYPE_HIKING, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_elliptical, "🎿", 2, 10, true, ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_soccer, "⚽", 3, 25, true, ExerciseSessionRecord.EXERCISE_TYPE_SOCCER, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_basketball, "🏀", 3, 25, true, ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_volleyball, "🏐", 2, 15, true, ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_tennis, "🎾", 2, 20, true, ExerciseSessionRecord.EXERCISE_TYPE_TENNIS, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_weights, "🏋️", 2, 15, true, ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_yoga, "🧘", 1, 10, true, ExerciseSessionRecord.EXERCISE_TYPE_YOGA, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),
        HabitoPreset(1, R.string.habit_pilates, "🤸", 2, 15, true, ExerciseSessionRecord.EXERCISE_TYPE_PILATES, RecompensaTipo.GEMAS, HabitCategoryEnum.DEPORTE),

        // --- Productividad --- (ID 2)
        HabitoPreset(2, R.string.habit_plan_day, "📅", 1, 40, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.PRODUCTIVIDAD, listOf("12 hr", "24 hr"), listOf(720f, 1440f)),
        HabitoPreset(2, R.string.habit_no_social, "📵", 3, 200, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.PRODUCTIVIDAD, listOf("1:30 hr", "3 hr", "24 hr"), listOf(90f, 180f, 1440f)),
        HabitoPreset(2, R.string.habit_study, "📚", 2, 120, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.PRODUCTIVIDAD, listOf("30 min", "1 hr", "2 hr"), listOf(30f, 60f, 120f)),
        HabitoPreset(2, R.string.habit_deep_work, "💻", 2, 100, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.PRODUCTIVIDAD, listOf("1:30 hr", "3 hr", "6 hr", "8 hr"), listOf(90f, 180f, 360f, 480f)),
        HabitoPreset(2, R.string.habit_clean_email, "📧", 1, 30, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.PRODUCTIVIDAD, listOf("5 min"), listOf(5f)),

        // --- Nutrición --- (ID 3)
        HabitoPreset(3, R.string.habit_fruit, "🍏", 1, 30, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("3 min"), listOf(3f)),
        HabitoPreset(3, R.string.habit_light_dinner, "🥗", 2, 70, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("15 min", "30 min", "45 min"), listOf(15f, 30f, 45f)),
        HabitoPreset(3, R.string.habit_water, "💧", 1, 50, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("2 min"), listOf(2f)),
        HabitoPreset(3, R.string.habit_healthy_breakfast, "🥑", 1, 40, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("15 min", "30 min", "45 min"), listOf(15f, 30f, 45f)),
        HabitoPreset(3, R.string.habit_no_junk, "🚫🍔", 3, 300, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("8 hr", "12 hr", "24 hr"), listOf(480f, 720f, 1440f)),
        HabitoPreset(3, R.string.habit_nuts, "🥜", 1, 30, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("2 min"), listOf(2f)),
        HabitoPreset(3, R.string.habit_less_salt, "🧂", 2, 50, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.NUTRICION, listOf("15 min", "30 min", "45 min"), listOf(15f, 30f, 45f)),

        // --- Salud Mental --- (ID 4)
        HabitoPreset(4, R.string.habit_affirmations, "✨", 1, 30, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("5 min"), listOf(5f)),
        HabitoPreset(4, R.string.habit_gratitude, "🙏", 1, 40, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("5 min"), listOf(5f)),
        HabitoPreset(4, R.string.habit_digital_detox, "📵", 2, 100, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("30 min", "1 hr", "3 hr"), listOf(30f, 60f, 180f)),
        HabitoPreset(4, R.string.habit_meditate, "🧘", 1, 50, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("10 min", "20 min", "30 min"), listOf(10f, 20f, 30f)),
        HabitoPreset(4, R.string.habit_read_10, "📖", 1, 50, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("10 min"), listOf(10f)),
        HabitoPreset(4, R.string.habit_journal, "📔", 1, 50, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("10 min"), listOf(10f)),
        HabitoPreset(4, R.string.habit_podcast, "🎧", 1, 40, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("30 min", "1 hr", "2 hr"), listOf(30f, 60f, 120f)),
        HabitoPreset(4, R.string.habit_puzzle, "🧩", 2, 80, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.SALUD_MENTAL, listOf("10 min", "30 min", "1 hr"), listOf(10f, 30f, 60f)),
        
        // --- Hogar --- (ID 5)
        HabitoPreset(5, R.string.habit_make_bed, "🛏️", 1, 20, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("5 min"), listOf(5f)),
        HabitoPreset(5, R.string.habit_clean, "🧹", 1, 60, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("30 min", "1 hr", "2 hr"), listOf(30f, 60f, 120f)),
        HabitoPreset(5, R.string.habit_organize_clothes, "👕", 1, 40, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("15 min", "30 min", "1 hr"), listOf(15f, 30f, 60f)),
        HabitoPreset(5, R.string.habit_wash_dishes, "🍽️", 1, 30, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("10 min", "20 min", "30 min"), listOf(10f, 20f, 30f)),
        HabitoPreset(5, R.string.habit_water_plants, "🪴", 1, 20, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("5 min"), listOf(5f)),
        HabitoPreset(5, R.string.habit_take_trash, "🗑️", 1, 20, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("5 min"), listOf(5f)),
        HabitoPreset(5, R.string.habit_ventilate, "🪟", 1, 10, false, null, RecompensaTipo.MONEDAS, HabitCategoryEnum.HOGAR, listOf("2 min"), listOf(2f))
    )
}
