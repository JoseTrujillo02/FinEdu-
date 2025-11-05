package com.finedu.app.ui.dictation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.DictationRequest
import com.finedu.app.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define el estado de la pantalla
data class VoiceDictationState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VoiceDictationViewModel @Inject constructor(
    private val apiService: AuthApiService,      // Inyecta el "tubo"
    private val sessionRepository: SessionRepository // Inyecta la "caja fuerte"
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceDictationState())
    val state = _state.asStateFlow()

    // 2. Esta es la función que llamará tu UI
    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Pone la UI en modo "Cargando"
            _state.update { it.copy(isLoading = true, error = null) }

            // 3. ¡Obtiene el token de la "caja fuerte"!
            val session = sessionRepository.getStoredSession().firstOrNull()
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "No hay sesión activa") }
                return@launch
            }

            // Prepara el token "Bearer"
            val token = "Bearer ${session.idToken}"
            val request = DictationRequest(mensaje = message)

            try {
                // 4. Llama a la API
                val response = apiService.sendDictation(token, request)

                if (response.isSuccessful) {
                    // ¡Éxito!
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    // Error del servidor
                    _state.update { it.copy(isLoading = false, error = "Error: ${response.code()}") }
                }
            } catch (e: Exception) {
                // Error de red (sin internet, etc.)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}