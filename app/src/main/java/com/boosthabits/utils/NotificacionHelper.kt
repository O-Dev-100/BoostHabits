package com.boosthabits.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.boosthabits.R

class NotificacionHelper(private val contexto: Context) {

    private val administradorNotificaciones = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CANAL_PROGRESO = "habit_progress_channel"
        const val CANAL_BIENVENIDA = "welcome_channel"
        const val CANAL_RECORDATORIOS = "reminders_channel"
        
        const val ID_BIENVENIDA = 999
        const val ID_RECORDATORIO = 888
    }

    init {
        crearCanalesNotificacion()
    }

    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // canal para Progreso
            val canalProgreso = NotificationChannel(
                CANAL_PROGRESO,
                contexto.getString(R.string.notification_channel_progress),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = contexto.getString(R.string.notification_channel_progress_desc)
            }
            
            // canal para Bienvenida
            val canalBienvenida = NotificationChannel(
                CANAL_BIENVENIDA,
                contexto.getString(R.string.notification_channel_welcome),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = contexto.getString(R.string.notification_channel_welcome_desc)
            }

            // canal para Recordatorios
            val canalRecordatorio = NotificationChannel(
                CANAL_RECORDATORIOS,
                contexto.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = contexto.getString(R.string.notification_channel_reminders_desc)
            }
            
            administradorNotificaciones.createNotificationChannel(canalProgreso)
            administradorNotificaciones.createNotificationChannel(canalBienvenida)
            administradorNotificaciones.createNotificationChannel(canalRecordatorio)
        }
    }

    fun mostrarNotificacionBienvenida(nombreUsuario: String) {
        val notificacion = NotificationCompat.Builder(contexto, CANAL_BIENVENIDA)
            .setSmallIcon(R.drawable.ic_aplicacion)
            .setContentTitle(contexto.getString(R.string.notification_welcome_title, nombreUsuario))
            .setContentText(contexto.getString(R.string.notification_welcome_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        administradorNotificaciones.notify(ID_BIENVENIDA, notificacion)
    }

    fun mostrarNotificacionProgreso(idHabito: Long, nombreHabito: String, progreso: Int, meta: String) {
        val notificacion = NotificationCompat.Builder(contexto, CANAL_PROGRESO)
            .setSmallIcon(R.drawable.ic_aplicacion)
            .setContentTitle(contexto.getString(R.string.notification_progress_title, nombreHabito))
            .setContentText(contexto.getString(R.string.notification_progress_body, progreso, meta))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, progreso, false)
            .setOngoing(progreso < 100)
            .build()

        administradorNotificaciones.notify(idHabito.toInt(), notificacion)
    }

    fun mostrarNotificacionCompletado(idHabito: Long, nombreHabito: String) {
        val notificacion = NotificationCompat.Builder(contexto, CANAL_PROGRESO)
            .setSmallIcon(R.drawable.ic_aplicacion)
            .setContentTitle(contexto.getString(R.string.notification_completion_title))
            .setContentText(contexto.getString(R.string.notification_completion_body, nombreHabito))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        administradorNotificaciones.notify(idHabito.toInt(), notificacion)
    }

    fun mostrarNotificacionRecordatorio() {
        val notificacion = NotificationCompat.Builder(contexto, CANAL_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_aplicacion)
            .setContentTitle(contexto.getString(R.string.notification_reminder_title))
            .setContentText(contexto.getString(R.string.notification_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        administradorNotificaciones.notify(ID_RECORDATORIO, notificacion)
    }

    fun cancelarNotificacion(idHabito: Long) {
        administradorNotificaciones.cancel(idHabito.toInt())
    }
}
