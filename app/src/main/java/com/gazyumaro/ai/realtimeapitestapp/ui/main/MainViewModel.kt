package com.gazyumaro.ai.realtimeapitestapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gazyumaro.ai.realtimeapitestapp.data.repository.SessionRepository
import com.gazyumaro.ai.realtimeapitestapp.webrtc.WebRtcEventListener
import com.gazyumaro.ai.realtimeapitestapp.webrtc.WebRtcManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val webRtcManager: WebRtcManager
) : ViewModel(), WebRtcEventListener {
    private val _state = MutableStateFlow<MainState>(MainState.Initial)
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events = _events.asSharedFlow()


    fun onConnectButtonClicked(token: String) {
        if (token.isBlank()) {
            _state.value = MainState.Error("トークンが入力されていません")
            return
        }

        viewModelScope.launch {
            runCatching {
                webRtcManager.initialize(this@MainViewModel)
              getEphemeralKey(token)
            }

        }

    }

    fun onDisconnectButtonClicked(){
        viewModelScope.launch {
            webRtcManager.dispose()
            _state.value = MainState.Disconnected
        }
    }


    private suspend fun getEphemeralKey(token: String) = withContext(Dispatchers.IO){
        _state.value = MainState.Loading

        try {
            val result = sessionRepository.getSession(
                token,
                "gpt-4o-realtime-preview-2024-12-17",
                "verse",
                "日本語で会話をしてください。"
            )

            result.onSuccess { config ->
                _state.value = MainState.TokenReceived(config.clientSecret.value)
                initRtc(config.clientSecret.value)
            }.onFailure { error ->
                _state.value = MainState.Error("トークン取得エラー: ${error.message}")
            }
        } catch (e: Exception) {
            _state.value = MainState.Error("予期せぬエラー: ${e.message}")
        }
    }

    private fun initRtc(token: String) {
        viewModelScope.launch {
            try {
                webRtcManager.setupConnection()
                webRtcManager.createOffer { offer ->
                    sendOfferToOpenAI(token, offer)
                }
            } catch (e: Exception) {
                _state.value = MainState.Error("WebRTC初期化エラー: ${e.message}")
            }
        }
    }

    private fun sendOfferToOpenAI(token: String, offer: String) {
        viewModelScope.launch {
            _state.value = MainState.SendingOffer

            try {
                val response = withContext(Dispatchers.IO) {
                    val url =
                        "https://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview-2024-12-17"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/sdp")
                        .post(offer.toByteArray().toRequestBody())
                        .build()

                    webRtcManager.getHttpClient().newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val answerSdp = response.body?.string() ?: ""
                    webRtcManager.setRemoteDescription(answerSdp)
                    _state.value = MainState.Connected
                } else {
                    _state.value = MainState.Error("OpenAIからの応答エラー: ${response.code}")
                }
            } catch (e: Exception) {
                _state.value = MainState.Error("オファー送信エラー: ${e.message}")
            }
        }
    }

    // WebRtcEventListener実装
    override fun onDataChannelMessage(message: String) {
        viewModelScope.launch {
            _events.emit(MainEvent.MessageReceived(message))
        }
    }

    override fun onConnectionStateChange(state: PeerConnection.PeerConnectionState) {
        viewModelScope.launch {
            when (state) {
                PeerConnection.PeerConnectionState.CONNECTED -> {
                    _state.value = MainState.Connected
                }

                PeerConnection.PeerConnectionState.DISCONNECTED,
                PeerConnection.PeerConnectionState.FAILED,
                PeerConnection.PeerConnectionState.CLOSED -> {
                    _state.value = MainState.Disconnected
                }

                else -> {}
            }
        }
    }

    override fun onError(error: String) {
        viewModelScope.launch {
            _state.value = MainState.Error(error)
        }
    }

    override fun onCleared() {
        super.onCleared()
        webRtcManager.dispose()
    }
}

sealed class MainState {
    object Initial : MainState()
    object Loading : MainState()
    data class TokenReceived(val token: String) : MainState()
    object SendingOffer : MainState()
    object Connected : MainState()
    object Disconnected : MainState()
    data class Error(val message: String) : MainState()
}

sealed class MainEvent {
    data class MessageReceived(val message: String) : MainEvent()
}