package com.finedu.app.ui.dictation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.DictationRequest
import com.finedu.app.auth.data.FinancialAiService
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

// 1. Define el estado de la pantalla
data class VoiceDictationState(
    val isLoading: Boolean = false
)

// 4. Define los eventos que la UI puede recibir
sealed class UiEvent {
    data class Success(val message: String) : UiEvent()
    data class Error(val message: String) : UiEvent()
}

@HiltViewModel
class VoiceDictationViewModel @Inject constructor(
    private val aiService: FinancialAiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceDictationState())
    val state = _state.asStateFlow()
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow() // La UI escuchará de aquí

    // 2. Esta es la función que llamará tu UI
    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Pone la UI en modo "Cargando"
            _state.update { it.copy(isLoading = true) }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error("No hay sesión activa")) // Envía evento de error
                return@launch
            }
            // Prepara el token "Bearer"
            val token = "Bearer ${session.idToken}"
            val request = DictationRequest(mensaje = message)
            try {
                val response = aiService.sendDictation(token, request)

                if (response.isSuccessful) {
                    _state.update { it.copy(isLoading = false) }
                    // Envía un evento de éxito
                    _uiEvent.send(UiEvent.Success("¡Enviado con éxito!"))
                } else {
                    _state.update { it.copy(isLoading = false) }
                    // Envía un evento de error
                    _uiEvent.send(UiEvent.Error("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                // Envía un evento de error
                _uiEvent.send(UiEvent.Error(e.message ?: "Error de red"))
            }
        }
    }

}