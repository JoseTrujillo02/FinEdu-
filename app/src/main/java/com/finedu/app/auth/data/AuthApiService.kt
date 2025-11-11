package com.finedu.app.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    @GET("api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = 20
    ): Response<TransactionsResponse>

    @PUT("api/settings/capital")
    suspend fun updateCapitalSettings(
        @Header("Authorization") token: String,
        @Body request: CapitalSettingsRequest
    ): Response<Unit>

    // --- ¡AQUÍ ESTÁ LA NUEVA FUNCIÓN GET! ---
    @GET("api/settings/capital") // <-- 3. Usa @GET en la misma URL
    suspend fun getCapitalSettings(
        @Header("Authorization") token: String // <-- 4. Solo necesita el token
    ): Response<CapitalSettingsRequest>
}