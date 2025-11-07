package com.finedu.app.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.LoginRequest // <-- 1. Importa el LoginRequest
import com.finedu.app.auth.data.RegisterRequest
import com.finedu.app.data.SessionRepository // <-- 2. Importa la "caja fuerte"
import com.finedu.app.data.UserSessionData // <-- 3. Importa el paquete de sesión
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // (Asegúrate de tener este import)
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApiService: AuthApiService, // <-- 4. COMA CORREGIDA
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
                // --- 5. CORRECCIÓN LÓGICA (Auto-Login) ---

                // PASO 1: REGISTRAR
                val regRequest = RegisterRequest(
                    name = name, // <-- 6. CORRECCIÓN DE CAMPO (name, no displayName)
                    email = email,
                    password = password
                )
                val regResponse = authApiService.register(regRequest)

                if (!regResponse.isSuccessful || regResponse.body()?.user == null) {
                    val errorMessage = when (regResponse.code()) {
                        400 -> "Email ya está registrado o datos inválidos"
                        else -> regResponse.body()?.error ?: "Error: ${regResponse.code()}"
                    }
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    return@launch // Detiene si el registro falla
                }

                Log.d("RegisterViewModel", "✅ Registro exitoso. Iniciando sesión automáticamente...")

                // PASO 2: INICIAR SESIÓN (AUTO-LOGIN)
                val logRequest = LoginRequest(email, password)
                val logResponse = authApiService.login(logRequest)
                val loginBody = logResponse.body()

                if (logResponse.isSuccessful && loginBody?.user != null && loginBody.tokens != null) {

                    // PASO 3: GUARDAR SESIÓN
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

                    // ¡Guardamos en la caja fuerte!
                    sessionRepository.saveSession(sessionData)

                    // PASO 4: MARCAR ÉXITO
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    Log.d("RegisterViewModel", "✅ ¡Sesión automática guardada!")

                } else {
                    // El registro funcionó, pero el auto-login falló.
                    _state.update { it.copy(isLoading = false, error = "Registro exitoso, pero el auto-login falló. Intenta iniciar sesión manualmente.") }
                }

            } catch (e: Exception) {
                // (Tu manejo de excepciones se queda igual, ¡está perfecto!)
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