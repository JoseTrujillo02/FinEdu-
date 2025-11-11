package com.finedu.app.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.TransactionItem
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Calendar

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

    fun loadDashboardDataForThisMonth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "No hay sesión") }
                return@launch
            }
            val token = "Bearer ${session.idToken}"

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val fromDate = "$year-${String.format("%02d", month)}-01"

            try {
                // Ejecutar en IO para no bloquear UI
                val transactionsDeferred = async(Dispatchers.IO) {
                    runCatching { authApiService.getTransactions(token = token, from = fromDate) }
                }
                val capitalDeferred = async(Dispatchers.IO) {
                    runCatching { authApiService.getCapitalSettings(token = token) }
                }

                // await resultados (cada uno puede fallar, lo manejamos)
                val transactionsResult = transactionsDeferred.await()
                val capitalResult = capitalDeferred.await()

                // Si alguna coroutine fue cancelada debemos propagarla
                // (no atrapar CancellationException como general)
                // runCatching no captura CancellationException por default; si se catched, re-lanzar:
                // (en este patrón no necesitamos re-lanzar explícitamente aquí)

                // Manejo de cada respuesta:
                if (transactionsResult.isFailure) {
                    val ex = transactionsResult.exceptionOrNull()
                    handleNetworkException(ex)
                    return@launch
                }
                if (capitalResult.isFailure) {
                    val ex = capitalResult.exceptionOrNull()
                    handleNetworkException(ex)
                    return@launch
                }

                val transactionsResponse = transactionsResult.getOrNull()
                val capitalResponse = capitalResult.getOrNull()

                // Comprobaciones seguras antes de usar body()
                if (transactionsResponse == null || capitalResponse == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Respuesta nula del servidor. Intenta de nuevo."
                        )
                    }
                    return@launch
                }

                // Manejo de 401 (sesión inválida)
                if (transactionsResponse.code() == 401 || capitalResponse.code() == 401) {
                    sessionRepository.clearSession()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Tu sesión ha expirado. Por favor, inicia sesión de nuevo."
                        )
                    }
                    return@launch
                }

                if (!transactionsResponse.isSuccessful || !capitalResponse.isSuccessful) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error del servidor (Código ${transactionsResponse.code()} o ${capitalResponse.code()}). Intenta más tarde."
                        )
                    }
                    return@launch
                }

                // Usar safe-call y valores por defecto
                val transactions = transactionsResponse.body()?.items ?: emptyList<TransactionItem>()
                val totalEgresos = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                val totalIngresos = transactions.filter { it.type == "income" }.sumOf { it.amount }
                val capitalAmount = capitalResponse.body()?.amount ?: 0.0

                _state.update {
                    it.copy(
                        isLoading = false,
                        transactions = transactions,
                        totalEgresos = totalEgresos,
                        totalIngresos = totalIngresos,
                        capitalAmount = capitalAmount,
                        error = null
                    )
                }

            } catch (ce: CancellationException) {
                // Re-lanzar cancelaciones para respetar la semántica de coroutines
                throw ce
            } catch (e: Exception) {
                // Último recurso: mapear excepción y actualizar estado
                handleNetworkException(e)
            }
        }
    }

    private fun handleNetworkException(e: Throwable?) {
        val errorMessage = when (e) {
            is UnknownHostException -> "Error de conexión. Revisa tu internet."
            is SocketTimeoutException -> "El servidor tardó mucho en responder."
            is IOException -> "Error de red. Intenta de nuevo."
            null -> "Error desconocido de red."
            else -> "Error: ${e.message ?: "desconocido"}"
        }
        _state.update { it.copy(isLoading = false, error = errorMessage) }
        Log.e("MainDashboardVM", "Excepción atrapada en red: ", e)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
