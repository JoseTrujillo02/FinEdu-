package com.finedu.app.di

import com.finedu.app.auth.data.AuthApiService
import com.finedu.app.auth.data.FinancialAiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AUTH_BASE_URL = "https://api-firebase-auth.onrender.com/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL) // Esta es la URL de Auth
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }


    // --- 2. SECCIÓN NUEVA PARA LA API DE IA ---

    private const val IA_BASE_URL = "https://ia-financiera-fastapi.onrender.com/"

    @Provides
    @Singleton
    fun provideFinancialAiService(
        okHttpClient: OkHttpClient, // <-- ¡Reutiliza el mismo OkHttpClient!
        gson: Gson                  // <-- ¡Reutiliza el mismo Gson!
    ): FinancialAiService {
        // Crea una instancia de Retrofit "privada" solo para este servicio
        val retrofit = Retrofit.Builder()
            .baseUrl(IA_BASE_URL) // <-- Usa la NUEVA URL base
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(FinancialAiService::class.java)
    }

}
