package com.finedu.app

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun showTestNotification(context: Context) {
    val channelId = "general" // debe existir (lo creamos en FinEduApp)
    val notificationId = 1001

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher) // usa tu icono
        .setContentTitle("FinEdu — Notificación de prueba")
        .setContentText("Esto es una notificación de prueba del canal 'general'.")
        .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
        .setAutoCancel(true)
        .build()

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, notification)
    }
}
