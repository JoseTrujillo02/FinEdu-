package com.finedu.app.ui.dictation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.finedu.app.R
import com.finedu.app.ui.dictation.UiEvent
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import kotlinx.coroutines.delay

// 🔹 Datadog RUM
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumActionType
import com.datadog.android.rum.RumErrorSource

// --- MODELOS DE UI LOCALES ---
data class AlertState(
    val show: Boolean = false,
    val message: String = "",
    val type: AlertType = AlertType.SUCCESS,
    val showExtraInfo: Boolean = false,
    val onAction: (() -> Unit)? = null,
    val actionText: String = "Entendido"
)

enum class AlertType {
    SUCCESS, ERROR, INFO, WARNING, PERMISSION_DENIED
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VoiceDictationScreen(
    navController: NavController,
    viewModel: VoiceDictationViewModel
) {
    val state by viewModel.state.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    var alertState by remember { mutableStateOf(AlertState()) }
    var permissionDeniedCount by remember { mutableStateOf(0) }
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("show_success_snackbar", event.message)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                    navController.popBackStack()
                }
                is UiEvent.Error -> {
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
                if (newText.isNotBlank()) {

                    // 🔹 Datadog: dictado exitoso
                    GlobalRumMonitor.get().addAction(
                        RumActionType.CUSTOM,
                        "voice_dictation_success",
                        mapOf(
                            "feature" to "voice_dictation",
                            "screen" to "VoiceDictationScreen",
                            "recognized_length" to newText.length
                        )
                    )

                    viewModel.sendMessage(newText)
                }
            } else {
                // 🔹 Datadog: hubo audio pero no se obtuvieron resultados
                GlobalRumMonitor.get().addError(
                    "voice_dictation_no_results",
                    RumErrorSource.SOURCE,
                    null,
                    mapOf(
                        "feature" to "voice_dictation",
                        "screen" to "VoiceDictationScreen"
                    )
                )
            }
        } else {
            // 🔹 Datadog: el usuario canceló o el intent devolvió error
            GlobalRumMonitor.get().addError(
                "voice_dictation_cancel_or_error",
                RumErrorSource.SOURCE,
                null,
                mapOf(
                    "feature" to "voice_dictation",
                    "screen" to "VoiceDictationScreen",
                    "result_code" to result.resultCode
                )
            )

            // El usuario canceló o hubo un error
            alertState = AlertState(
                show = true,
                message = "No se detectó voz. Intenta nuevamente y habla más claro.",
                type = AlertType.INFO
            )
        }
    }

    val launchDictationIntent = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            putExtra("android.speech.extra.DICTATION_MODE", true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            // 🔹 Datadog: el usuario inicia un dictado de voz
            GlobalRumMonitor.get().addAction(
                RumActionType.CUSTOM,
                "voice_dictation_start",
                mapOf(
                    "feature" to "voice_dictation",
                    "screen" to "VoiceDictationScreen"
                )
            )

            speechRecognizerLauncher.launch(intent)
        } else {
            // 🔹 Datadog: no hay app de reconocimiento de voz disponible
            GlobalRumMonitor.get().addError(
                "voice_recognition_not_available",
                RumErrorSource.SOURCE,
                null,
                mapOf(
                    "feature" to "voice_dictation",
                    "screen" to "VoiceDictationScreen"
                )
            )

            alertState = AlertState(
                show = true,
                message = "Tu dispositivo no soporta el reconocimiento de voz. Asegúrate de tener Google instalado y actualizado.",
                type = AlertType.ERROR
            )
        }
    }

    // 2. LAUNCHER DE PERMISOS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, verificar una vez más antes de lanzar
            val currentPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )

            if (currentPermission == PackageManager.PERMISSION_GRANTED) {
                launchDictationIntent()
            } else {
                // Algo salió mal
                showPermissionExplanation(
                    onDismiss = { alertState = alertState.copy(show = false) },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        alertState = alertState.copy(show = false)
                    }
                ) { newState ->
                    alertState = newState
                }
            }
        } else {
            permissionDeniedCount++

            // 🔹 Datadog: el usuario negó el permiso del micrófono
            GlobalRumMonitor.get().addError(
                "mic_permission_denied",
                RumErrorSource.SOURCE,
                null,
                mapOf(
                    "feature" to "voice_dictation",
                    "screen" to "VoiceDictationScreen",
                    "denied_count" to permissionDeniedCount
                )
            )

            if (permissionDeniedCount >= 2) {
                // El usuario ha rechazado el permiso múltiples veces
                showPermissionExplanation(
                    onDismiss = { alertState = alertState.copy(show = false) },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        alertState = alertState.copy(show = false)
                    }
                ) { newState ->
                    alertState = newState
                }
            } else {
                // Primera vez que rechaza
                alertState = AlertState(
                    show = true,
                    message = "El permiso de micrófono es necesario para registrar transacciones por voz. Por favor, concede el permiso para continuar.",
                    type = AlertType.WARNING
                )
            }
        }
    }

    // 3. FUNCIÓN DEL BOTÓN
    val handleMicrophoneClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchDictationIntent()
        } else {
            // Solicitar permiso
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
                Spacer(modifier = Modifier.width(48.dp))
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

                        // TÍTULO ANIMADO
                        AnimatedContent(
                            targetState = state.isLoading,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "title_animation"
                        ) { isLoading ->
                            Text(
                                text = if (isLoading) "Procesando tu solicitud..." else "Toca el micrófono para dictar",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkGray,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // BOTÓN DE MICRÓFONO MEJORADO
                        ImprovedMicrophoneButton(
                            isProcessing = state.isLoading,
                            onClick = handleMicrophoneClick,
                            enabled = !state.isLoading
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Caja de Texto Reconocido CON ANIMACIÓN
                        AnimatedTextBox(
                            text = recognizedText,
                            isProcessing = state.isLoading
                        )
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

// Función helper para mostrar explicación de permisos
private fun showPermissionExplanation(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    setAlertState: (AlertState) -> Unit
) {
    setAlertState(
        AlertState(
            show = true,
            message = "Para usar el dictado por voz necesitas habilitar el permiso de micrófono.\n\n" +
                    "Pasos:\n" +
                    "1. Toca 'Ir a Configuración'\n" +
                    "2. Busca 'Permisos'\n" +
                    "3. Toca 'Micrófono'\n" +
                    "4. Selecciona 'Permitir'",
            type = AlertType.PERMISSION_DENIED,
            onAction = onOpenSettings,
            actionText = "Ir a Configuración"
        )
    )
}

// =============================================
//   BOTÓN DE MICRÓFONO MEJORADO
// =============================================
@Composable
fun ImprovedMicrophoneButton(
    isProcessing: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1_alpha"
    )

    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring2_alpha"
    )

    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring3_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        if (isProcessing) {
            Box(
                Modifier
                    .size(220.dp)
                    .scale(pulseScale)
                    .alpha(ring1Alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FineduGreen.copy(alpha = 0.3f),
                                FineduGreen.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                Modifier
                    .size(180.dp)
                    .scale(pulseScale * 0.95f)
                    .alpha(ring2Alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FineduGreen.copy(alpha = 0.4f),
                                FineduGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                Modifier
                    .size(140.dp)
                    .scale(pulseScale * 0.9f)
                    .alpha(ring3Alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FineduGreen.copy(alpha = 0.5f),
                                FineduGreen.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(110.dp)
                .shadow(
                    elevation = if (isProcessing) 16.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = if (isProcessing) FineduGreen else Color.Gray,
                    spotColor = if (isProcessing) FineduGreen else Color.Gray
                )
                .scale(if (isProcessing) pulseScale else 1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = when {
                            isProcessing -> Brush.radialGradient(
                                colors = listOf(
                                    FineduGreen.copy(alpha = 0.9f),
                                    FineduDarkGreen
                                )
                            )
                            enabled -> Brush.linearGradient(
                                colors = listOf(FineduGreen, FineduDarkGreen)
                            )
                            else -> Brush.linearGradient(
                                colors = listOf(LightGray, LightGray.copy(alpha = 0.8f))
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(75.dp),
                            color = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 3.dp
                        )
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Micrófono",
                        modifier = Modifier.size(52.dp),
                        tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// =============================================
//   CAJA DE TEXTO ANIMADA
// =============================================
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTextBox(
    text: String,
    isProcessing: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_box_animation")

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isProcessing) {
                    Brush.verticalGradient(
                        colors = listOf(
                            FineduGreen.copy(alpha = 0.08f * shimmerAlpha),
                            Color(0xFFF7F9FC),
                            FineduGreen.copy(alpha = 0.08f * shimmerAlpha)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF7F9FC), Color(0xFFF7F9FC))
                    )
                }
            )
            .padding(20.dp)
    ) {
        AnimatedContent(
            targetState = Pair(text.isEmpty(), isProcessing),
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) with
                        fadeOut(animationSpec = tween(400))
            },
            label = "text_content"
        ) { (isEmpty, processing) ->
            when {
                processing -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = FineduGreen,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analizando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                isEmpty -> {
                    Text(
                        text = "Tu texto aparecerá aquí...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightGray,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkGray,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================
//   DIÁLOGO DE ALERTA MEJORADO
// =============================================
@Composable
fun AnimatedAlertDialog(
    alertState: AlertState,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = alertState.show,
        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
        exit = fadeOut(tween(300)) + scaleOut(tween(300))
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, color, title) = when (alertState.type) {
                        AlertType.SUCCESS -> Triple(Icons.Filled.CheckCircle, FineduGreen, "¡Éxito!")
                        AlertType.ERROR -> Triple(Icons.Filled.Error, FineduRed, "Error")
                        AlertType.WARNING -> Triple(Icons.Outlined.Warning, FineduOrange, "Atención")
                        AlertType.INFO -> Triple(Icons.Outlined.Info, FineduBlue, "Información")
                        AlertType.PERMISSION_DENIED -> Triple(Icons.Outlined.MicOff, FineduOrange, "Permiso Necesario")
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(48.dp), tint = color)
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
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (alertState.type != AlertType.SUCCESS) {
                        Button(
                            onClick = {
                                alertState.onAction?.invoke() ?: onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            Text(
                                alertState.actionText,
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
