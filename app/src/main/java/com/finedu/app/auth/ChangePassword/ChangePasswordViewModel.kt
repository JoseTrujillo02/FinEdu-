package com.finedu.app.auth.changepassword

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.ChangePasswordRequest
import com.finedu.app.auth.login.ErrorResponseWithValidation
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    // ✅ Usar ChangePasswordState en lugar de ChangePasswordRequest
    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun changePassword(newPassword: String) {
        // Limpiar errores previos
        _state.value = _state.value.copy(
            error = null,
            passwordError = null
        )

        // Validación básica local
        if (newPassword.isBlank()) {
            _state.value = _state.value.copy(
                passwordError = "La contraseña no puede estar vacía"
            )
            return
        }

        if (newPassword.length < 8 || newPassword.length > 20) {
            _state.value = _state.value.copy(
                passwordError = "Tu contraseña debe tener entre 8 y 20 caracteres."
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // Obtener el idToken actual desde DataStore
                val sessionData = sessionRepository.getStoredSession().first()
                val idToken = sessionData?.idToken

                if (idToken.isNullOrBlank()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Sesión no válida. Por favor inicia sesión nuevamente."
                    )
                    return@launch
                }

                // ✅ Crear el request correcto solo con newPassword
                val request = ChangePasswordRequest(newPassword = newPassword)
                val response = authApiService.changePassword(
                    authorization = "Bearer $idToken",
                    request = request
                )

                if (response.isSuccessful) {
                    val changePasswordResponse = response.body()

                    if (changePasswordResponse?.user != null &&
                        changePasswordResponse.tokens != null &&
                        changePasswordResponse.error == null) {

                        val user = changePasswordResponse.user
                        val tokens = changePasswordResponse.tokens

                        // Actualizar tokens en el almacenamiento
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
                        Log.d("ChangePasswordVM", "✅ Contraseña cambiada y tokens actualizados")
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = changePasswordResponse?.error ?: "Error al cambiar contraseña"
                        )
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
                Log.e("ChangePasswordVM", "❌ Excepción al cambiar contraseña", e)
            }
        }
    }

    private fun handleErrorResponse(errorBody: String?, statusCode: Int) {
        var generalError: String? = null
        var passwordError: String? = null

        when (statusCode) {
            401 -> {
                generalError = "Sesión expirada. Por favor inicia sesión nuevamente."
            }
            422 -> {
                if (errorBody != null) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponseWithValidation::class.java)

                        if (errorResponse.error?.code == "VALIDATION_ERROR" &&
                            !errorResponse.error.fields.isNullOrEmpty()) {

                            errorResponse.error.fields.forEach { fieldError ->
                                when (fieldError.field) {
                                    "newPassword" -> passwordError = fieldError.message
                                    else -> generalError = fieldError.message
                                }
                            }
                        } else {
                            generalError = errorResponse.error?.message ?: "Error de validación"
                        }
                    } catch (e: Exception) {
                        Log.e("ChangePasswordVM", "❌ No se pudo parsear el JSON de error", e)
                        generalError = "Error de validación"
                    }
                } else {
                    generalError = "Error de validación"
                }
            }
            500 -> {
                generalError = "Error del servidor. Inténtalo más tarde."
            }
            else -> {
                generalError = "Error: $statusCode"
            }
        }

        _state.value = _state.value.copy(
            isLoading = false,
            error = generalError,
            passwordError = passwordError
        )
        Log.e("ChangePasswordVM", "❌ Error HTTP: $statusCode - General: $generalError, Password: $passwordError")
    }

    fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            passwordError = null
        )
    }

    fun clearPasswordError() {
        _state.value = _state.value.copy(passwordError = null)
    }
}