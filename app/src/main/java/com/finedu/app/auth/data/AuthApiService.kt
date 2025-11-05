package com.finedu.app.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("enviar-mensaje") // <-- ¡REEMPLAZA ESTO CON TU RUTA REAL! Esta pendiente
    suspend fun sendDictation(
        @Header("Authorization") token: String, // La cabecera con el token
        @Body request: DictationRequest        // El JSON { "mensaje": "..." }
    ): Response<DictationResponse>
}