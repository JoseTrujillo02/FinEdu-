package com.finedu.app.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                error = "Por favor completa todos los campos"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val request = LoginRequest(email = email, password = password)
                val response = authApiService.login(request)

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    // Verificar si hay un usuario en la respuesta
                    if (loginResponse?.user != null && loginResponse.tokens != null && loginResponse.error == null) {

                        // 1. Extraemos los datos del response
                        val user = loginResponse.user
                        val tokens = loginResponse.tokens

                        // 2. Creamos nuestro paquete de sesión (Paso 2)
                        val sessionData = UserSessionData(
                            idToken = tokens.idToken,
                            refreshToken = tokens.refreshToken,
                            uid = user.uid,
                            email = user.email,
                            name = user.displayName
                        )

                        // 3. ¡Guardamos en la caja fuerte!
                        sessionRepository.saveSession(sessionData)

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        Log.d("LoginViewModel", "✅ Login exitoso y sesión guardada")

                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = loginResponse?.error ?: "Credenciales inválidas"
                        )
                        Log.e("LoginViewModel", "❌ Error: ${loginResponse?.error}")
                    }
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error del servidor: ${response.code()}"
                    )
                    Log.e("LoginViewModel", "❌ Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet"
                    e.message?.contains("timeout") == true ->
                        "Tiempo de espera agotado. Intenta de nuevo"
                    else ->
                        "Error: ${e.localizedMessage}"
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                Log.e("LoginViewModel", "❌ Excepción login", e)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}