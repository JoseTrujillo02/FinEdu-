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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.finedu.app.auth.login.LoginScreen
import com.finedu.app.auth.login.LoginViewModel
import com.finedu.app.auth.register.RegisterScreen
import com.finedu.app.auth.register.RegisterViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d("Perms", "Resultados permisos: $results")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }

    private fun ensureRuntimePermissions() {
        val toRequest = mutableListOf<String>()

        toRequest += Manifest.permission.RECORD_AUDIO

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
fun MainContent() {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()

    val loginState by loginViewModel.state.collectAsState()
    val registerState by registerViewModel.state.collectAsState()

    var currentScreen by remember { mutableStateOf("login") }

    when {
        loginState.isSuccess || registerState.isSuccess -> {
            // Pantalla principal después del login/registro exitoso
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Bienvenido a FinEdu! 🎉")
            }
        }
        currentScreen == "login" -> {
            LoginScreen(
                onLoginClick = { email, password ->
                    loginViewModel.login(email, password)
                },
                onRegisterClick = {
                    currentScreen = "register"
                },
                state = loginState,
                onDismissError = {
                    loginViewModel.clearError()
                }
            )
        }
        currentScreen == "register" -> {
            RegisterScreen(
                onRegisterClick = { name, email, password ->
                    registerViewModel.register(name, email, password)
                },
                onLoginClick = {
                    currentScreen = "login"
                },
                state = registerState,
                onDismissError = {
                    registerViewModel.clearError()
                }
            )
        }
    }
}