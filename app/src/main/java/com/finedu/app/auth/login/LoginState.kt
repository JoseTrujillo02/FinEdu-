package com.finedu.app.auth.login

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

// Modelos para errores de validación
data class ValidationError(
    val field: String,
    val message: String
)

data class ErrorResponseWithValidation(
    val error: ErrorDetail?
)

data class ErrorDetail(
    val code: String?,
    val message: String?,
    val fields: List<ValidationError>?
)