package com.finedu.app.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finedu.app.auth.data.TransactionItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import kotlin.math.max

// =============================================
//   CONTENEDOR DE ACTIVIDAD
// =============================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCardContainer(
    colors: AppColors,
    state: com.finedu.app.ui.dashboard.MainDashboardState,
    onDeleteTransaction: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when {
            state.isLoading -> LoadingState(colors)
            state.transactions.isEmpty() -> EmptyState(colors)
            else -> TransactionsList(colors, state.transactions, onDeleteTransaction)
        }
    }
}

@Composable
fun LoadingState(colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
fun EmptyState(colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.Receipt,
                null,
                modifier = Modifier.size(64.dp),
                tint = colors.textTertiary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No hay transacciones",
                color = colors.textSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Registra tu primera transacción usando el botón de arriba",
                color = colors.textTertiary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionsList(
    colors: AppColors,
    transactions: List<TransactionItem>,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { tx ->
            TransactionItemRow(colors, tx) { onDelete(tx.id) }
        }
    }
}

@Composable
fun SaludFinancieraCard(colors: AppColors, ingresos: String, egresos: String) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.TrendingUp,
                            null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Tu Salud Financiera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Diseño compacto y elegante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Capital disponible
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ArrowUpward,
                            null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Capital",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        ingresos,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }

                // Divisor vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(colors.textTertiary.copy(alpha = 0.2f))
                )

                // Egresos
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ArrowDownward,
                            null,
                            tint = colors.expense,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Egresos",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        egresos,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.expense
                    )
                }
            }
        }
    }
}

// =============================================
//   ESTADÍSTICAS (Balance del Mes) – CON DATOS REALES POR SEMANA
// =============================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EstadisticasCard(
    colors: AppColors,
    ingresos: Double,
    egresos: Double,
    transactions: List<TransactionItem> = emptyList()
) {
    val balance = ingresos - egresos

    // Agrupar transacciones por semana del mes actual
    val weeklyData = remember(transactions) {
        getWeeklyData(transactions)
    }

    Card(
        Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Balance del Mes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        balance.toCurrencyString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = if (balance >= 0) colors.primary else colors.expense
                    )
                }

                val balanceColor = if (balance >= 0) colors.primary else colors.expense
                Box(
                    Modifier
                        .size(56.dp)
                        .background(balanceColor.copy(alpha = 0.15f), CircleShape),
                    Alignment.Center
                ) {
                    Icon(
                        if (balance >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                        null,
                        tint = balanceColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // GRÁFICA DUAL: Ingresos vs Egresos por semana
            DualLineChart(
                Modifier.fillMaxWidth().height(200.dp),
                colors,
                weeklyData
            )

            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.textTertiary.copy(alpha = 0.2f))
            )
            Spacer(Modifier.height(16.dp))

            val total = ingresos + egresos
            val ingresosPercent = if (total > 0) (ingresos / total * 100).toInt() else 50
            val egresosPercent = if (total > 0) (egresos / total * 100).toInt() else 50

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                PercentageItem(colors, ingresosPercent, colors.primary, "Ingresos")
                PercentageItem(colors, egresosPercent, colors.expense, "Egresos")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
data class WeeklyData(
    val labels: List<String>,
    val ingresos: List<Float>,
    val egresos: List<Float>
)

@RequiresApi(Build.VERSION_CODES.O)
fun getWeeklyData(transactions: List<TransactionItem>): WeeklyData {
    val now = LocalDate.now()
    val startOfMonth = now.withDayOfMonth(1)
    val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())

    // Dividir el mes en 4 semanas
    val weeks = mutableListOf<Pair<LocalDate, LocalDate>>()
    var currentStart = startOfMonth

    while (currentStart <= endOfMonth) {
        val currentEnd = minOf(currentStart.plusDays(6), endOfMonth)
        weeks.add(currentStart to currentEnd)
        currentStart = currentEnd.plusDays(1)
    }

    val labels = weeks.mapIndexed { index, (start, end) ->
        if (weeks.size <= 4) {
            "Sem ${index + 1}"
        } else {
            "${start.dayOfMonth}-${end.dayOfMonth}"
        }
    }

    val ingresosByWeek = mutableListOf<Float>()
    val egresosByWeek = mutableListOf<Float>()

    weeks.forEach { (start, end) ->
        var ingresosWeek = 0.0
        var egresosWeek = 0.0

        transactions.forEach { tx ->
            try {
                val txDate = Instant.parse(tx.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                if (txDate >= start && txDate <= end) {
                    if (tx.type == "income") {
                        ingresosWeek += tx.amount
                    } else {
                        egresosWeek += tx.amount
                    }
                }
            } catch (e: Exception) {
                // Si hay error parseando la fecha, ignorar esta transacción
            }
        }

        ingresosByWeek.add(ingresosWeek.toFloat())
        egresosByWeek.add(egresosWeek.toFloat())
    }

    return WeeklyData(labels, ingresosByWeek, egresosByWeek)
}

@Composable
fun RowScope.PercentageItem(colors: AppColors, percent: Int, color: Color, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "$percent%",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}

// =============================================
//   TENDENCIAS (categorías) - CON DATOS REALES
// =============================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TendenciasCard(colors: AppColors, transactions: List<TransactionItem>) {
    val categoryTotals = transactions
        .filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val totalExpenses = categoryTotals.sumOf { it.second }

    Card(
        Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(colors.expense.copy(alpha = 0.15f), CircleShape),
                    Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Timeline,
                        null,
                        tint = colors.expense,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Tendencias de Gastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Top 5 categorías",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (categoryTotals.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.PieChart,
                            null,
                            tint = colors.textTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No hay datos de gastos", color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                TrendsBarChart(
                    Modifier.fillMaxWidth().height(200.dp),
                    colors,
                    categoryTotals
                )

                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.textTertiary.copy(alpha = 0.2f))
                )
                Spacer(Modifier.height(16.dp))

                categoryTotals.forEach { (category, amount) ->
                    val percentage =
                        if (totalExpenses > 0) (amount / totalExpenses * 100).toInt() else 0
                    TrendItem(colors, category, amount, percentage)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TrendItem(colors: AppColors, category: String, amount: Double, percentage: Int) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            percentage / 100f,
            tween(800, easing = FastOutSlowInEasing)
        )
    }

    val categoryIcon = when (category.lowercase()) {
        "comida", "alimentos" -> Icons.Outlined.Restaurant
        "transporte" -> Icons.Outlined.DirectionsCar
        "entretenimiento" -> Icons.Outlined.Theaters
        "salud" -> Icons.Outlined.LocalHospital
        "educación" -> Icons.Outlined.School
        else -> Icons.Outlined.ShoppingBag
    }

    Column {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.expense.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon, null, tint = colors.expense, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        category,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        "$percentage% del total",
                        color = colors.textTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                amount.toCurrencyString(),
                color = colors.expense,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.expense.copy(alpha = 0.1f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.expense.copy(alpha = 0.7f), colors.expense)
                        )
                    )
            )
        }
    }
}

