package com.gazyumaro.ai.realtimeapitestapp.data.repository

import com.gazyumaro.ai.realtimeapitestapp.data.api.ApiService
import com.gazyumaro.ai.realtimeapitestapp.data.model.SessionConfig
import com.gazyumaro.ai.realtimeapitestapp.data.model.SessionQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getSession(apiKey: String, model: String, voice: String, instructions: String): Result<SessionConfig> {
        return try {
            val sessionQuery = SessionQuery(model, voice, instructions)
            val result = apiService.getSession("Bearer $apiKey", sessionQuery)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}