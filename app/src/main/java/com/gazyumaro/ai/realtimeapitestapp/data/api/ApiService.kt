package com.gazyumaro.ai.realtimeapitestapp.data.api

import com.gazyumaro.ai.realtimeapitestapp.data.model.SessionConfig
import com.gazyumaro.ai.realtimeapitestapp.data.model.SessionQuery
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("v1/realtime/sessions")
    @Headers(
        "Content-Type: application/json"
    )
    suspend fun getSession(
        @Header("Authorization") authorization: String,
        @Body sessionQuery: SessionQuery
    ): SessionConfig
}