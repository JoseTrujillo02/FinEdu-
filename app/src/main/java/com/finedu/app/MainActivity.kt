package com.finedu.app



// Imports de Android y Actividad

import android.Manifest

import android.content.pm.PackageManager

import android.os.Build

import android.os.Bundle

import android.util.Log

import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

import androidx.core.content.ContextCompat



// Imports de UI (Compose)

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface

import androidx.compose.ui.Modifier



// Imports de Navegación (¡el importante!)

import com.finedu.app.navigation.AppNavegacion
import com.finedu.app.ui.theme.AppTheme


// Imports de Firebase

import com.google.firebase.FirebaseApp

import com.google.firebase.messaging.FirebaseMessaging



// Import de Hilt

import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint

class MainActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d("Perms", "Resultados permisos: $results")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "📩 Token FCM: $token")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ Error al obtener token FCM", e)
            }
        ensureRuntimePermissions()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavegacion()
                }
            }
        }
    }

    private fun ensureRuntimePermissions() {
        val toRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            toRequest += Manifest.permission.POST_NOTIFICATIONS
        }



        val needed = toRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            requestPermissions.launch(needed.toTypedArray())
        }
    }

}



