package com.boosthabits.data.repository

import android.content.Context
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.HabitCategory
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.HabitLogEntity
import com.boosthabits.data.local.entity.RecompensaTipo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.work.*
import com.boosthabits.data.sync.SyncHabitWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

// gestiona habitos y sincronizacion con la nube
class HabitoRepository(private val database: AppDatabase, private val context: Context) {

    private val habitDao = database.habitDao()
    private val habitLogDao = database.habitLogDao()
    private val dbFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val economiaRepository = EconomiaRepository(database, dbFirestore)
    private val healthRepository = HealthRepository(context)
    val healthConnectManager = healthRepository.obtenerManager()

    fun obtenerHabitosActivos(): Flow<List<HabitoEntity>> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return habitDao.obtenerHabitosActivos(idUsuario)
    }

    fun obtenerHabitosPendientesHoy(): Flow<List<HabitoEntity>> {
        val idUsuario = auth.currentUser?.uid ?: ""
        val hoy = LocalDate.now().toEpochDay()
        return habitDao.obtenerHabitosPendientesHoy(idUsuario, hoy)
    }

    fun obtenerHabitosEnEspera(): Flow<List<HabitoEntity>> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return habitDao.obtenerHabitosEnEspera(idUsuario)
    }

    suspend fun obtenerHabitosEnEsperaUnaVez(idUsuario: String): List<HabitoEntity> {
        return habitDao.obtenerHabitosEnEsperaUnaVez(idUsuario)
    }

    fun obtenerTotalPuntos(): Flow<Int?> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return database.userStatsDao().obtenerEstadisticasUsuario(idUsuario).map { it?.monedas }
    }
    
    fun obtenerCompletadosHoy(hoy: Long): Flow<Int> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return habitLogDao.obtenerHabitosCompletadosHoy(idUsuario, hoy)
    }

    suspend fun obtenerProgresoHabito(habito: HabitoEntity): Int {
        return healthRepository.obtenerProgresoHabito(habito)
    }

    suspend fun insertarHabito(habito: HabitoEntity): Result<Long> {
        val idUsuario = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        
        val habitoExistente = habitDao.obtenerHabitoPorNombre(idUsuario, habito.nombre)
        if (habitoExistente != null) {
            return Result.failure(Exception("Ya tienes un hábito llamado '${habito.nombre}'"))
        }

        val habitoAInsertar = habito.copy(idUsuario = idUsuario, estaSincronizado = false)
        val nuevoId = habitDao.insertarHabito(habitoAInsertar)
        encolarSincronizacion(nuevoId)
        return Result.success(nuevoId)
    }

    suspend fun actualizarHabito(habito: HabitoEntity) {
        habitDao.actualizarHabito(habito.copy(estaSincronizado = false))
        encolarSincronizacion(habito.id)
    }

    fun obtenerHabitos() = obtenerHabitosActivos()

    fun obtenerRegistros(idHabito: Long) = habitLogDao.obtenerRegistrosPorHabito(idHabito)

    suspend fun guardarHabito(habito: HabitoEntity) {
        actualizarHabito(habito)
    }

    suspend fun guardarRegistro(registro: HabitLogEntity) {
        habitLogDao.insertarRegistro(registro)
    }

    // programa la subida del habito a firestore
    private fun encolarSincronizacion(idHabito: Long) {
        val data = Data.Builder().putLong("habit_id", idHabito).build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncHabitWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, java.util.concurrent.TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_habit_$idHabito",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // programa la subida del registro a firestore
    private fun encolarSincronizacionLog(idRegistro: Long) {
        val data = Data.Builder().putLong("log_id", idRegistro).build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<com.boosthabits.data.sync.SyncLogWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, java.util.concurrent.TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_log_$idRegistro",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun eliminarHabito(idHabito: Long) {
        val idUsuario = auth.currentUser?.uid ?: return
        try {
            val habito = habitDao.obtenerHabitoPorId(idHabito, idUsuario) ?: return
            habitDao.eliminarHabito(habito)

            dbFirestore.collection("usuarios").document(idUsuario)
                .collection("habitos").document(idHabito.toString())
                .delete()
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
    }

    // descarga todos los datos del usuario desde la nube
    suspend fun sincronizarTodoDesdeFirestore(idUsuario: String) {
        try {
            val snapshotHabitos = dbFirestore.collection("usuarios").document(idUsuario)
                .collection("habitos").get().await()
            
            snapshotHabitos.toObjects(HabitoEntity::class.java).forEach { habito ->
                habitDao.insertarHabito(habito)
            }

            val snapshotLogs = dbFirestore.collection("usuarios").document(idUsuario)
                .collection("registros_actividad").get().await()
            
            snapshotLogs.toObjects(HabitLogEntity::class.java).forEach { registro ->
                habitLogDao.insertarRegistro(registro)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // completa el habito y otorga recompensas
    suspend fun marcarComoCompletado(idHabito: Long, multiplicador: Float = 1.0f): Result<Int> {
        val hoy = LocalDate.now().toEpochDay()
        val idUsuario = auth.currentUser?.uid ?: return Result.failure(Exception("Sesión expirada"))
        val habito = habitDao.obtenerHabitoPorId(idHabito, idUsuario) ?: return Result.failure(Exception("Hábito no encontrado"))
        
        if (habito.completadoHoy) return Result.success(0)

        val puntosFinales = if (habito.valorRecompensa > 0) habito.valorRecompensa else (habito.obtenerPuntosFinales() * multiplicador).toInt()

        val registro = HabitLogEntity(idHabito = idHabito, fechaCompletado = hoy, puntosGanados = puntosFinales, estaSincronizado = false)
        val idRegistro = habitLogDao.insertarRegistro(registro)
        encolarSincronizacionLog(idRegistro)

        val habitoActualizado = habito.copy(
            rachaActual = habito.rachaActual + 1,
            mejorRacha = if (habito.rachaActual + 1 > habito.mejorRacha) habito.rachaActual + 1 else habito.mejorRacha,
            tiempoInicioEspera = 0L,
            completadoHoy = true
        )
        actualizarHabito(habitoActualizado)

        if (habito.esVerificable || habito.tipoRecompensa == RecompensaTipo.GEMAS) {
            val recompensaGemas = if (habito.valorRecompensa > 0) habito.valorRecompensa else (10 * multiplicador).toInt()
            economiaRepository.sumarGemasYSincronizar(idUsuario, recompensaGemas)
        } else {
            economiaRepository.sumarMonedasYSincronizar(idUsuario, puntosFinales)
        }
        return Result.success(puntosFinales)
    }

    suspend fun obtenerHabitoPorId(id: Long): HabitoEntity? {
        val idUsuarioActual: String = auth.currentUser?.uid ?: return null
        return habitDao.obtenerHabitoPorId(id, idUsuarioActual)
    }

    suspend fun insertarHabitosPredeterminados() {
        val idUsuario = auth.currentUser?.uid ?: return
        if (habitDao.contarHabitos(idUsuario) > 0) return
        val predeterminados = listOf(
            HabitoEntity(idUsuario = idUsuario, nombre = "Caminar 10k pasos", dificultad = 2, icono = "🏃", objetivoDiario = 10000, valorObjetivo = 10000f, categoria = HabitCategory.DEPORTE, tipoRecompensa = RecompensaTipo.GEMAS, esVerificable = true, valorRecompensa = 50),
            HabitoEntity(idUsuario = idUsuario, nombre = "Meditar 10 min", dificultad = 1, icono = "🧘", objetivoDiario = 10, valorObjetivo = 10f, duracionMinutos = 10, categoria = HabitCategory.SALUD_MENTAL, esVerificable = false, valorRecompensa = 50)
        )
        predeterminados.forEach { habitDao.insertarHabito(it) }
    }

    // gestiona el proceso de compra en el mercado
    suspend fun canjearOferta(offerId: String, idUsuario: String): Result<Unit> {
        return try {
            dbFirestore.runTransaction { transaction ->
                val userRef = dbFirestore.collection("usuarios").document(idUsuario)
                val offerRef = dbFirestore.collection("ofertas_mercado").document(offerId)

                val userSnap = transaction.get(userRef)
                val offerSnap = transaction.get(offerRef)

                if (!offerSnap.exists()) throw Exception("Oferta no encontrada")

                val totalGems = userSnap.getLong("gemas_totales") ?: 0L
                val cost = offerSnap.getLong("coste") ?: 0L
                val stock = offerSnap.getLong("stock") ?: 0L

                if (totalGems < cost) throw Exception("Saldo insuficiente")
                if (stock <= 0) throw Exception("Producto agotado")

                transaction.update(userRef, "gemas_totales", totalGems - cost)
                transaction.update(offerRef, "stock", stock - 1)

                val purchasedOfferRef = userRef.collection("ofertas_compradas").document()
                val purchasedData = hashMapOf(
                    "id_oferta" to offerId,
                    "titulo" to offerSnap.getString("titulo"),
                    "comprado_el" to FieldValue.serverTimestamp(),
                    "coste" to cost
                )
                transaction.set(purchasedOfferRef, purchasedData)
            }.await()

            economiaRepository.sincronizarEstadisticasUsuario(idUsuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
