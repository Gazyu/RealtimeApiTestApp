package com.gazyumaro.ai.realtimeapitestapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionConfig(
    @SerialName("id")
    val id: String,

    @SerialName("object")
    val objectType: String,

    @SerialName("model")
    val model: String,

    @SerialName("modalities")
    val modalities: List<String>,

    @SerialName("instructions")
    val instructions: String,

    @SerialName("voice")
    val voice: String,

    @SerialName("input_audio_format")
    val inputAudioFormat: String,

    @SerialName("output_audio_format")
    val outputAudioFormat: String,


    @SerialName("tool_choice")
    val toolChoice: String,

    @SerialName("temperature")
    val temperature: Double,

    @SerialName("max_response_output_tokens")
    val maxResponseOutputTokens: String,

    @SerialName("client_secret")
    val clientSecret: ClientSecret
)


@Serializable
data class InputAudioTranscription(
    @SerialName("model")
    val model: String
)

@Serializable
data class ClientSecret(
    @SerialName("value")
    val value: String,

    @SerialName("expires_at")
    val expiresAt: Long
)

