package com.finedu.app.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(name: String, email: String, password: String) {
        // Validaciones
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                error = "Por favor completa todos los campos"
            )
            return
        }

        if (password.length < 6) {
            _state.value = _state.value.copy(
                error = "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }

        if (!email.contains("@")) {
            _state.value = _state.value.copy(
                error = "Por favor ingresa un email válido"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val request = RegisterRequest(
                    name = name,
                    email = email,
                    password = password
                )
                val response = authApiService.register(request)

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    // Verificar si hay un usuario en la respuesta
                    if (registerResponse?.user != null && registerResponse.error == null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        Log.d("RegisterViewModel", "✅ Registro exitoso: ${registerResponse.user.uid}")
                        Log.d("RegisterViewModel", "Usuario: ${registerResponse.user.displayName}")
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = registerResponse?.error ?: "Error al registrar usuario"
                        )
                        Log.e("RegisterViewModel", "❌ Error: ${registerResponse?.error}")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Email ya está registrado o datos inválidos"
                        500 -> "Error del servidor. Intenta más tarde"
                        else -> "Error del servidor: ${response.code()}"
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    Log.e("RegisterViewModel", "❌ Error HTTP: ${response.code()}")
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
                Log.e("RegisterViewModel", "❌ Excepción registro", e)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}