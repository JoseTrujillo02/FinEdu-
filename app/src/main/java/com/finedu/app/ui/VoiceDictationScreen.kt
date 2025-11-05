package com.finedu.app.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack // <-- 1. Importar icono
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // <-- 2. Importar NavController

/**
 * Composible para la interfaz de dictado y reconocimiento de voz.
 * Usa el sistema RecognizerIntent de Android para la captura de audio.
 */
@Composable
fun VoiceDictationScreen(
    navController: NavController // <-- 3. Aceptar el NavController
) {
    // Estado para almacenar el texto reconocido por voz.
    var recognizedText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Launcher para iniciar la actividad del sistema de reconocimiento de voz y obtener el resultado.
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Extrae la lista de resultados de texto (EXTRA_RESULTS).
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!matches.isNullOrEmpty()) {
                // Toma el resultado más probable (índice 0).
                recognizedText = matches[0]
            }
        } else {
            recognizedText = "Reconocimiento cancelado o fallido."
        }
    }

    // Lógica para construir y lanzar el Intent de reconocimiento de voz.
    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es") // Idioma español
            putExtra(RecognizerIntent.EXTRA_PROMPT, "¡Habla ahora para dictar!")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            speechRecognizerLauncher.launch(intent)
        } else {
            recognizedText = "El dispositivo no soporta el reconocimiento de voz."
        }
    }

    // Lógica para enviar la patecion
    val sendMessage ={

    }

    // Interfaz de Usuario (UI) de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // Cambiamos el Arrangement para hacer espacio arriba
        verticalArrangement = Arrangement.Top // <-- 4. CAMBIO AQUÍ
    ) {

        // --- 5. BOTÓN DE VOLVER (AÑADIDO) ---
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController.popBackStack() }, // <-- 6. Acción de volver
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
        // --- FIN DEL BLOQUE AÑADIDO ---


        // Campo de texto para el resultado
        OutlinedTextField(
            value = recognizedText,
            onValueChange = { recognizedText = it },
            label = { Text("Texto Dictado / Edítame") },
            readOnly = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para iniciar el dictado
        Button(
            onClick = startSpeechRecognition,
            enabled = (recognizedText != "El dispositivo no soporta el reconocimiento de voz."),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Micrófono")
        }
         // Espaciador entre los botones
        Spacer(modifier = Modifier.height(16.dp))

        // boton para enviar la solicitud
        Button(
            onClick = sendMessage,
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Enviar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.Add, contentDescription = "enviar")
        }
    }
}