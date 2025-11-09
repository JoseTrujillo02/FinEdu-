package com.finedu.app.auth.data // o com.finedu.app.ai.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FinancialAiService {
    @POST("/clasificar_gasto")
    suspend fun sendDictation(
        @Header("Authorization") token: String,
        @Body request: DictationRequest
    ): Response<DictationResponse>
}