package com.finedu.app.auth.login

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)
data class ErrorBody(
    val code: String,
    val message: String,
    val detail: String? = null
)