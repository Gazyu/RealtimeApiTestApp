package com.gazyumaro.ai.realtimeapitestapp.ui.main

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gazyumaro.ai.realtimeapitestapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private val tokenText: EditText by lazy {
        findViewById(R.id.editText)
    }
    private val connectButton: AppCompatButton by lazy {
        findViewById(R.id.connect)
    }
    private val disconnectButton: AppCompatButton by lazy {
        findViewById(R.id.disconnect)
    }
    private val logTextView: TextView by lazy {
        findViewById(R.id.logTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtonListeners()

        viewModel.state.onEach { state ->
            when (state) {
                is MainState.Connected -> {
                    addLog("接続完了")
                }

                is MainState.Disconnected -> {
                    addLog("接続終了")
                    connectButton.isEnabled = true
                }

                is MainState.Error -> {
                    addLog(state.message)
                    connectButton.isEnabled = true
                }

                is MainState.Initial -> {}
                is MainState.Loading -> {}
                is MainState.SendingOffer -> {}
                is MainState.TokenReceived -> {

                }
            }

        }.launchIn(lifecycleScope)

        viewModel.events.onEach { event ->
            when (event) {
                is MainEvent.MessageReceived -> addLog(event.message)
            }

        }.launchIn(lifecycleScope)
    }

    override fun onPause() {
        viewModel.onDisconnectButtonClicked()
        connectButton.isEnabled = true
        super.onPause()
    }

    private fun setupButtonListeners() {
        connectButton.setOnClickListener {
            connectButton.isEnabled = false
            checkAndRequestPermissions {
                viewModel.onConnectButtonClicked(tokenText.text.toString())
            }

        }

        disconnectButton.setOnClickListener {
            viewModel.onDisconnectButtonClicked()
            connectButton.isEnabled = true
        }

    }

    @SuppressLint("SetTextI18n")
    private fun addLog(message: String) {
        logTextView.text = "$message \n ${logTextView.text}"
    }

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO
    )

    private var onPermissionGranted: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            onPermissionGranted?.invoke()
        }
    }

    private fun checkAndRequestPermissions(onGranted: () -> Unit) {
        this.onPermissionGranted = onGranted

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            onGranted()
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}