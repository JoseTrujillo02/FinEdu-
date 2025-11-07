package com.finedu.app.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

/**
 * Composible para la interfaz de dictado y reconocimiento de voz.
 */
@Composable
fun VoiceDictationScreen(
    navController: NavController,
    viewModel: VoiceDictationViewModel
) {

    val state by viewModel.state.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // --- 1. PREPARA EL SNACKBAR ---
    val snackbarHostState = remember { SnackbarHostState() }

    // --- 2. ESCUCHA LOS EVENTOS DE ÉXITO/ERROR ---
    // (Este LaunchedEffect se ejecuta una vez y escucha los "eventos únicos")
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    // Muestra el banner de éxito
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                }
                is UiEvent.Error -> {
                    // Muestra el banner de error
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long, // Más tiempo para leer el error
                        withDismissAction = true
                    )
                }
            }
        }
    }

    // --- (Tu lógica de reconocimiento de voz se queda igual) ---
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                recognizedText = matches[0]
            }
        } else {
            recognizedText = "Reconocimiento cancelado o fallido."
        }
    }

    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "¡Habla ahora para dictar!")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            speechRecognizerLauncher.launch(intent)
        } else {
            recognizedText = "El dispositivo no soporta el reconocimiento de voz."
        }
    }

    // --- 3. ENVUELVE TU UI EN UN SCAFFOLD ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        // Columna principal con tu UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding del Scaffold
                .padding(24.dp), // Tu padding original
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // --- BOTÓN DE VOLVER (se queda igual) ---
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController.popBackStack() },
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

            // --- 4. ELIMINAMOS LOS AnimatedVisibility Y Text(Error) ---
            // (Ya no son necesarios, el Snackbar los reemplaza)

            // Campo de texto para el resultado
            OutlinedTextField(
                value = recognizedText,
                onValueChange = { recognizedText = it },
                label = { Text("Texto Dictado / Edítame") },
                // Se deshabilita solo si está cargando
                readOnly = state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para iniciar el dictado
            Button(
                onClick = startSpeechRecognition,
                // Se deshabilita solo si está cargando
                enabled = !state.isLoading,
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Micrófono")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para enviar la solicitud
            Button(
                onClick = { viewModel.sendMessage(recognizedText) },
                // Se deshabilita si está cargando O si el texto está vacío
                enabled = !state.isLoading && recognizedText.isNotBlank(),
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
                    Text("Enviar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Add, contentDescription = "enviar")
                }
            }
        } // Fin de Column
    } // Fin de Scaffold
}