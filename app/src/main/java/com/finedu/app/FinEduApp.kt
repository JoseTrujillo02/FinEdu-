package com.finedu.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinEduApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val general = NotificationChannel(
                "general",
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notificaciones generales de FinEdu" }

            val alerts = NotificationChannel(
                "alerts",
                "Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alertas importantes (presupuestos, recordatorios)" }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(general)
            nm.createNotificationChannel(alerts)
        }
    }
}
