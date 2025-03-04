package com.gazyumaro.ai.realtimeapitestapp.data.api

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val format = Json { ignoreUnknownKeys = true }

    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(FixJsonContentTypeInterceptor())
            .build()
    }

    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(format.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    private class FixJsonContentTypeInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val oldContentType = original.header("Content-Type") ?: ""
            val newContentType = oldContentType.replace("; charset=utf-8", "")

            val fixed = original.newBuilder()
                .header("Content-Type", newContentType)
                .build()

            return chain.proceed(fixed)
        }
    }
}