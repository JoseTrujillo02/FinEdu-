package com.finedu.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Enums para filtros
enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

enum class TransactionSort {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, CATEGORY
}

// Colores adaptativos
@Composable
fun getColorScheme(): AppColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        AppColors(
            primary = Color(0xFF66BB6A),
            primaryDark = Color(0xFF4CAF50),
            primaryLight = Color(0xFF81C784),
            expense = Color(0xFFFF8A65),
            textPrimary = Color(0xFFE8EAED),
            textSecondary = Color(0xFFB0B8C1),
            textTertiary = Color(0xFF8A9199),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2A2A2A),
            topBarStart = Color(0xFF263238),
            topBarEnd = Color(0xFF37474F)
        )
    } else {
        AppColors(
            primary = Color(0xFF4CAF50),
            primaryDark = Color(0xFF388E3C),
            primaryLight = Color(0xFF81C784),
            expense = Color(0xFFFF7043),
            textPrimary = Color(0xFF1A2332),
            textSecondary = Color(0xFF3A4F66),
            textTertiary = Color(0xFF94A3B8),
            background = Color(0xFFF5F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFAFAFA),
            topBarStart = Color(0xFF2C3E50),
            topBarEnd = Color(0xFF4A5568)
        )
    }
}

data class AppColors(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val expense: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val topBarStart: Color,
    val topBarEnd: Color
)