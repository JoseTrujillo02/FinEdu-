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
    val FineduGreen = Color(0xFF66BB6A)
    val FineduWhite = Color.White

    val scaleAnimatable = remember { Animatable(0f) }
    val logoAlphaAnimatable = remember { Animatable(1f) }

    LaunchedEffect(key1 = true) {
        delay(1000L)

        launch {
            logoAlphaAnimatable.animateTo(0f, animationSpec = tween(500))
        }
        scaleAnimatable.animateTo(1f, animationSpec = tween(700))
        delay(500L)

        scaleAnimatable.animateTo(50f, animationSpec = tween(800))

        delay(800L)

        navController.navigate(destinationRoute) {
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
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scaleAnimatable.value)
                .background(FineduGreen, CircleShape)
        )

        // Logo "Finedu"
        Text(
           "Finedu",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 48.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            letterSpacing =  -2.sp,
            modifier = Modifier
                .alpha(logoAlphaAnimatable.value)
        )
    }
}