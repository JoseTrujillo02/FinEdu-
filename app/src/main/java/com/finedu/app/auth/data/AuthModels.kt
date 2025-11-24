package com.finedu.app.auth.data

import com.google.gson.annotations.SerializedName

// Request para Login
data class LoginRequest(
    val email: String,
    val password: String
)
// Request para Registro
data class RegisterRequest(
    @SerializedName("displayName") //
    val name: String,
    val email: String,
    val password: String
)
// Respuesta de Login
data class LoginResponse(
    @SerializedName("user") val user: User? = null,
    @SerializedName("tokens") val tokens: Tokens? = null,
    @SerializedName("error") val error: String? = null
)
// Usuario en la respuesta
data class User(
    @SerializedName("uid") val uid: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("emailVerified") val emailVerified: Boolean = false
)
// Tokens en la respuesta
data class Tokens(
    @SerializedName("idToken") val idToken: String = "",
    @SerializedName("refreshToken") val refreshToken: String = "",
    @SerializedName("expiresIn") val expiresIn: Long = 0L
)
// Respuesta de Registro
data class RegisterResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("error") val error: String? = null
)
// Request de Dictado
data class DictationRequest(
    @SerializedName("mensaje") val mensaje: String
)
// Respuesta de Dictado
data class DictationResponse(
    @SerializedName("type") val type: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("category") val category: String = "",
    @SerializedName("descripcion") val description: String? = null, // Ahora es nullable
    @SerializedName("date") val date: String = "",

    // Campos adicionales para manejar errores del backend
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("success") val success: Boolean? = null
)
// Respuesta de Transacciones
data class TransactionsResponse(
    @SerializedName("items") val items: List<TransactionItem> = emptyList(),
    @SerializedName("nextCursor") val nextCursor: String? = null
)
// Un item de Transacción
data class TransactionItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("category") val category: String = "",
    @SerializedName("descripcion") val description: String? = null,
    @SerializedName("date") val date: String = "",
    @SerializedName("remainingCapital") val remainingCapital: Double? = null
)
// Respuesta de Error
data class ErrorResponse(
    @SerializedName("error") val error: ErrorBody? = null
)
// Cuerpo del Error
data class ErrorBody(
    @SerializedName("code") val code: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("detail") val detail: String? = null // <-- ¡AÑADE ESTO!
)
// Request de Ajustes de Capital
data class CapitalSettingsRequest(
    @SerializedName("amount") val amount: Double = 0.0, // <-- ¡CORREGIDO!
    @SerializedName("periodicity") val periodicity: String = "" // <-- ¡CORREGIDO!
)
// Request de Eliminar Cuenta
data class DeleteAccountRequest(
    @SerializedName("confirm") val confirm: String = "" // <-- ¡CORREGIDO!
)

data class ChangePasswordRequest(
    @SerializedName("newPassword") val newPassword: String
)