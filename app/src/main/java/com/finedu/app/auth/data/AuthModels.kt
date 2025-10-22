package com.finedu.app.auth.data

import com.google.gson.annotations.SerializedName

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

// Usuario en la respuesta
data class User(
    @SerializedName("uid")
    val uid: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("displayName")
    val displayName: String?,

    @SerializedName("emailVerified")
    val emailVerified: Boolean?
)

// Respuesta de Login
data class LoginResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: User?,

    @SerializedName("error")
    val error: String?
)

// Respuesta de Registro
data class RegisterResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: User?,

    @SerializedName("error")
    val error: String?
)