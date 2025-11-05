package com.finedu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finedu.app.data.SessionRepository // <-- 1. Importa la "caja fuerte"
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    sessionRepository: SessionRepository // <-- 2. Hilt inyecta la "caja fuerte"
) : ViewModel() {

    val state: StateFlow<ProfileState> = sessionRepository.getStoredSession()
        .map { userData ->
            // Si hay una sesión guardada...
            if (userData != null) {
                ProfileState(
                    name = userData.name,
                    email = userData.email
                )
            } else {
                // Si no hay sesión (algo salió mal)...
                ProfileState(name = "Error", email = "Error")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileState() // Muestra "Cargando..." al inicio
        )
}