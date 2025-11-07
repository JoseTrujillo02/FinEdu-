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
    @SerializedName("displayName") //
    val name: String,
    val email: String,
    val password: String
)


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

 // Para enviar el mensaje de la app
data class DictationRequest(
    @SerializedName("mensaje") val mensaje: String
)

// 2. El JSON que RECIBES este queda pendiente
data class DictationResponse(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)


//3. para el get de las colecciones de firebase transaccion

data class TransactionsResponse(
    @SerializedName("items") val items: List<TransactionItem> = emptyList(),
    @SerializedName("nextCursor") val nextCursor: String? = null
)
data class TransactionItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String, // "income" o "expense"
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String? = null, // Basado en tu Firestore
    @SerializedName("date") val date: String, // O puedes usar java.util.Date
    @SerializedName("remainingCapital") val remainingCapital: Double? = null
)