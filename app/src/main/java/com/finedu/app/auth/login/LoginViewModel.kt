package com.finedu.app.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.ErrorResponse
import com.finedu.app.auth.data.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData
import com.google.gson.Gson

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        // Limpiar errores previos
        _state.value = _state.value.copy(
            error = null,
            emailError = null,
            passwordError = null
        )

        // Validación básica
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                error = "Por favor completa todos los campos"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val request = LoginRequest(email = email, password = password)
                val response = authApiService.login(request)

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse?.user != null && loginResponse.tokens != null && loginResponse.error == null) {
                        val user = loginResponse.user
                        val tokens = loginResponse.tokens

                        val sessionData = UserSessionData(
                            idToken = tokens.idToken,
                            refreshToken = tokens.refreshToken,
                            uid = user.uid,
                            email = user.email,
                            name = user.displayName
                        )

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
                    handleErrorResponse(response.errorBody()?.string(), response.code())
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

    private fun handleErrorResponse(errorBody: String?, statusCode: Int) {
        var generalError: String? = null
        var emailError: String? = null
        var passwordError: String? = null

        if (errorBody != null) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponseWithValidation::class.java)

                // Manejar errores de validación por campo
                if (errorResponse.error?.code == "VALIDATION_ERROR" &&
                    !errorResponse.error.fields.isNullOrEmpty()) {

                    errorResponse.error.fields.forEach { fieldError ->
                        when (fieldError.field) {
                            "email" -> emailError = fieldError.message
                            "password" -> passwordError = fieldError.message
                            else -> generalError = fieldError.message
                        }
                    }

                    Log.e("LoginViewModel", "❌ Errores de validación: email=$emailError, password=$passwordError")
                } else {
                    // Manejar otros tipos de errores
                    generalError = when (errorResponse.error?.message ?: errorResponse.error?.code) {
                        "INVALID_LOGIN_CREDENTIALS" -> "Email o contraseña incorrectos."
                        "EMAIL_NOT_FOUND" -> "Este email no está registrado."
                        "INVALID_PASSWORD" -> "La contraseña es incorrecta."
                        "USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
                        "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Demasiados intentos. Intenta más tarde."
                        else -> errorResponse.error?.message ?: "Error desconocido."
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ No se pudo parsear el JSON de error", e)
                generalError = "Error de respuesta del servidor."
            }
        } else {
            generalError = "Error: $statusCode"
        }

        _state.value = _state.value.copy(
            isLoading = false,
            error = generalError,
            emailError = emailError,
            passwordError = passwordError
        )
        Log.e("LoginViewModel", "❌ Error HTTP: $statusCode - General: $generalError")
    }

    fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            emailError = null,
            passwordError = null
        )
    }

    fun clearFieldError(field: String) {
        when (field) {
            "email" -> _state.value = _state.value.copy(emailError = null)
            "password" -> _state.value = _state.value.copy(passwordError = null)
        }
    }
}