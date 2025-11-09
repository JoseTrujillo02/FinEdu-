package com.finedu.app.data

/**
 * Representa la sesión guardada en la "caja fuerte".
 * Contiene los datos que necesitamos para futuras llamadas a la API
 * y para mostrar en el perfil.
 */
data class UserSessionData(
    val idToken: String,
    val refreshToken: String,
    val uid: String,
    val email: String,
    val name: String
)