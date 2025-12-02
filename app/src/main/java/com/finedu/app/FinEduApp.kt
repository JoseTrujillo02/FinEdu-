package com.finedu.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.DatadogSite
import com.datadog.android.log.Logs
import com.datadog.android.log.LogsConfiguration
import com.datadog.android.log.Logger
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration
import com.datadog.android.privacy.TrackingConsent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinEduApp : Application() {

    // Logger global de Datadog para usar en toda la app
    lateinit var datadogLogger: Logger
        private set

    override fun onCreate() {
        super.onCreate()
        initDatadog()
        createNotificationChannels()
    }

    /**
     * Inicializa Datadog (Core + RUM + Logs)
     */
    private fun initDatadog() {
        // Consentimiento de tracking (luego puedes hacerlo dinámico si quieres GDPR)
        val trackingConsent = TrackingConsent.GRANTED

        // Configuración core (token, env, site, modo dev)
        val coreConfig = Configuration.Builder(
            clientToken = BuildConfig.DD_CLIENT_TOKEN,
            env = BuildConfig.DD_ENV,
            variant = "android" // puedes poner "android" o "finedu"
        )
            // ⚠️ CAMBIA ESTO AL SITE DE TU ORG:
            // US1, US3, US5, EU1, AP1 o AP2
            .useSite(DatadogSite.US5)
            .setUseDeveloperModeWhenDebuggable(BuildConfig.DEBUG)
            .build()

        Datadog.initialize(
            this,           // context
            coreConfig,     // configuration
            trackingConsent // trackingConsent
        )

        // Logs internos del SDK en Logcat
        Datadog.setVerbosity(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)

        // --- RUM (Real User Monitoring) ---
        val rumConfig = RumConfiguration.Builder(
            applicationId = BuildConfig.DD_RUM_APP_ID
        ).build()

        Rum.enable(rumConfig)

        // --- Logs ---
        val logsConfig = LogsConfiguration.Builder().build()
        Logs.enable(logsConfig)

        datadogLogger = Logger.Builder()
            .setService("finedu-android")      // nombre del servicio en Datadog
            .setNetworkInfoEnabled(true)       // adjunta info de red a los logs
            .setLogcatLogsEnabled(true)        // también muestra en Logcat
            .setRemoteSampleRate(100f)         // 100% de los logs se envían a Datadog
            .setBundleWithRumEnabled(true)     // correlaciona logs con RUM
            .build()
    }

    /**
     * Crea los canales de notificación usados en FinEdu
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val general = NotificationChannel(
                "general",
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de FinEdu"
            }

            val alerts = NotificationChannel(
                "alerts",
                "Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes (presupuestos, recordatorios)"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(general)
            nm.createNotificationChannel(alerts)
        }
    }
}
