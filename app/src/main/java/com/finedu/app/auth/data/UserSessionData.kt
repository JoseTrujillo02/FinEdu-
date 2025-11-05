package com.finedu.app.data

/**
 * Representa la sesión guardada en la "caja fuerte".
 * Contiene los datos que necesitamos para futuras llamadas a la API
 * y para mostrar en el perfil.
 */
data class UserSessionData(
    val idToken: String,      // El token principal para la API
    val refreshToken: String, // Para renovar el token cuando expire
    val uid: String,          // El ID único del usuario
    val email: String,
    val name: String
)