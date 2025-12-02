package com.finedu.app.ui.dashboard

import com.finedu.app.auth.data.TransactionItem

data class MainDashboardState(
    val totalIngresos: Double = 0.0,
    val totalEgresos: Double = 0.0,
    val capitalAmount: Double = 0.0,
    val transactions: List<TransactionItem> = emptyList(),
    val isLoading: Boolean = true,
    val availableCategories: List<String> = emptyList(),
    val error: String? = null
)