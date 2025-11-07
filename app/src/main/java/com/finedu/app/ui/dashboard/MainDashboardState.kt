package com.finedu.app.ui.dashboard

// 1. Importa el nuevo modelo
import com.finedu.app.auth.data.TransactionItem

data class MainDashboardState(
    val totalIngresos: Double = 0.0,
    val totalEgresos: Double = 0.0,
    val transactions: List<TransactionItem> = emptyList(), // <-- 2. AÑADE ESTA LÍNEA
    val isLoading: Boolean = true,
    val error: String? = null // <-- 3. AÑADE ESTA LÍNEA (para errores)
)