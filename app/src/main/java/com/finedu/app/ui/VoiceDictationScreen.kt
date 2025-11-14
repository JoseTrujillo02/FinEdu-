package com.finedu.app.ui

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.finedu.app.R
import com.finedu.app.ui.dictation.UiEvent
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import kotlinx.coroutines.delay

data class AlertState(
    val show: Boolean = false,
    val message: String = "",
    val type: AlertType = AlertType.SUCCESS,
    val showExtraInfo: Boolean = false
)

enum class AlertType {
    SUCCESS, ERROR, INFO, WARNING
}

// Paleta de colores refinada - Verde como acento
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

    // Manejo de eventos UI
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("show_success_snackbar", event.message)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                    navController.popBackStack()
                }
                is UiEvent.Error -> {
                    // Detectar tipos específicos de error y mostrar mensajes apropiados
                    val (type, message) = when {
                        // Errores de fondos insuficientes
                        event.message.contains("fondos insuficientes", ignoreCase = true) ||
                                event.message.contains("no tienes suficiente", ignoreCase = true) ||
                                event.message.contains("saldo insuficiente", ignoreCase = true) ||
                                event.message.contains("no hay suficiente", ignoreCase = true) ||
                                event.message.contains("excede", ignoreCase = true) ||
                                event.message.contains("supera", ignoreCase = true) -> {
                            AlertType.WARNING to "No tienes suficiente dinero disponible para realizar esta transacción. Verifica tu capital actual."
                        }
                        // Errores de capital no configurado
                        event.message.contains("capital", ignoreCase = true) &&
                                event.message.contains("configurado", ignoreCase = true) -> {
                            AlertType.WARNING to "Debes configurar tu capital inicial en tu perfil antes de registrar transacciones."
                        }
                        // Otros errores
                        else -> AlertType.ERROR to event.message
                    }

                    alertState = AlertState(
                        show = true,
                        message = message,
                        type = type,
                        showExtraInfo = false
                    )
                    delay(5000)
                    alertState = alertState.copy(show = false)
                }
            }
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!matches.isNullOrEmpty()) {
                val newText = matches[0]
                // Mostrar el texto exactamente como fue reconocido
                recognizedText = newText

                if (newText.isNotBlank()) {
                    viewModel.sendMessage(newText)
                }
            }
        } else {
            alertState = AlertState(
                show = true,
                message = "Reconocimiento cancelado o fallido",
                type = AlertType.ERROR
            )
        }
    }

    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("es-MX", "es-ES", "es"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora…")
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo con overlay oscuro sutil
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro para mejorar contraste
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
            // Header con botón de volver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
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

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card principal con diseño limpio y botón integrado
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

                        // Botón del micrófono animado (ahora clickeable)
                        MicrophoneButton(
                            isRecording = state.isLoading,
                            onClick = startSpeechRecognition,
                            enabled = !state.isLoading
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Campo de texto con diseño minimalista
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

        // Alert Dialog Centralizado con información adicional
        AnimatedAlertDialog(
            alertState = alertState,
            onDismiss = { alertState = alertState.copy(show = false) }
        )
    }
}

@Composable
fun MicrophoneButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")

    // Animaciones para los anillos pulsantes
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1_scale"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring2_scale"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring3_scale"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha1"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha2"
    )

    val pulseAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha3"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Anillos exteriores pulsantes cuando está grabando
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale1)
                    .background(
                        color = FineduGreen.copy(alpha = pulseAlpha1),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale2)
                    .background(
                        color = FineduGreen.copy(alpha = pulseAlpha2),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale3)
                    .background(
                        color = FineduGreen.copy(alpha = pulseAlpha3),
                        shape = CircleShape
                    )
            )
        }

        // Círculo principal del micrófono con efecto de elevación
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = if (isRecording) 12.dp else 8.dp,
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isRecording) {
                            Brush.radialGradient(
                                colors = listOf(
                                    FineduGreen.copy(alpha = 0.3f),
                                    FineduGreen.copy(alpha = 0.15f),
                                    FineduGreen.copy(alpha = 0.1f)
                                )
                            )
                        } else if (enabled) {
                            Brush.linearGradient(
                                colors = listOf(
                                    FineduGreen,
                                    FineduDarkGreen
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    LightGray,
                                    LightGray.copy(alpha = 0.8f)
                                )
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(70.dp)
                                .scale(scale3),
                            color = FineduGreen,
                            strokeWidth = 3.dp
                        )

                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Procesando",
                            modifier = Modifier
                                .size(40.dp)
                                .scale(scale3),
                            tint = FineduGreen
                        )
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

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

        if (!isRecording && enabled) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                FineduGreen.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
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
        enter = fadeIn(animationSpec = tween(300)) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(300))
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon: ImageVector
                    val iconColor: Color
                    val backgroundColor: Color
                    val title: String

                    when (alertState.type) {
                        AlertType.SUCCESS -> {
                            icon = Icons.Filled.CheckCircle
                            iconColor = FineduGreen
                            backgroundColor = FineduGreen.copy(alpha = 0.1f)
                            title = "¡Éxito!"
                        }
                        AlertType.ERROR -> {
                            icon = Icons.Filled.Error
                            iconColor = FineduRed
                            backgroundColor = FineduRed.copy(alpha = 0.1f)
                            title = "Error"
                        }
                        AlertType.WARNING -> {
                            icon = Icons.Outlined.Warning
                            iconColor = FineduOrange
                            backgroundColor = FineduOrange.copy(alpha = 0.1f)
                            title = "Atención"
                        }
                        AlertType.INFO -> {
                            icon = Icons.Outlined.Info
                            iconColor = FineduBlue
                            backgroundColor = FineduBlue.copy(alpha = 0.1f)
                            title = "Información"
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = backgroundColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = iconColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = alertState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MediumGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (alertState.type != AlertType.SUCCESS) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (alertState.type) {
                                    AlertType.ERROR -> FineduRed
                                    AlertType.WARNING -> FineduOrange
                                    AlertType.INFO -> FineduBlue
                                    else -> FineduGreen
                                }
                            )
                        ) {
                            Text(
                                "Entendido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}