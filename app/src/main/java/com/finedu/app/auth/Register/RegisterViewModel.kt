package com.finedu.app.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                // Crear usuario en Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    // Actualizar el perfil con el nombre
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()

                    // Guardar información adicional en Firestore
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .await()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    Log.d("RegisterViewModel", "✅ Registro exitoso para: $email")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("email address is already") == true ->
                        "Este email ya está registrado"
                    e.message?.contains("network") == true ->
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("password") == true ->
                        "La contraseña no cumple con los requisitos"
                    else ->
                        "Error al registrar: ${e.localizedMessage}"
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                Log.e("RegisterViewModel", "❌ Error registro", e)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}