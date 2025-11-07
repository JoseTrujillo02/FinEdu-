package com.finedu.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainDashboardViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainDashboardState())
    val state = _state.asStateFlow()

    init {
        loadDashboardDataForThisMonth()
    }

    // --- ¡CAMBIO AQUÍ! ---
    // 1. Quitamos la palabra 'private'
    fun loadDashboardDataForThisMonth() {
        viewModelScope.launch {
            // 2. Nos aseguramos de poner 'isLoading = true' al inicio
            _state.update { it.copy(isLoading = true, error = null) }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "No hay sesión") }
                return@launch
            }
            val token = "Bearer ${session.idToken}"

            // ... (Lógica de fechas)
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val fromDate = "$year-${String.format("%02d", month)}-01"

            try {
                // ... (Lógica de API)
                val response = authApiService.getTransactions(token = token, from = fromDate)

                if (response.isSuccessful && response.body() != null) {
                    val transactions = response.body()!!.items
                    val totalEgresos = transactions
                        .filter { it.type == "expense" }
                        .sumOf { it.amount }
                    val totalIngresos = transactions
                        .filter { it.type == "income" }
                        .sumOf { it.amount }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            transactions = transactions,
                            totalEgresos = totalEgresos,
                            totalIngresos = totalIngresos
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Error de red") }
            }
        }
    }
}