// =============================================
//     GRÁFICA DUAL – DATOS REALES POR SEMANA
// =============================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DualLineChart(
    modifier: Modifier = Modifier,
    colors: AppColors,
    data: WeeklyData
) {
    val maxValue = max(
        data.ingresos.maxOrNull() ?: 1f,
        data.egresos.maxOrNull() ?: 1f
    )

    Canvas(modifier = modifier) {
        if (data.ingresos.size < 2) return@Canvas

        val chartHeight = size.height * 0.7f
        val chartTop = size.height * 0.1f
        val stepX = size.width / (data.ingresos.size - 1)

        fun valueToY(value: Float): Float {
            val normalized = if (maxValue > 0) (value / maxValue).coerceIn(0f, 1f) else 0f
            return chartTop + chartHeight * (1f - normalized)
        }

        // LÍNEA DE INGRESOS
        val ingresosPath = Path()
        val ingresosGlowPath = Path()
        val ingresosPoints = data.ingresos.mapIndexed { i, v ->
            Offset(i * stepX, valueToY(v))
        }

        ingresosPath.moveTo(ingresosPoints.first().x, ingresosPoints.first().y)
        ingresosGlowPath.moveTo(ingresosPoints.first().x, ingresosPoints.first().y)

        for (i in 0 until ingresosPoints.size - 1) {
            val p0 = ingresosPoints[i]
            val p1 = ingresosPoints[i + 1]
            val midX = (p0.x + p1.x) / 2f
            ingresosPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            ingresosGlowPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        val ingresosFill = Path().apply {
            addPath(ingresosPath)
            lineTo(ingresosPoints.last().x, chartTop + chartHeight)
            lineTo(ingresosPoints.first().x, chartTop + chartHeight)
            close()
        }

        drawPath(
            ingresosFill,
            brush = Brush.verticalGradient(
                listOf(colors.primary.copy(alpha = 0.25f), colors.primary.copy(alpha = 0.05f)),
                startY = chartTop,
                endY = chartTop + chartHeight
            ),
            style = Fill
        )
        drawPath(
            ingresosGlowPath,
            colors.primary.copy(alpha = 0.3f),
            style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            ingresosPath,
            colors.primary,
            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
        )

        ingresosPoints.forEach { point ->
            drawCircle(Color.White, 6.dp.toPx(), center = point)
            drawCircle(colors.primary, 4.dp.toPx(), center = point)
        }

        // LÍNEA DE EGRESOS
        val egresosPath = Path()
        val egresosGlowPath = Path()
        val egresosPoints = data.egresos.mapIndexed { i, v ->
            Offset(i * stepX, valueToY(v))
        }

        egresosPath.moveTo(egresosPoints.first().x, egresosPoints.first().y)
        egresosGlowPath.moveTo(egresosPoints.first().x, egresosPoints.first().y)

        for (i in 0 until egresosPoints.size - 1) {
            val p0 = egresosPoints[i]
            val p1 = egresosPoints[i + 1]
            val midX = (p0.x + p1.x) / 2f
            egresosPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            egresosGlowPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        val egresosFill = Path().apply {
            addPath(egresosPath)
            lineTo(egresosPoints.last().x, chartTop + chartHeight)
            lineTo(egresosPoints.first().x, chartTop + chartHeight)
            close()
        }

        drawPath(
            egresosFill,
            brush = Brush.verticalGradient(
                listOf(colors.expense.copy(alpha = 0.25f), colors.expense.copy(alpha = 0.05f)),
                startY = chartTop,
                endY = chartTop + chartHeight
            ),
            style = Fill
        )
        drawPath(
            egresosGlowPath,
            colors.expense.copy(alpha = 0.3f),
            style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            egresosPath,
            colors.expense,
            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
        )

        egresosPoints.forEach { point ->
            drawCircle(Color.White, 6.dp.toPx(), center = point)
            drawCircle(colors.expense, 4.dp.toPx(), center = point)
        }

        // ETIQUETAS EN EL EJE X
        val textPaint = android.graphics.Paint().apply {
            color = colors.textTertiary.toArgb()
            textSize = 11.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }

        data.labels.forEachIndexed { i, label ->
            drawContext.canvas.nativeCanvas.drawText(
                label,
                i * stepX,
                size.height - 5.dp.toPx(),
                textPaint
            )
        }
    }
}

// =============================================
//     GRÁFICA DE BARRAS PARA TENDENCIAS
// =============================================
@Composable
fun TrendsBarChart(
    modifier: Modifier = Modifier,
    colors: AppColors,
    categoryTotals: List<Pair<String, Double>>
) {
    if (categoryTotals.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val maxValue = categoryTotals.maxOf { it.second }.toFloat()

    Canvas(modifier = modifier) {
        val chartHeight = size.height * 0.75f
        val chartTop = size.height * 0.05f
        val barWidth = size.width / (categoryTotals.size * 2f)
        val spacing = barWidth * 0.5f

        categoryTotals.forEachIndexed { index, (category, amount) ->
            val barHeight = if (maxValue > 0) {
                (amount / maxValue * chartHeight).toFloat()
            } else 0f

            val x = spacing + index * (barWidth + spacing)
            val y = chartTop + chartHeight - barHeight

            // Barra con gradiente
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(colors.expense, colors.expense.copy(alpha = 0.7f)),
                    startY = y,
                    endY = chartTop + chartHeight
                ),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Etiqueta de categoría
            val textPaint = android.graphics.Paint().apply {
                color = colors.textTertiary.toArgb()
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val shortLabel = when {
                category.length > 8 -> category.take(6) + ".."
                else -> category
            }

            drawContext.canvas.nativeCanvas.drawText(
                shortLabel,
                x + barWidth / 2,
                size.height - 5.dp.toPx(),
                textPaint
            )

            // Valor encima de la barra
            if (barHeight > 30.dp.toPx()) {
                val valuePaint = android.graphics.Paint().apply {
                    color = Color.White.toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }

                val valueText = if (amount >= 1000) {
                    "${(amount / 1000).toInt()}k"
                } else {
                    amount.toInt().toString()
                }

                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    x + barWidth / 2,
                    y + 20.dp.toPx(),
                    valuePaint
                )
            }
        }
    }
}