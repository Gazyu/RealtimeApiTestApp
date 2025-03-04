package com.gazyumaro.ai.realtimeapitestapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionQuery(
    @SerialName("model")
    val model: String,
    @SerialName("voice")
    val voice: String,
    @SerialName("instructions")
    val instructions: String,
)
