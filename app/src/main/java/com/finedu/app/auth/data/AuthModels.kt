package com.finedu.app.auth.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)
data class RegisterRequest(
    @SerializedName("displayName") //
    val name: String,
    val email: String,
    val password: String
)
data class LoginResponse(
    @SerializedName("user") val user: User? = null,
    @SerializedName("tokens") val tokens: Tokens? = null,
    @SerializedName("error") val error: String? = null
)
data class User(
    @SerializedName("uid") val uid: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("emailVerified") val emailVerified: Boolean = false
)
data class Tokens(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long
)
data class RegisterResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("error") val error: String? = null
)
data class DictationRequest(
    @SerializedName("mensaje") val mensaje: String
)
data class DictationResponse(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("descripcion") val description: String,
    @SerializedName("date") val date: String
)
data class TransactionsResponse(
    @SerializedName("items") val items: List<TransactionItem> = emptyList(),
    @SerializedName("nextCursor") val nextCursor: String? = null
)
data class TransactionItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("descripcion") val description: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("remainingCapital") val remainingCapital: Double? = null
)
data class ErrorResponse(
    @SerializedName("error") val error: ErrorBody?
)
data class ErrorBody(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)
data class CapitalSettingsRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("periodicity") val periodicity: String
)