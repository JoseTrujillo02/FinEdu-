// Asume que este archivo es: com.finedu.app.ui.SplashScreen.kt

package com.finedu.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController, destinationRoute: String) {
    // Definición de colores
    val FineduGreen = Color(0xFF66BB6A)
    val FineduWhite = Color.White

    // Animatable para el tamaño de la esfera
    val scaleAnimatable = remember { Animatable(0f) }
    // Animatable para la opacidad del logo
    val logoAlphaAnimatable = remember { Animatable(1f) }

    // Orquestación de la animación
    LaunchedEffect(key1 = true) {
        // Fase 1: Mostrar el logo por un momento
        delay(1000L)

        // Fase 2: Esfera crece (0f a 1f) y logo se desvanece
        launch {
            logoAlphaAnimatable.animateTo(0f, animationSpec = tween(500))
        }
        scaleAnimatable.animateTo(1f, animationSpec = tween(700))
        delay(500L)

        // Fase 3: Esfera se expande para llenar la pantalla
        scaleAnimatable.animateTo(50f, animationSpec = tween(800))

        delay(800L) // Mantener la pantalla verde

        // Navegación inmediata a la pantalla de Autenticación
        navController.navigate(destinationRoute) {
            // Esto borra la pantalla Splash del historial.
            // Es la forma correcta de evitar que el usuario "vuelva" al Splash.
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FineduWhite),
        contentAlignment = Alignment.Center
    ) {
        // Esfera Verde
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scaleAnimatable.value)
                .background(FineduGreen, CircleShape)
        )

        // Logo "Finedu"
        Text(
           "Finedu",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing =  -2.sp,
            modifier = Modifier
                .alpha(logoAlphaAnimatable.value)
        )
    }
}