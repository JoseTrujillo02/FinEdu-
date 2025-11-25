@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finedu.app.auth.data.TransactionItem
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// =========================
//  FinancialItem
// =========================
@Composable
fun FinancialItem(
    colors: AppColors,
    icon: ImageVector,
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// =========================
//  Card para registro por voz
// =========================
@Composable
fun AddTransactionCard(colors: AppColors, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(
        1f,
        1.15f,
        infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.4f,
        0.85f,
        infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primary)
    ) {
        Row(modifier = Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(scale)
                        .background(Color.White.copy(alpha = glowAlpha * 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .scale(scale * 0.95f)
                        .background(Color.White.copy(alpha = glowAlpha * 0.35f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        "Grabar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale * 1.05f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Registrar transacción",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    "Usa tu voz para agregar",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp
                )
            }

            Icon(
                Icons.Outlined.ArrowForward,
                null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// =========================
//  Botón toggle (Balance / Tendencias)
// =========================
@Composable
fun ToggleViewButton(colors: AppColors, showTrends: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.primary.copy(alpha = 0.08f),
            contentColor = colors.primary
        )
    ) {
        Icon(
            if (showTrends) Icons.Outlined.BarChart else Icons.Outlined.Timeline,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (showTrends) "Ver Gráfica" else "Ver Tendencias",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Outlined.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.primary.copy(alpha = 0.7f)
        )
    }
}

// =========================
//  Item de transacción
// =========================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionItemRow(colors: AppColors, tx: TransactionItem, onDelete: () -> Unit) {
    val isExpense = tx.type == "expense"
    val iconColor = if (isExpense) colors.expense else colors.primary
    val amountPrefix = if (isExpense) "-" else "+"
    val categoryIcon = when {
        isExpense -> when (tx.category.lowercase()) {
            "comida", "alimentos" -> Icons.Outlined.Restaurant
            "transporte" -> Icons.Outlined.DirectionsCar
            "entretenimiento" -> Icons.Outlined.Theaters
            "salud" -> Icons.Outlined.LocalHospital
            "educación" -> Icons.Outlined.School
            else -> Icons.Outlined.ShoppingBag
        }
        else -> Icons.Outlined.AccountBalanceWallet
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon,
                    tx.category,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                if (!tx.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        tx.description,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tx.date.toFriendlyDateString(),
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "$amountPrefix${tx.amount.toCurrencyString()}",
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        "Eliminar",
                        tint = colors.textTertiary.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// =========================
//  Extensiones de ayuda
// =========================
fun Double.toCurrencyString(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(this)

@RequiresApi(Build.VERSION_CODES.O)
fun String.toFriendlyDateString(): String {
    return try {
        val instant = Instant.parse(this)
        DateTimeFormatter
            .ofPattern("d MMM, yyyy", Locale("es", "MX"))
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (e: Exception) {
        this.take(10)
    }
}
