package com.finedu.app.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.ErrorResponse
import com.finedu.app.auth.data.LoginRequest
import com.finedu.app.auth.data.RegisterRequest
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        // Validaciones básicas del cliente (para mejor UX)
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _state.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        // Validación: Contraseñas deben coincidir
        if (password != confirmPassword) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        // Si pasan las validaciones básicas, enviamos al backend
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
                    var errorMessage = "Error: ${regResponse.code()}"

                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, ValidationErrorResponse::class.java)

                            // Manejo del nuevo formato de error del backend
                            if (errorResponse.error?.code == "VALIDATION_ERROR" &&
                                errorResponse.error.fields != null &&
                                errorResponse.error.fields.isNotEmpty()) {

                                // Construir mensaje con todos los errores de validación
                                val errorMessages = errorResponse.error.fields.mapNotNull { field ->
                                    when (field.field) {
                                        "email" -> "Email: ${field.message}"
                                        "password" -> "Contraseña: ${field.message}"
                                        "name" -> "Nombre: ${field.message}"
                                        else -> "${field.field}: ${field.message}"
                                    }
                                }

                                errorMessage = if (errorMessages.size == 1) {
                                    errorMessages.first()
                                } else {
                                    errorMessages.joinToString("\n• ", prefix = "Errores:\n• ")
                                }
                            } else {
                                // Fallback para otros tipos de error
                                errorMessage = when (errorResponse.error?.message) {
                                    "EMAIL_EXISTS" -> "Este email ya está registrado."
                                    "WEAK_PASSWORD" -> "La contraseña es muy débil."
                                    "INVALID_EMAIL" -> "El formato del email es incorrecto."
                                    else -> errorResponse.error?.message ?: "Error desconocido."
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterViewModel", "❌ No se pudo parsear el JSON de error", e)
                            Log.e("RegisterViewModel", "ErrorBody: $errorBody")
                            errorMessage = "Error de respuesta del servidor."
                        }
                    }
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    Log.e("RegisterViewModel", "❌ Error HTTP: ${regResponse.code()} - $errorMessage")
                    return@launch
                }

                // Registro exitoso, ahora auto-login
                val logRequest = LoginRequest(email, password)
                val logResponse = authApiService.login(logRequest)
                val loginBody = logResponse.body()

                if (logResponse.isSuccessful && loginBody?.user != null && loginBody.tokens != null) {
                    val user = loginBody.user
                    val tokens = loginBody.tokens

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
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Verifica tu conexión."
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

// Modelo para el nuevo formato de error del backend
data class ValidationErrorResponse(
    val error: ValidationError?
)

data class ValidationError(
    val code: String?,
    val message: String?,
    val fields: List<FieldError>?
)

data class FieldError(
    val field: String,
    val message: String
)