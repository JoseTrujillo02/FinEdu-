package com.finedu.app.auth.data

import com.google.gson.annotations.SerializedName

// --- REQUESTS (Estos estaban bien) ---

// Request para Login
data class LoginRequest(
    val email: String,
    val password: String
)

// Request para Registro
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)


// --- RESPONSES (Aquí está la corrección) ---

// Respuesta de Login (¡CORREGIDA!)
// Esto coincide con tu JSON: { "user": {...}, "tokens": {...} }
data class LoginResponse(
    @SerializedName("user") val user: User? = null,
    @SerializedName("tokens") val tokens: Tokens? = null, // <-- ¡LA PARTE QUE FALTABA!
    @SerializedName("error") val error: String? = null
)

// Objeto "user" (¡CORREGIDO!)
// (Lo hacemos no-nulable para que sea más seguro)
data class User(
    @SerializedName("uid") val uid: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("emailVerified") val emailVerified: Boolean = false
)

// Objeto "tokens" (¡NUEVO!)
data class Tokens(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long
)

// Respuesta de Registro (Asumimos que es similar, pero puede que no devuelva tokens)
data class RegisterResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("error") val error: String? = null
)