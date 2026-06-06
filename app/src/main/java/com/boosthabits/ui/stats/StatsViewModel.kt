package com.boosthabits.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.MutableLiveData
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.dao.ConteoPorDia
import com.boosthabits.data.local.dao.PuntosPorDia
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.repository.HabitoRepository
import com.boosthabits.data.model.LeaderboardUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.random.Random

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitoRepository
    private val db = AppDatabase.getDatabase(application)
    private val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val totalPuntos: LiveData<Int?>
    val rachaMaxima: LiveData<Int>
    val habitosCompletadosHoy: LiveData<Int>
    val totalHabitosActivos: LiveData<Int>

    // datos para Gráficos
    val habitosActivos: LiveData<List<HabitoEntity>>
    val puntosSemanales: LiveData<List<PuntosPorDia>>
    
    // para el Heatmap (Mapa de contribuciones, ContributionMapView)
    val completadosPorDia: LiveData<List<ConteoPorDia>>
    
    // para el ranking (simulado)
    private val _leaderboard = MutableLiveData<List<LeaderboardUser>>()
    val leaderboard: LiveData<List<LeaderboardUser>> = _leaderboard

    // datos para el calendario anual
    val logsAnuales: LiveData<Map<LocalDate, Int>>

    init {
        repository = HabitoRepository(db, application)
        val hoy = LocalDate.now()
        val hoyEpoch = hoy.toEpochDay() //epoch es una marca de tiempo
        val haceUnaSemana = hoyEpoch - 7
        val haceVinteSemanas = hoyEpoch - (20 * 7)
        
        val inicioAnio = hoy.withDayOfYear(1).toEpochDay()
        val finAnio = hoy.withMonth(12).withDayOfMonth(31).toEpochDay()


        totalPuntos = repository.obtenerTotalPuntos().asLiveData()

        // racha máxima global (la mayor racha de todos tus hábitos)
        rachaMaxima = repository.obtenerHabitosActivos().map { lista: List<HabitoEntity> ->
            lista.maxOfOrNull { h: HabitoEntity -> h.rachaActual } ?: 0
        }.asLiveData()

        // hábitos completados hoy
        habitosCompletadosHoy = repository.obtenerCompletadosHoy(hoyEpoch).asLiveData()

        // todos los hábitos activos
        totalHabitosActivos = repository.obtenerHabitosActivos().map { lista: List<HabitoEntity> ->
            lista.size 
        }.asLiveData()

        habitosActivos = repository.obtenerHabitosActivos().asLiveData()

        // puntos de la última semana para el gráfico de líneas
        puntosSemanales = db.habitLogDao().obtenerPuntosPorDia(idUsuario, haceUnaSemana).asLiveData()
        
        // completados por día para el Heatmap (últimas 20 semanas)
        completadosPorDia = db.habitLogDao().obtenerCompletadosPorDia(idUsuario, haceVinteSemanas).asLiveData()
        

        logsAnuales = db.habitLogDao().obtenerRegistrosPorRango(idUsuario, inicioAnio, finAnio).map { lista ->
            lista.associate { 
                LocalDate.ofEpochDay(it.fecha) to it.conteo 
            }
        }.asLiveData()

        generarLeaderboardSimulada()
    }

    private fun generarLeaderboardSimulada() {
        val nombres = listOf("Marcos", "Elena", "Lucas", "Sofia", "Adrian", "Marta", "David", "Lucia", "Javier", "Carmen")
        val banderas = listOf("🇪🇸", "🇲🇽", "🇦🇷", "🇨🇴", "🇨🇱", "🇵🇪", "🇺🇾")
        val pfps = listOf("pfp_agua", "pfp_yoga", "pfp_apple", "pfp_libro", "pfp_salad", "pfp_shoes")
        val frames = listOf("frame_fire", "frame_electric", "frame_flowers", "")
        val effects = listOf("3", "4", "5", "style_bold", "style_italic", "style_bold_italic", "style_monospace", "style_underline", "color_red", "color_blue", "color_green", "color_purple", null)

        val users = nombres.mapIndexed { index, name ->
            LeaderboardUser(
                id = index.toString(),
                rango = index + 1,
                nombreUsuario = name,
                emojiBandera = banderas.random(),
                gemasGastadas = (10 - index) * 100 + Random.nextInt(10, 50),
                nombreRecursoAvatar = pfps.random(),
                nombreRecursoMarcoAvatar = frames.random(),
                idEfectoNombre = effects.random()
            )
        }
        _leaderboard.value = users
    }
}
