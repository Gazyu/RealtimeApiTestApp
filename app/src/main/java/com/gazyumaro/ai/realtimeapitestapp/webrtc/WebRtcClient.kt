package com.gazyumaro.ai.realtimeapitestapp.webrtc

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject


class WebRtcClient @Inject constructor(@ApplicationContext private val context: Context) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var localAudioTrack: AudioTrack? = null

    fun initialize() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
    }

    fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
        return peerConnection
    }

    fun createAudioTrack(): AudioTrack? {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("localAudio", audioSource)
        localAudioTrack?.setEnabled(true)
        return localAudioTrack
    }

    fun addAudioTrack() {
        localAudioTrack?.let { peerConnection?.addTrack(it) }
    }

    fun createDataChannel(): DataChannel? {
        dataChannel = peerConnection?.createDataChannel("oai-events", DataChannel.Init())
        return dataChannel
    }

    fun createOffer(sdpObserver: SdpObserver) {
        peerConnection?.createOffer(sdpObserver, MediaConstraints())
    }

    fun setLocalDescription(sdpObserver: SdpObserver, sessionDescription: SessionDescription) {
        peerConnection?.setLocalDescription(sdpObserver, sessionDescription)
    }

    fun setRemoteDescription(sdpObserver: SdpObserver, sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(sdpObserver, sessionDescription)
    }

    @Synchronized
    fun dispose() {
        dataChannel?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
        dataChannel = null
        peerConnection = null
        peerConnectionFactory = null
    }
}