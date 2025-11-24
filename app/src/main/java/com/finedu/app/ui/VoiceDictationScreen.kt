package com.finedu.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.finedu.app.R
import com.finedu.app.ui.dictation.UiEvent
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import kotlinx.coroutines.delay

// --- MODELOS DE UI LOCALES ---
data class AlertState(
    val show: Boolean = false,
    val message: String = "",
    val type: AlertType = AlertType.SUCCESS,
    val showExtraInfo: Boolean = false
)

enum class AlertType {
    SUCCESS, ERROR, INFO, WARNING
}

// --- COLORES ---
private val FineduGreen = Color(0xFF66BB6A)
private val FineduDarkGreen = Color(0xFF4CAF50)
private val FineduRed = Color(0xFFEF5350)
private val FineduBlue = Color(0xFF2196F3)
private val FineduOrange = Color(0xFFFF9800)
private val DarkGray = Color(0xFF2C3E50)
private val MediumGray = Color(0xFF4A5568)
private val LightGray = Color(0xFF9CA3AF)

@Composable
fun VoiceDictationScreen(
    navController: NavController,
    viewModel: VoiceDictationViewModel
) {
    val state by viewModel.state.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    var alertState by remember { mutableStateOf(AlertState()) }
    val context = LocalContext.current

    // Manejo de eventos que vienen del ViewModel (Respuestas de la API)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    alertState = AlertState(
                        show = true,
                        message = event.message,
                        type = AlertType.SUCCESS
                    )
                    delay(2000)
                    // Dejar recados para MainScreen
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("show_success_snackbar", event.message)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                    // Regresar
                    navController.popBackStack()
                }
                is UiEvent.Error -> {
                    // Clasificación de errores para mostrar iconos/colores distintos
                    val (type, message) = when {
                        event.message.contains("fondos insuficientes", ignoreCase = true) ||
                                event.message.contains("no tienes suficiente", ignoreCase = true) ||
                                event.message.contains("saldo insuficiente", ignoreCase = true) -> {
                            AlertType.WARNING to "No tienes suficiente dinero disponible. Verifica tu capital."
                        }
                        event.message.contains("capital", ignoreCase = true) &&
                                event.message.contains("configurado", ignoreCase = true) -> {
                            AlertType.WARNING to "Debes configurar tu capital inicial en tu perfil."
                        }
                        else -> AlertType.ERROR to event.message
                    }

                    alertState = AlertState(
                        show = true,
                        message = message,
                        type = type
                    )
                    // Ocultar alerta después de 5 segundos
                    delay(5000)
                    alertState = alertState.copy(show = false)
                }
            }
        }
    }

    // 1. LAUNCHER DE RECONOCIMIENTO DE VOZ
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!matches.isNullOrEmpty()) {
                val newText = matches[0]
                recognizedText = newText
                // Auto-envío si hay texto
                if (newText.isNotBlank()) {
                    viewModel.sendMessage(newText)
                }
            }
        } else {
            // Si el usuario cancela o hay error en el reconocimiento
            // Opcional: alertState = AlertState(show = true, message = "Cancelado", type = AlertType.INFO)
        }
    }

    // Función interna para construir y lanzar el Intent de voz
    val launchDictationIntent = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora…")
            // Espera 5 segundos de silencio antes de terminar automáticamente
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            speechRecognizerLauncher.launch(intent)
        } else {
            alertState = AlertState(
                show = true,
                message = "El dispositivo no soporta el reconocimiento de voz",
                type = AlertType.ERROR
            )
        }
    }

    // 2. LAUNCHER DE PERMISOS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el usuario da permiso, lanzamos el dictado inmediatamente
            launchDictationIntent()
        } else {
            // Si deniega, mostramos alerta
            alertState = AlertState(
                show = true,
                message = "Se requiere permiso de micrófono para escuchar tu gasto.",
                type = AlertType.WARNING
            )
        }
    }

    // 3. FUNCIÓN DEL BOTÓN (Manejador de Click)
    val handleMicrophoneClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            // Ya tiene permiso -> Dictar
            launchDictationIntent()
        } else {
            // No tiene permiso -> Pedir
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // --- INTERFAZ DE USUARIO ---
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay Oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Text(
                    text = "Dictado por Voz",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance visual
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta Principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (state.isLoading) "Procesando tu solicitud..." else "Toca el micrófono para dictar",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkGray,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // BOTÓN DE MICRÓFONO (Usa la nueva lógica handleMicrophoneClick)
                        MicrophoneButton(
                            isRecording = state.isLoading,
                            onClick = handleMicrophoneClick, // <-- AQUÍ SE LLAMA
                            enabled = !state.isLoading
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Caja de Texto Reconocido
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF7F9FC))
                                .padding(20.dp)
                        ) {
                            if (recognizedText.isEmpty()) {
                                Text(
                                    text = "Tu texto aparecerá aquí...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LightGray,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )
                            } else {
                                Text(
                                    text = recognizedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = DarkGray,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Alerta Flotante
        AnimatedAlertDialog(
            alertState = alertState,
            onDismiss = { alertState = alertState.copy(show = false) }
        )
    }
}

// --- COMPONENTES AUXILIARES (NO BORRAR) ---

@Composable
fun MicrophoneButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ring1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ring2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ring3"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha1"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        if (isRecording) {
            Box(Modifier.size(200.dp).scale(scale1).background(FineduGreen.copy(alpha = pulseAlpha1), CircleShape))
            Box(Modifier.size(180.dp).scale(scale2).background(FineduGreen.copy(alpha = 0.2f), CircleShape))
            Box(Modifier.size(160.dp).scale(scale3).background(FineduGreen.copy(alpha = 0.15f), CircleShape))
        }

        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(120.dp)
                .shadow(elevation = if (isRecording) 12.dp else 8.dp, shape = CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isRecording) {
                            Brush.radialGradient(listOf(FineduGreen.copy(alpha = 0.3f), FineduGreen.copy(alpha = 0.1f)))
                        } else if (enabled) {
                            Brush.linearGradient(listOf(FineduGreen, FineduDarkGreen))
                        } else {
                            Brush.linearGradient(listOf(LightGray, LightGray.copy(alpha = 0.8f)))
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    CircularProgressIndicator(modifier = Modifier.size(70.dp).scale(scale3), color = FineduGreen, strokeWidth = 3.dp)
                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(40.dp).scale(scale3), tint = FineduGreen)
                } else {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Micrófono",
                        modifier = Modifier.size(56.dp),
                        tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedAlertDialog(
    alertState: AlertState,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = alertState.show,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, color, title) = when (alertState.type) {
                        AlertType.SUCCESS -> Triple(Icons.Filled.CheckCircle, FineduGreen, "¡Éxito!")
                        AlertType.ERROR -> Triple(Icons.Filled.Error, FineduRed, "Error")
                        AlertType.WARNING -> Triple(Icons.Outlined.Warning, FineduOrange, "Atención")
                        AlertType.INFO -> Triple(Icons.Outlined.Info, FineduBlue, "Información")
                    }

                    Box(
                        modifier = Modifier.size(80.dp).background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(48.dp), tint = color)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = alertState.message, style = MaterialTheme.typography.bodyLarge, color = MediumGray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (alertState.type != AlertType.SUCCESS) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            Text("Entendido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}