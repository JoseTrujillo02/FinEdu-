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
import org.json.JSONObject
import javax.inject.Inject

data class VoiceDictationState(
    val isLoading: Boolean = false
)

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
    val uiEvent = _uiEvent.receiveAsFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Validación de mensaje vacío
            if (message.trim().isEmpty()) {
                _uiEvent.send(UiEvent.Error("Por favor, ingresa un mensaje"))
                return@launch
            }

            // Pone la UI en modo "Cargando"
            _state.update { it.copy(isLoading = true) }

            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error("No hay sesión activa"))
                return@launch
            }

            val token = "Bearer ${session.idToken}"
            val request = DictationRequest(mensaje = message)

            try {
                val response = aiService.sendDictation(token, request)

                _state.update { it.copy(isLoading = false) }

                when {
                    response.isSuccessful -> {
                        _uiEvent.send(UiEvent.Success("¡Transacción registrada exitosamente!"))
                    }
                    response.code() == 400 -> {
                        // Intenta extraer el mensaje de error del backend
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            val jsonError = JSONObject(errorBody ?: "{}")
                            jsonError.optString("detail", "El mensaje contiene contenido inapropiado")
                        } catch (e: Exception) {
                            "El mensaje contiene lenguaje inapropiado o no permitido"
                        }
                        _uiEvent.send(UiEvent.Error(errorMessage))
                    }
                    response.code() == 401 -> {
                        _uiEvent.send(UiEvent.Error("Sesión expirada. Por favor, inicia sesión nuevamente"))
                    }
                    response.code() == 422 -> {
                        _uiEvent.send(UiEvent.Error("El formato del mensaje no es válido"))
                    }
                    response.code() == 500 -> {
                        _uiEvent.send(UiEvent.Error("Error en el servidor. Intenta nuevamente más tarde"))
                    }
                    response.code() in 502..504 -> {
                        _uiEvent.send(UiEvent.Error("Servicio no disponible temporalmente"))
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            val jsonError = JSONObject(errorBody ?: "{}")
                            jsonError.optString("detail", "Error desconocido (${response.code()})")
                        } catch (e: Exception) {
                            "Error al procesar la transacción (código: ${response.code()})"
                        }
                        _uiEvent.send(UiEvent.Error(errorMessage))
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error("Sin conexión a internet. Verifica tu red"))
            } catch (e: java.net.SocketTimeoutException) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error("Tiempo de espera agotado. Intenta nuevamente"))
            } catch (e: javax.net.ssl.SSLException) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error("Error de seguridad en la conexión"))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(UiEvent.Error(e.message ?: "Error de conexión desconocido"))
            }
        }
    }
}