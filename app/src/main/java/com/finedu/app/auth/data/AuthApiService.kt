package com.finedu.app.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    // ✅ NUEVO ENDPOINT: Obtener categorías únicas
    @GET("api/transactions/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): Response<CategoriesResponse>

    @PUT("api/settings/capital")
    suspend fun updateCapitalSettings(
        @Header("Authorization") token: String,
        @Body request: CapitalSettingsRequest
    ): Response<Unit>

    @GET("api/settings/capital")
    suspend fun getCapitalSettings(
        @Header("Authorization") token: String
    ): Response<CapitalSettingsRequest>

    @HTTP(method = "DELETE", path = "api/account/me", hasBody = true)
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body request: DeleteAccountRequest
    ): Response<Unit>

    @DELETE("api/transactions/{transactionId}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): Response<Unit>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequest
    ): Response<LoginResponse>
}

// ✅ NUEVA DATA CLASS: Respuesta de categorías
data class CategoriesResponse(
    val categories: List<String>
)