package com.gazyumaro.ai.realtimeapitestapp.webrtc

import android.util.Log
import okhttp3.OkHttpClient
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject

class WebRtcManager @Inject constructor(
    private val webRtcClient: WebRtcClient,
    private val okHttpClient: OkHttpClient
) {
    private var eventListener: WebRtcEventListener? = null
    private var isInitialized = false
    private var pendingOffer: String? = null

    /**
     * WebRTCクライアントを初期化し、イベントリスナーを設定する
     */
    fun initialize(listener: WebRtcEventListener) {
        if (isInitialized) return

        this.eventListener = listener
        webRtcClient.initialize()
        isInitialized = true

        Log.d(TAG, "WebRTC initialized")
    }

    /**
     * WebRTC接続をセットアップする
     * PeerConnectionを作成し、オーディオトラックとデータチャネルを追加する
     */
    fun setupConnection() {
        if (!isInitialized) {
            throw IllegalStateException("WebRtcManager must be initialized before setting up connection")
        }

        // PeerConnection.Observerを実装したWebRtcObserverを作成
        val observer = createPeerConnectionObserver()

        // PeerConnectionを作成
        webRtcClient.createPeerConnection(observer)

        // オーディオトラックを作成して追加
        webRtcClient.createAudioTrack()
        webRtcClient.addAudioTrack()

        // データチャネルを作成
        val dataChannel = webRtcClient.createDataChannel()
        dataChannel?.registerObserver(createDataChannelObserver())

        Log.d(TAG, "WebRTC connection setup completed")
    }

    /**
     * PeerConnection.Observerを作成する
     */
    private fun createPeerConnectionObserver(): PeerConnection.Observer {
        return object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                Log.d(TAG, "onIceCandidate: $candidate")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d(TAG, "onConnectionChange: $newState")
                newState?.let { eventListener?.onConnectionStateChange(it) }
            }

            override fun onDataChannel(dataChannel: DataChannel?) {
                Log.d(TAG, "onDataChannel")
                dataChannel?.registerObserver(createDataChannelObserver())
            }

            // 他のコールバックメソッドの実装
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                Log.d(TAG, "onIceCandidatesRemoved")
            }

            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "onAddStream")
            }

            override fun onRemoveStream(stream: MediaStream?) {
                Log.d(TAG, "onRemoveStream")
            }

            override fun onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded")
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "onSignalingChange: $state")
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "onIceConnectionChange: $state")
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {
                Log.d(TAG, "onIceConnectionReceivingChange: $receiving")
            }

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "onIceGatheringChange: $state")
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                Log.d(TAG, "onAddTrack")
                mediaStreams?.firstOrNull()?.audioTracks?.firstOrNull()?.setEnabled(true)
            }
        }
    }

    /**
     * DataChannel.Observerを作成する
     */
    private fun createDataChannelObserver(): DataChannel.Observer {
        return object : DataChannel.Observer {
            override fun onMessage(buffer: DataChannel.Buffer) {
                val msgBytes = ByteArray(buffer.data.remaining()).also { buffer.data.get(it) }
                val message = String(msgBytes, Charsets.UTF_8)
                Log.d(TAG, "DataChannel message: $message")
                eventListener?.onDataChannelMessage(message)
            }

            override fun onStateChange() {
                Log.d(TAG, "DataChannel state changed")
            }

            override fun onBufferedAmountChange(previousAmount: Long) {
                // バッファ量の変更は通常ログに記録する必要はない
            }
        }
    }

    /**
     * SDPオファーを作成する
     * @param onOfferCreated オファーが作成されたときに呼び出されるコールバック
     */
    fun createOffer(onOfferCreated: (String) -> Unit) {
        val sdpObserver = object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                Log.d(TAG, "Offer created successfully")
                Log.d(TAG, "Offer type: ${sessionDescription.type}")

                pendingOffer = sessionDescription.description

                webRtcClient.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                Log.d(TAG, "Local description set successfully")
                pendingOffer?.let { offer ->
                    onOfferCreated(offer)
                }
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Failed to create offer: $error")
                eventListener?.onError("オファー作成エラー: $error")
            }

            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set local description: $error")
                eventListener?.onError("ローカル記述設定エラー: $error")
            }
        }

        webRtcClient.createOffer(sdpObserver)
    }

    /**
     * リモート記述（SDP応答）を設定する
     * @param sdpAnswer SDP応答文字列
     */
    fun setRemoteDescription(sdpAnswer: String) {
        val sessionDescription = SessionDescription(SessionDescription.Type.ANSWER, sdpAnswer)

        val sdpObserver = object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully")
            }

            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description: $error")
                eventListener?.onError("リモート記述設定エラー: $error")
            }

            override fun onCreateSuccess(description: SessionDescription?) {
                Log.d(TAG, "Create success (unexpected for setRemoteDescription)")
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Create failure (unexpected for setRemoteDescription): $error")
            }
        }

        webRtcClient.setRemoteDescription(sdpObserver, sessionDescription)
    }

    /**
     * OkHttpClientを取得する
     * @return OkHttpClient
     */
    fun getHttpClient(): OkHttpClient = okHttpClient

    /**
     * リソースを解放する
     */
    fun dispose() {
        webRtcClient.dispose()
        eventListener = null
        isInitialized = false
        Log.d(TAG, "WebRTC resources disposed")
    }

    companion object{
        private const val TAG = "WebRtcManager"
    }
}

/**
 * WebRTCイベントリスナーインターフェース
 */
interface WebRtcEventListener {
    /**
     * データチャネルからメッセージを受信したときに呼び出される
     * @param message 受信したメッセージ
     */
    fun onDataChannelMessage(message: String)

    /**
     * 接続状態が変化したときに呼び出される
     * @param state 新しい接続状態
     */
    fun onConnectionStateChange(state: PeerConnection.PeerConnectionState)

    /**
     * エラーが発生したときに呼び出される
     * @param error エラーメッセージ
     */
    fun onError(error: String)
}
