package com.finedu.app.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>


    @GET("api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,

        // Filtros opcionales
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = 20
    ): Response<TransactionsResponse>


}