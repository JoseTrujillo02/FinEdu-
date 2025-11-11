package com.finedu.app.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.ErrorResponse
import com.finedu.app.auth.data.LoginRequest // <-- 1. Importa el LoginRequest
import com.finedu.app.auth.data.RegisterRequest
import com.finedu.app.data.SessionRepository // <-- 2. Importa la "caja fuerte"
import com.finedu.app.data.UserSessionData // <-- 3. Importa el paquete de sesión
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // (Asegúrate de tener este import)
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(name: String, email: String, password: String) {
        // (Tus validaciones se quedan igual, ¡están perfectas!)
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }
        if (password.length < 6) { /* ... */ }
        if (!email.contains("@")) { /* ... */ }


        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val regRequest = RegisterRequest(
                    name = name,
                    email = email,
                    password = password
                )
                val regResponse = authApiService.register(regRequest)

                if (!regResponse.isSuccessful || regResponse.body()?.user == null) {

                    val errorBody = regResponse.errorBody()?.string()
                    var errorMessage = "Error: ${regResponse.code()}" // Mensaje por defecto

                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                            errorMessage = when (errorResponse.error?.message) {
                                "EMAIL_EXISTS" -> "Este email ya está registrado."
                                "WEAK_PASSWORD" -> "La contraseña es muy débil (debe tener al menos 6 caracteres)."
                                "INVALID_EMAIL" -> "El formato del email es incorrecto."
                                // Añade más traducciones de Firebase aquí
                                else -> errorResponse.error?.message ?: "Error desconocido."
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterViewModel", "❌ No se pudo parsear el JSON de error", e)
                            errorMessage = "Error de respuesta del servidor."
                        }
                    }
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    Log.e("RegisterViewModel", "❌ Error HTTP: ${regResponse.code()} - $errorMessage")
                    return@launch
                }

                val logRequest = LoginRequest(email, password)
                val logResponse = authApiService.login(logRequest)
                val loginBody = logResponse.body()

                if (logResponse.isSuccessful && loginBody?.user != null && loginBody.tokens != null) {

                    val user = loginBody.user
                    val tokens = loginBody.tokens

                    val currentTime = System.currentTimeMillis()
                    val expiresAt = currentTime + (tokens.expiresIn * 1000L)

                    val sessionData = UserSessionData(
                        idToken = tokens.idToken,
                        refreshToken = tokens.refreshToken,
                        uid = user.uid,
                        email = user.email,
                        name = user.displayName,
                    )

                    sessionRepository.saveSession(sessionData)

                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    Log.d("RegisterViewModel", "✅ ¡Sesión automática guardada!")

                } else {
                    _state.update { it.copy(isLoading = false, error = "Registro exitoso, pero el auto-login falló. Intenta iniciar sesión manualmente.") }
                }

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                    else -> "Error: ${e.localizedMessage}"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                Log.e("RegisterViewModel", "❌ Excepción registro", e)
            }
        }
    }
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}