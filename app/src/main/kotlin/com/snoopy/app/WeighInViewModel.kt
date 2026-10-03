package com.snoopy.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ScreenPhase {
    READY,
    RECORDING,
    DONE,
}

data class UiState(
    val phase: ScreenPhase = ScreenPhase.READY,
    val connectionState: ConnectionStateLabel? = null,
    val deviceName: String = "",
    val logLines: List<String> = emptyList(),
    val userMessage: String? = null,
    val weightLike: List<String> = emptyList(),
    val sharePath: String? = null,
    val startedAt: Instant? = null,
)

class WeighInViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = SessionStorage(application)
    private var runner: BleWeighInRunner? = null
    private var sessionLines = mutableListOf<LogLine>()
    private var sessionStarted: Instant? = null
    private var advertisedName = ""

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onRecordTapped() {
        when (_state.value.phase) {
            ScreenPhase.READY -> beginRecording()
            ScreenPhase.RECORDING -> finishRecording()
            ScreenPhase.DONE -> beginRecording()
        }
    }

    private fun beginRecording() {
        sessionLines.clear()
        sessionStarted = Instant.now()
        advertisedName = ""
        _state.update {
            UiState(
                phase = ScreenPhase.RECORDING,
                connectionState = ConnectionStateLabel.LOOKING,
                logLines = emptyList(),
                userMessage = null,
                startedAt = sessionStarted,
            )
        }
        val context = getApplication<Application>()
        runner = BleWeighInRunner(context, listener).also { it.start() }
    }

    private fun finishRecording() {
        runner?.stop()
        runner = null
    }

    private val listener = object : BleWeighInListener {
        override fun onConnectionState(state: ConnectionStateLabel, deviceName: String?) {
            _state.update {
                it.copy(
                    connectionState = state,
                    deviceName = deviceName ?: it.deviceName,
                )
            }
            if (deviceName != null) {
                advertisedName = deviceName
            }
        }

        override fun onLogLine(line: LogLine) {
            sessionLines += line
            _state.update {
                it.copy(logLines = it.logLines + SessionLog.formatDisplayLine(line))
            }
        }

        override fun onUserMessage(message: String?) {
            _state.update { it.copy(userMessage = message) }
            if (message == ScaleMessages.UNSUPPORTED_MODEL) {
                finishRecording()
            }
        }

        override fun onFinished(name: String, lines: List<LogLine>) {
            val ended = Instant.now()
            val started = sessionStarted ?: ended
            val weights = WeightLike.fromLogLines(lines)
            val content = SessionLog.formatFile(
                started = started,
                ended = ended,
                advertisedName = name,
                weightLike = weights,
                lines = lines,
            )
            val file = storage.saveSession(content, ended)
            _state.update {
                UiState(
                    phase = ScreenPhase.DONE,
                    deviceName = name,
                    weightLike = weights,
                    sharePath = file.absolutePath,
                    userMessage = it.userMessage,
                )
            }
        }
    }

    fun shareIntentAction(context: Context): android.content.Intent? {
        val path = _state.value.sharePath ?: return null
        val file = java.io.File(path)
        if (!file.exists()) return null
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
