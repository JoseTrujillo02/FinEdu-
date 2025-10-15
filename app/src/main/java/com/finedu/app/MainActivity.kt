package com.finedu.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    // Launcher para pedir permisos en runtime
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d("Perms", "Resultados permisos: $results")
        // Aquí podrías reaccionar si alguno fue denegado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializar Firebase (requiere google-services.json en app/)
        FirebaseApp.initializeApp(this)

        // ✅ Token de FCM (solo log)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "📩 Token FCM: $token")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ Error al obtener token FCM", e)
            }

        // ✅ Pedir permisos en runtime donde aplique
        ensureRuntimePermissions()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FirebaseTestScreen(
                        onTestFirebase = { testFirebaseConnection() },
                        onFetchFcm = { fetchFcmToken() },
                        onNotify = { showTestNotification(this) }
                    )
                }
            }
        }
    }

    private fun ensureRuntimePermissions() {
        val toRequest = mutableListOf<String>()

        // Micrófono en todas las versiones (si lo usarás)
        toRequest += Manifest.permission.RECORD_AUDIO

        // Notificaciones solo en Android 13+ (API 33)
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

@Composable
fun FirebaseTestScreen(
    onTestFirebase: () -> Unit,
    onFetchFcm: () -> Unit,
    onNotify: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔥 Pruebas FinEdu — Firebase + Notificaciones")

        Button(onClick = onTestFirebase) {
            Text("Probar Firebase (Auth + Firestore)")
        }

        Button(onClick = onFetchFcm) {
            Text("Obtener Token FCM")
        }

        Button(onClick = onNotify) {
            Text("Notificación de prueba")
        }
    }
}

// 🧩 Prueba (Auth + Firestore)
fun testFirebaseConnection() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.signInAnonymously()
        .addOnSuccessListener { result ->
            val uid = result.user?.uid
            Log.d("FirebaseAuth", "✅ Usuario anónimo autenticado: $uid")

            val data = mapOf(
                "mensaje" to "Hola desde FinEdu App 👋",
                "timestamp" to System.currentTimeMillis(),
                "usuario" to uid
            )

            db.collection("pruebas").add(data)
                .addOnSuccessListener { docRef ->
                    Log.d("Firestore", "✅ Documento agregado con ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ Error al agregar documento", e)
                }
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseAuth", "❌ Error al autenticar usuario anónimo", e)
        }
}

// 📩 Token de FCM manual
fun fetchFcmToken() {
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            Log.d("FCM", "📩 Token obtenido manualmente: $token")
        }
        .addOnFailureListener { e ->
            Log.e("FCM", "❌ Error al obtener token manualmente", e)
        }
}
