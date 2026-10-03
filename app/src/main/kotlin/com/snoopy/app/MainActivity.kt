package com.snoopy.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val viewModel: WeighInViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermissions()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.state.collectAsState()
                    SnoopyScreen(
                        state = state,
                        onRecord = { viewModel.onRecordTapped() },
                        onShare = {
                            viewModel.shareIntentAction(this)?.let { startActivity(Intent.createChooser(it, "Share session")) }
                        },
                    )
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                needed += Manifest.permission.BLUETOOTH_SCAN
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                needed += Manifest.permission.BLUETOOTH_CONNECT
            }
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter? = manager.adapter
        if (adapter != null && !adapter.isEnabled) {
            @Suppress("DEPRECATION")
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}

private fun connectionLabel(state: ConnectionStateLabel?): String =
    when (state) {
        ConnectionStateLabel.LOOKING -> "Looking"
        ConnectionStateLabel.CONNECTING -> "Connecting"
        ConnectionStateLabel.CONNECTED -> "Connected"
        ConnectionStateLabel.FAILED -> "Failed"
        null -> "Looking"
    }

@Composable
private fun SnoopyScreen(
    state: UiState,
    onRecord: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        when (state.phase) {
            ScreenPhase.READY -> {
                Text("Stand on the scale, then tap Record.")
            }
            ScreenPhase.RECORDING -> {
                Text("Device: ${state.deviceName.ifBlank { "—" }}")
                Text("Connection: ${connectionLabel(state.connectionState)}")
                state.userMessage?.let { Text(it) }
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.logLines) { line ->
                        Text(line, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            ScreenPhase.DONE -> {
                Text("Scale: ${state.deviceName.ifBlank { "—" }}")
                val weights = state.weightLike
                Text(
                    if (weights.isEmpty()) {
                        "No weight-like number found."
                    } else {
                        "Weight-like: ${weights.joinToString(", ")}"
                    },
                )
                state.userMessage?.let { Text(it) }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                    Text("Share session file")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRecord, modifier = Modifier.fillMaxWidth()) {
            Text(
                when (state.phase) {
                    ScreenPhase.RECORDING -> "Finish recording"
                    else -> "Record"
                },
            )
        }
    }
}
