package com.finedu.app.auth.login
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
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
                auth.signInWithEmailAndPassword(email, password).await()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                Log.d("LoginViewModel", "✅ Login exitoso")
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al iniciar sesión: ${e.localizedMessage}"
                )
                Log.e("LoginViewModel", "❌ Error login", e)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}