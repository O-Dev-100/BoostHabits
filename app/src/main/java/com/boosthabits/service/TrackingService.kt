package com.boosthabits.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.boosthabits.R
import com.boosthabits.data.health.HealthConnectManager
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.repository.HabitoRepository
import com.boosthabits.utils.NotificacionHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class TrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: HabitoRepository
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var notificacionHelper: NotificacionHelper
    private var trackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = HabitoRepository(database, this)
        healthConnectManager = HealthConnectManager(this)
        notificacionHelper = NotificacionHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarSeguimiento()
        return START_STICKY
    }

    private fun iniciarSeguimiento() {
        if (trackingJob?.isActive == true) return
        
        // Mostrar notificación de primer plano inmediatamente
        mostrarNotificacionInicialPrimerPlano()

        trackingJob = serviceScope.launch {
            val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                stopSelf()
                return@launch
            }

            while (isActive) {
                val habitosEnEspera = repository.obtenerHabitosEnEsperaUnaVez(idUsuario)
                val verificablesEnEspera = habitosEnEspera.filter { it.esVerificable }

                if (verificablesEnEspera.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }

                for (habito in verificablesEnEspera) {
                    val progreso = repository.obtenerProgresoHabito(habito)
                    val nombre = habito.nombre.lowercase()
                    val esPasos = nombre.contains("pasos") || nombre.contains("steps") || 
                                  nombre.contains("caminar") || nombre.contains("walk")
                    
                    val valorRaw = when {
                        esPasos -> {
                            val pasos = healthConnectManager.obtenerPasosHoy()
                            val sufijo = getString(R.string.stats_steps)
                            "${pasos.toInt()} / ${habito.valorObjetivo.toInt()} $sufijo"
                        }
                        habito.tipoHealthConnect != null -> {
                            val duracion = healthConnectManager.obtenerDuracionEjercicioMinutosPorTipo(habito.tipoHealthConnect!!)
                            "${duracion.toInt()} / ${habito.valorObjetivo.toInt()} min"
                        }
                        else -> "${habito.valorActual.toInt()} / ${habito.valorObjetivo.toInt()}"
                    }

                    // Actualizar si hay cambios
                    if (progreso.toFloat() != habito.valorActual) {
                        repository.actualizarHabito(habito.copy(valorActual = progreso.toFloat()))
                        
                        if (progreso >= 100) {
                            notificacionHelper.mostrarNotificacionCompletado(habito.id, habito.nombre)
                            repository.marcarComoCompletado(habito.id)
                        } else {
                            notificacionHelper.mostrarNotificacionProgreso(habito.id, habito.nombre, progreso, valorRaw)
                        }
                    }
                }
                delay(30000) // Verificar cada 30 segundos
            }
        }
    }

    private fun mostrarNotificacionInicialPrimerPlano() {
        val notification = NotificationCompat.Builder(this, NotificacionHelper.CANAL_PROGRESO)
            .setSmallIcon(R.drawable.ic_aplicacion)
            .setContentTitle("BoostHabits")
            .setContentText("Sincronizando actividad física en segundo plano...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(1001, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
