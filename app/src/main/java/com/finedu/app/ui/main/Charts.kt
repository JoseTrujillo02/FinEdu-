
package com.finedu.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SmoothLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFEC407A),
    glowColor: Color = Color(0x33EC407A),
    strokeWidth: Float = 6f
) {
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f

    var animProgress by remember { mutableStateOf(0f) }

    val animation by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "lineChartAnim"
    )

    LaunchedEffect(Unit) { animProgress = 1f }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 10.dp)
    ) {
        if (values.size < 2) return@Canvas

        val chartWidth = size.width
        val chartHeight = size.height
        val space = chartWidth / (values.size - 1)

        fun valueToY(v: Float): Float {
            val normalized = if (maxValue == minValue) 0.5f
            else (v - minValue) / (maxValue - minValue)
            return chartHeight - (normalized * chartHeight)
        }

        val totalPoints = values.size
        val visiblePoints = (totalPoints * animation).roundToInt().coerceIn(2, totalPoints)
        val subValues = values.take(visiblePoints)

        if (subValues.size < 2) return@Canvas

        val path = Path()
        val startY = valueToY(subValues.first())
        path.moveTo(0f, startY)

        for (i in 1 until subValues.size) {
            val x = i * space
            val y = valueToY(subValues[i])
            val prevX = (i - 1) * space
            val prevY = valueToY(subValues[i - 1])
            val controlX = (prevX + x) / 2f

            path.cubicTo(controlX, prevY, controlX, y, x, y)
        }

        // Glow
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(glowColor, Color.Transparent)
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth * 4
            )
        )

        // Línea principal
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth
            )
        )

        // Puntos finales (opcional, look más "pulso")
        val lastX = (subValues.size - 1) * space
        val lastY = valueToY(subValues.last())
        drawCircle(
            color = lineColor,
            radius = strokeWidth * 1.4f,
            center = Offset(lastX, lastY)
        )
    }
}
