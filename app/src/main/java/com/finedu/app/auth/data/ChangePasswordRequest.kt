package com.finedu.app.auth.data

import com.google.gson.annotations.SerializedName

// Este es el request que se envía a la API
data class ChangePasswordRequest(
    @SerializedName("newPassword")
    val newPassword: String
)