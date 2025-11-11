package com.finedu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.CapitalSettingsRequest
import com.finedu.app.auth.data.DeleteAccountRequest
import com.finedu.app.data.SessionRepository
// import com.finedu.app.ui.dictation.UiEvent // <-- 1. IMPORT INCORRECTO (Eliminado)
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
// --- 2. IMPORTS QUE FALTABAN ---
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class ProfileUiEvent {
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
    object NavigateToLogin : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authApiService: AuthApiService
) : ViewModel() {


    val state: StateFlow<ProfileState> = sessionRepository.getStoredSession()
        .map { userData ->
            if (userData != null) {
                ProfileState(
                    name = userData.name,
                    email = userData.email
                )
            } else {
                ProfileState(name = "Error", email = "Error")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileState()
        )

    private val _uiEvent = Channel<ProfileUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun saveCapital(amountStr: String, periodicity: String) {
        viewModelScope.launch {
            val parsedAmount = amountStr
                .replace("$", "")
                .replace(",", "")
                .trim()
                .toDoubleOrNull()

            if (parsedAmount == null) {
                // --- 4. CORRECCIÓN DE EVENTO ---
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("Monto inválido"))
                return@launch
            }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                // --- 5. CORRECCIÓN DE EVENTO ---
                _uiEvent.send(ProfileUiEvent.ShowSnackbar("No hay sesión activa"))
                return@launch
            }
            val token = "Bearer ${session.idToken}"

            try {
                val request = CapitalSettingsRequest(
                    amount = parsedAmount,
                    periodicity = periodicity.lowercase()
                )
                val response = authApiService.updateCapitalSettings(token, request)

                if (response.isSuccessful) {
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar("¡Capital guardado!"))
                } else {
                    _uiEvent.send(ProfileUiEvent.ShowSnackbar("Error al guardar: ${response.code()}"))
                }
            } catch (e: Exception) {
                _uiEvent.send(ProfileUiEvent.ShowSnackbar(e.message ?: "Error de red"))
            }
        }
    }

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