package com.finedu.app.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.CapitalSettingsRequest
import com.finedu.app.auth.data.DeleteAccountRequest
import com.finedu.app.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ProfileUiEvent {
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
    object NavigateToLogin : ProfileUiEvent()
    object SaveCapitalSuccess : ProfileUiEvent() // <-- AÑADE ESTA LÍNEA
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authApiService: AuthApiService
) : ViewModel() {

    // --- 3. CORRECCIÓN DEL ESTADO ---
    // (Usamos MutableStateFlow para poder actualizarlo desde varias funciones)
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<ProfileUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        // Carga todos los datos al iniciar
        loadProfileData()
        loadCapitalSettings()
    }

    // Carga el nombre y el email desde la "caja fuerte"
    private fun loadProfileData() {
        viewModelScope.launch {
            sessionRepository.getStoredSession().collect { userData ->
                if (userData != null) {
                    _state.update { it.copy(name = userData.name, email = userData.email) }
                } else {
                    _state.update { it.copy(name = "Error", email = "Error") }
                }
            }
        }
    }

    // Carga (GET) la configuración de capital desde la API
    fun loadCapitalSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCapital = true) }
            try {
                val session = sessionRepository.getStoredSession().firstOrNull()
                if (session == null) {
                    _state.update { it.copy(isLoadingCapital = false) }
                    return@launch
                }
                val token = "Bearer ${session.idToken}"

                val response = authApiService.getCapitalSettings(token)

                if (response.isSuccessful && response.body() != null) {
                    val capital = response.body()!!
                    _state.update {
                        it.copy(
                            isLoadingCapital = false,
                            capitalAmount = capital.amount.toString(),
                            capitalPeriodicity = capital.periodicity.replaceFirstChar { p -> p.uppercase() }
                        )
                    }
                } else {
                    _state.update { it.copy(isLoadingCapital = false) }
                    Log.w("ProfileVM", "No se pudo cargar el capital: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingCapital = false) }
                _uiEvent.send(ProfileUiEvent.ShowSnackbar(e.message ?: "Error de red"))
            }
        }
    }

    // --- 4. FUNCIONES PARA QUE LA UI ACTUALICE EL ESTADO ---
    fun onCapitalAmountChanged(newAmount: String) {
        _state.update { it.copy(capitalAmount = newAmount) }
    }

    fun onPeriodicityChanged(newPeriodicity: String) {
        _state.update { it.copy(capitalPeriodicity = newPeriodicity) }
    }

    // --- 5. FUNCIÓN 'saveCapital' CORREGIDA ---
    fun saveCapital() {
        viewModelScope.launch {
            _state.update { it.copy(isSavingCapital = true) }

            val currentState = _state.value
            val parsedAmount = currentState.capitalAmount
                .replace("$", "").replace(",", "").trim().toDoubleOrNull()

            if (parsedAmount == null) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("Monto inválido"))
                _state.update { it.copy(isSavingCapital = false) }
                return@launch
            }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("No hay sesión activa"))
                _state.update { it.copy(isSavingCapital = false) }
                return@launch
            }
            val token = "Bearer ${session.idToken}"

            try {
                val request = CapitalSettingsRequest(
                    amount = parsedAmount,
                    periodicity = currentState.capitalPeriodicity.lowercase()
                )
                val response = authApiService.updateCapitalSettings(token, request)

                // --- 2. ¡ENVÍA EL NUEVO EVENTO DE ÉXITO! ---
                if (response.isSuccessful) {
                    _uiEvent.send(ProfileUiEvent.SaveCapitalSuccess) // <-- CAMBIO AQUÍ
                } else {
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar("Error al guardar: ${response.code()}"))
                }
            } catch (e: Exception) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar(e.message ?: "Error de red"))
            } finally {
                _state.update { it.copy(isSavingCapital = false) }
            }
        }
    }

    // (Tu función 'deleteAccount' se queda igual)
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getStoredSession().firstOrNull()
                if (session == null) {
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar("No hay sesión activa"))
                    return@launch
                }
                val token = "Bearer ${session.idToken}"
                val request = DeleteAccountRequest(confirm = "DELETE")

                val response = authApiService.deleteAccount(token, request)

                if (response.isSuccessful) {
                    sessionRepository.clearSession()
                    _uiEvent.send(ProfileUiEvent.NavigateToLogin)
                } else {
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar("Error al eliminar: ${response.code()}"))
                }
            } catch (e: Exception) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar(e.message ?: "Error de red"))
            }
        }
    }
}