package com.finedu.app.ui.dictation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.DictationRequest
import com.finedu.app.auth.data.DictationResponse
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
                        val body = response.body()

                        // Verificar si la respuesta contiene un error aunque el código sea 200
                        if (body != null) {
                            val errorMessage = checkForErrorsInResponse(body)

                            if (errorMessage != null) {
                                // Hay un error en la respuesta
                                _uiEvent.send(UiEvent.Error(errorMessage))
                            } else {
                                // Éxito real
                                _uiEvent.send(UiEvent.Success("¡Transacción registrada exitosamente!"))
                            }
                        } else {
                            // Respuesta vacía
                            _uiEvent.send(UiEvent.Error("Respuesta vacía del servidor"))
                        }
                    }
                    response.code() == 400 -> {
                        val errorMessage = extractErrorMessage(response.errorBody()?.string())
                            ?: "El mensaje contiene contenido inapropiado o no es válido"
                        _uiEvent.send(UiEvent.Error(errorMessage))
                    }
                    response.code() == 401 -> {
                        _uiEvent.send(UiEvent.Error("Sesión expirada. Por favor, inicia sesión nuevamente"))
                    }
                    response.code() == 422 -> {
                        val errorMessage = extractErrorMessage(response.errorBody()?.string())
                            ?: "El formato del mensaje no es válido"
                        _uiEvent.send(UiEvent.Error(errorMessage))
                    }
                    response.code() == 500 -> {
                        _uiEvent.send(UiEvent.Error("Error en el servidor. Intenta nuevamente más tarde"))
                    }
                    response.code() in 502..504 -> {
                        _uiEvent.send(UiEvent.Error("Servicio no disponible temporalmente"))
                    }
                    else -> {
                        val errorMessage = extractErrorMessage(response.errorBody()?.string())
                            ?: "Error al procesar la transacción (código: ${response.code()})"
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

    /**
     * Verifica si la respuesta exitosa (200) contiene errores en el body
     */
    private fun checkForErrorsInResponse(response: DictationResponse): String? {
        val description = response.description ?: ""

        // Verificar campos vacíos o inválidos que indican error
        return when {
            // Si type está vacío o es "error"
            response.type.isEmpty() || response.type.equals("error", ignoreCase = true) -> {
                description.ifEmpty {
                    "No se pudo procesar la transacción. Intenta ser más específico"
                }
            }

            // Si la descripción contiene palabras clave de error
            description.contains("fondos insuficientes", ignoreCase = true) ||
                    description.contains("no tienes suficiente", ignoreCase = true) ||
                    description.contains("saldo insuficiente", ignoreCase = true) ||
                    description.contains("no hay suficiente", ignoreCase = true) ||
                    description.contains("excede", ignoreCase = true) ||
                    description.contains("supera tu capital", ignoreCase = true) -> {
                "No tienes suficiente dinero disponible para realizar esta transacción. Verifica tu capital actual."
            }

            // Si menciona capital no configurado
            description.contains("capital no configurado", ignoreCase = true) ||
                    description.contains("configura tu capital", ignoreCase = true) -> {
                "Debes configurar tu capital inicial en tu perfil antes de registrar transacciones."
            }

            // Si el mensaje no tiene relación con finanzas
            description.contains("no relacionado", ignoreCase = true) ||
                    description.contains("no tiene sentido", ignoreCase = true) ||
                    description.contains("no se entiende", ignoreCase = true) ||
                    description.contains("no es claro", ignoreCase = true) -> {
                "El mensaje no parece estar relacionado con una transacción financiera. Intenta dictar algo como: 'Gasté 200 pesos en comida'"
            }

            // Si amount es 0 o negativo y no es un ingreso válido
            response.amount <= 0.0 -> {
                "No se pudo identificar un monto válido. Por favor, menciona claramente la cantidad"
            }

            // Si category está vacía
            response.category.isEmpty() -> {
                "No se pudo identificar la categoría. Menciona en qué gastaste o de dónde proviene el ingreso"
            }

            // Si hay un campo "error" en la descripción (tu backend podría incluirlo)
            description.startsWith("Error:", ignoreCase = true) ||
                    description.startsWith("ERROR:", ignoreCase = true) -> {
                description.removePrefix("Error:").removePrefix("ERROR:").trim()
            }

            // Todo está bien
            else -> null
        }
    }

    /**
     * Extrae el mensaje de error del errorBody
     */
    private fun extractErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null

        return try {
            val jsonError = JSONObject(errorBody)

            // Intentar diferentes formatos de error
            when {
                jsonError.has("detail") -> jsonError.getString("detail")
                jsonError.has("message") -> jsonError.getString("message")
                jsonError.has("error") -> {
                    val error = jsonError.get("error")
                    if (error is String) error else jsonError.getString("error")
                }
                jsonError.has("descripcion") -> jsonError.getString("descripcion")
                else -> null
            }
        } catch (e: Exception) {
            // Si no se puede parsear como JSON, devolver el body completo si es razonable
            if (errorBody.length < 200) errorBody else null
        }
    }
}