package com.finedu.app.auth.changepassword

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val passwordError: String? = null
)