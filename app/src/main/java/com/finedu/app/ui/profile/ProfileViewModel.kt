package com.finedu.app.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.CapitalSettingsRequest
import com.finedu.app.auth.data.ChangePasswordRequest
import com.finedu.app.auth.data.DeleteAccountRequest
import com.finedu.app.auth.data.ErrorResponse
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData
import com.google.gson.Gson
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
    data class ShowForceLogoutDialog(val title: String, val message: String) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authApiService: AuthApiService
) : ViewModel() {

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


    // --- NUEVA FUNCIÓN: CAMBIAR CONTRASEÑA ---
    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            // Validaciones locales
            if (newPassword.length < 8 || newPassword.length > 20) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("La contraseña debe tener entre 8 y 20 caracteres"))
                return@launch
            }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("No hay sesión activa"))
                return@launch
            }
            val token = "Bearer ${session.idToken}"

            try {
                val request = ChangePasswordRequest(newPassword = newPassword)
                val response = authApiService.changePassword(token, request)

                if (response.isSuccessful && response.body() != null) {
                    sessionRepository.clearSession()
                    _uiEvent.send(ProfileUiEvent.ShowForceLogoutDialog(
                        title = "Contraseña Actualizada",
                        message = "Tu contraseña se ha cambiado correctamente. Por favor, inicia sesión nuevamente."
                    ))

                } else {
                    val errorBody = response.errorBody()?.string()
                    var msg = "Error: ${response.code()}"
                    var forceLogout = false

                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val err = gson.fromJson(errorBody, ErrorResponse::class.java)
                            val detail = err.error?.detail

                            if (detail == "CREDENTIAL_TOO_OLD_LOGIN_AGAIN") {
                                forceLogout = true
                            } else {
                                msg = err.error?.message ?: msg
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileVM", "Error parseando error: ${e.message}")
                        }
                    }
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar(msg))
                    if (forceLogout) {
                        sessionRepository.clearSession()
                        // Enviamos el evento para mostrar el POP-UP de Error
                        _uiEvent.send(ProfileUiEvent.ShowForceLogoutDialog(
                            title = "Sesión Expirada",
                            message = "Por tu seguridad, ha pasado mucho tiempo desde tu último inicio de sesión. Debes ingresar nuevamente."
                        ))
                    } else {
                        _uiEvent.send(ProfileUiEvent.ShowSnackbar(msg))
                    }
                }
            } catch (e: Exception) {
                // --- MANEJO DE ERRORES DE RED (EL CATCH ROBUSTO) ---
                val errorMessage = when {
                    e is java.net.UnknownHostException -> "Sin conexión a internet. Revisa tu red."
                    e is java.net.SocketTimeoutException -> "El servidor tardó demasiado en responder."
                    e.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet."
                    else -> "Error inesperado: ${e.localizedMessage}"
                }
                _uiEvent.send(ProfileUiEvent.ShowSnackbar(errorMessage))
                Log.e("ProfileViewModel", "Excepción en changePassword", e)
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