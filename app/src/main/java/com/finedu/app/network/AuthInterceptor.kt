package com.finedu.app.network

import com.finedu.app.data.SessionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            runBlocking { sessionRepository.clearSession() }
            SessionExpiredManager.notifySessionExpired()
        }
        return response
    }
}

object SessionExpiredManager {
    private var listener: (() -> Unit)? = null
    fun setListener(listener: () -> Unit) { this.listener = listener }
    fun notifySessionExpired() { listener?.invoke() }
    fun clearListener() { listener = null }
}