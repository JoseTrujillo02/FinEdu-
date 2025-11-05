package com.finedu.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * La "Caja Fuerte" que guarda la sesión del usuario.
 * @Singleton significa que Hilt creará UNA SOLA copia de esta clase
 * para toda la aplicación.
 */
@Singleton
class SessionRepository @Inject constructor() {

    // Un Flow que "guarda" la sesión en memoria.
    // (En una app real, esto debería leer/escribir de DataStore o SharedPreferences,
    // pero para empezar, esto funciona perfectamente).
    private val _session = MutableStateFlow<UserSessionData?>(null)

    /**
     * Llamado por LoginViewModel cuando el login es exitoso.
     * Guarda los datos de la sesión.
     */
    fun saveSession(sessionData: UserSessionData) {
        _session.value = sessionData
    }

    /**
     * Llamado por ProfileViewModel (y otros) para obtener los datos.
     * Devuelve un Flow para que la UI se actualice si la sesión cambia.
     */
    fun getStoredSession(): Flow<UserSessionData?> {
        return _session
    }

    /**
     * Llamado por el 'onLogoutClick' en AppNavegacion para borrar la sesión.
     */
    fun clearSession() {
        _session.value = null
    }
}