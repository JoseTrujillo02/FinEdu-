package com.finedu.app.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent // <-- 1. ¡IMPORTA ESTO!
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.finedu.app.ui.dictation.UiEvent
import com.finedu.app.ui.dictation.VoiceDictationViewModel

@Composable
fun VoiceDictationScreen(
    navController: NavController,
    viewModel: VoiceDictationViewModel
) {
    val state by viewModel.state.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("show_success_snackbar", event.message)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                    navController.popBackStack()
                }
                is UiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
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
                recognizedText = newText

                if (newText.isNotBlank()) {
                    viewModel.sendMessage(newText)
                }
                // --- FIN ---

            }
        } else {
            recognizedText = "Reconocimiento cancelado o fallido."
        }
    }

    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "¡Habla ahora! (Se enviará tras 5s de silencio)")

            // --- ¡LA LÍNEA MÁGICA! ---
            // Le dice al dictado que espere 5 segundos de silencio antes de detenerse
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L) // 5000ms = 5s
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            speechRecognizerLauncher.launch(intent)
        } else {
            recognizedText = "El dispositivo no soporta el reconocimiento de voz."
        }
    }

    // --- 4. MODIFICAMOS LA UI ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // --- (Botón de Volver y Título se quedan igual) ---
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController.popBackStack() }, // Acción de volver
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Dictado por Voz",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = recognizedText,
                onValueChange = { recognizedText = it },
                label = { Text("Texto Dictado / Edítame") },
                readOnly = state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = startSpeechRecognition,
                enabled = !state.isLoading,
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Dictar y Enviar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Mic, contentDescription = "Micrófono")
                }
            }
        }
    }
}