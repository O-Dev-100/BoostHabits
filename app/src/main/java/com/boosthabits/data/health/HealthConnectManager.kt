package com.boosthabits.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime
import java.time.Duration

class HealthConnectManager(private val contexto: Context) {

    private val clienteHealthConnect by lazy {
        if (estaHealthConnectDisponible()) {
            HealthConnectClient.getOrCreate(contexto)
        } else {
            null
        }
    }

    val permisos = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    fun estaHealthConnectDisponible(): Boolean {
        return HealthConnectClient.getSdkStatus(contexto) == HealthConnectClient.SDK_AVAILABLE
    }

    fun obtenerIntentDeInstalacion(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun obtenerIntentDeInstalacionGoogleFit(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.fitness")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun estaGoogleFitInstalado(): Boolean {
        return try {
            contexto.packageManager.getPackageInfo("com.google.android.apps.fitness", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun obtenerIntentDeAjustesHealthConnect(): Intent {
        return Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    suspend fun tieneTodosLosPermisos(): Boolean {
        return tienePermisos(permisos)
    }

    suspend fun tienePermisos(permisosAComprobar: Set<String>): Boolean {
        val concedidos = clienteHealthConnect?.permissionController?.getGrantedPermissions() ?: emptySet()
        return concedidos.containsAll(permisosAComprobar)
    }

    suspend fun obtenerPermisosRequeridosParaHabito(habito: com.boosthabits.data.local.entity.HabitoEntity): Set<String> {
        val requeridos = mutableSetOf<String>()
        if (habito.nombre.contains("pasos", ignoreCase = true) || habito.nombre.contains("Caminar", ignoreCase = true)) {
            requeridos.add(HealthPermission.getReadPermission(StepsRecord::class))
        }
        if (habito.tipoHealthConnect != null) {
            requeridos.add(HealthPermission.getReadPermission(ExerciseSessionRecord::class))
            requeridos.add(HealthPermission.getReadPermission(DistanceRecord::class))
            requeridos.add(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
        }
        return requeridos
    }


    suspend fun obtenerPermisosConcedidos(): Set<String> {
        return clienteHealthConnect?.permissionController?.getGrantedPermissions() ?: emptySet()
    }

    suspend fun verificarPasos(pasosMinimos: Int): Boolean {
        return obtenerPasosHoy() >= pasosMinimos.toLong()
    }

    suspend fun verificarEjercicioPorTipo(tipo: Int): Boolean {
        val inicioDia = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val ahora = Instant.now()
        val sesiones = obtenerDatosDeActividad(inicioDia, ahora)
        return sesiones.any { it.exerciseType == tipo }
    }

    suspend fun obtenerDuracionEjercicioMinutosPorTipo(tipo: Int): Long {
        val inicioDia = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val ahora = Instant.now()
        val sesiones = obtenerDatosDeActividad(inicioDia, ahora)
        val sesionesFiltradas = sesiones.filter { it.exerciseType == tipo }
        return sesionesFiltradas.sumOf { 
            Duration.between(it.startTime, it.endTime).toMinutes()
        }
    }

    suspend fun obtenerDatosDeActividad(inicio: Instant, fin: Instant): List<ExerciseSessionRecord> {
        val cliente = clienteHealthConnect ?: return emptyList()

        return try {
            val respuesta = cliente.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(inicio, fin)
                )
            )
            respuesta.records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerPasosHoy(): Long {
        val cliente = clienteHealthConnect ?: return 0

        val comienzoDia = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val ahora = Instant.now()

        return try {
            val respuesta = cliente.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(comienzoDia, ahora)
                )
            )
            respuesta.records.sumOf { it.count }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun obtenerDistanciaHoy(): Double {
        val cliente = clienteHealthConnect ?: return 0.0
        if (!tieneTodosLosPermisos()) return 0.0

        val comienzoDia = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val ahora = Instant.now()

        return try {
            val respuesta = cliente.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(comienzoDia, ahora)
                )
            )
            respuesta.records.sumOf { it.distance.inMeters }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun obtenerCaloriasHoy(): Double {
        val cliente = clienteHealthConnect ?: return 0.0
        if (!tieneTodosLosPermisos()) return 0.0

        val comienzoDia = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
        val ahora = Instant.now()

        return try {
            val respuesta = cliente.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(comienzoDia, ahora)
                )
            )
            respuesta.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun leerPasos() = obtenerPasosHoy()
    suspend fun leerMetricas() = mapOf(
        "pasos" to obtenerPasosHoy(),
        "distancia" to obtenerDistanciaHoy(),
        "calorias" to obtenerCaloriasHoy()
    )
    fun solicitarPermisos() = permisos
}
