package com.snoopy.app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.time.Instant
import java.util.UUID

enum class ConnectionStateLabel {
    LOOKING,
    CONNECTING,
    CONNECTED,
    FAILED,
}

interface BleWeighInListener {
    fun onConnectionState(state: ConnectionStateLabel, deviceName: String?)
    fun onLogLine(line: LogLine)
    fun onUserMessage(message: String?)
    fun onFinished(advertisedName: String, lines: List<LogLine>)
}

@SuppressLint("MissingPermission")
class BleWeighInRunner(
    private val context: Context,
    private val listener: BleWeighInListener,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private var scanning = false
    private var gatt: BluetoothGatt? = null
    private var companionHintShown = false
    private var advertisedName = ""
    private val lines = mutableListOf<LogLine>()
    private var active = false

    fun start() {
        if (active) return
        active = true
        companionHintShown = false
        advertisedName = ""
        lines.clear()
        if (adapter == null || !adapter.isEnabled) {
            listener.onConnectionState(ConnectionStateLabel.FAILED, null)
            listener.onUserMessage(ScaleMessages.userMessage(bluetoothEnabled = false))
            active = false
            return
        }
        listener.onConnectionState(ConnectionStateLabel.LOOKING, null)
        listener.onUserMessage(null)
        startScan()
    }

    fun stop() {
        active = false
        stopScan()
        gatt?.close()
        gatt = null
        listener.onFinished(advertisedName.ifBlank { "unknown" }, lines.toList())
    }

    private fun startScan() {
        stopScan()
        scanning = true
        handler.postDelayed(scanTimeoutRunnable, SCAN_TIMEOUT_MS)
        adapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    private fun stopScan() {
        if (!scanning) return
        handler.removeCallbacks(scanTimeoutRunnable)
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanning = false
    }

    private val scanTimeoutRunnable = Runnable {
        if (!active) return@Runnable
        stopScan()
        listener.onConnectionState(ConnectionStateLabel.FAILED, null)
        listener.onUserMessage(ScaleMessages.userMessage(scanTimedOut = true))
        active = false
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!active || gatt != null) return
            val device = result.device ?: return
            val name = result.scanRecord?.deviceName ?: device.name
            advertisedName = name?.ifBlank { device.address } ?: device.address
            stopScan()
            connect(device)
        }

        override fun onScanFailed(errorCode: Int) {
            if (!active) return
            listener.onConnectionState(ConnectionStateLabel.FAILED, advertisedName.ifBlank { null })
            listener.onUserMessage(
                ScaleMessages.userMessage(
                    companionHintAlreadyShown = companionHintShown,
                    status = errorCode,
                ),
            )
            if (ScaleMessages.isCompanionLike(errorCode)) {
                companionHintShown = true
            }
            active = false
        }
    }

    private fun connect(device: BluetoothDevice) {
        listener.onConnectionState(ConnectionStateLabel.CONNECTING, advertisedName)
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (!active) return
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                listener.onConnectionState(ConnectionStateLabel.CONNECTED, advertisedName)
                listener.onUserMessage(null)
                gatt.discoverServices()
                return
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    handleFailure(status)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (!active || status != BluetoothGatt.GATT_SUCCESS) {
                handleFailure(status)
                return
            }
            for (service in gatt.services) {
                for (characteristic in service.characteristics) {
                    val props = characteristic.properties
                    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 ||
                        props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
                    ) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_UUID)
                        if (descriptor != null) {
                            val enable = if (props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            } else {
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                            descriptor.value = enable
                            recordWrite(descriptor.value ?: byteArrayOf())
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            if (!active) return
            recordIncoming(value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            recordIncoming(characteristic.value ?: byteArrayOf())
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (!active) return
            recordWrite(characteristic.value ?: byteArrayOf())
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            if (!active) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                handleFailure(status)
            }
        }
    }

    private fun recordIncoming(bytes: ByteArray) {
        val line = LogLine(Instant.now(), Direction.IN, bytes)
        lines += line
        listener.onLogLine(line)
    }

    private fun recordWrite(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val line = LogLine(Instant.now(), Direction.OUT, bytes)
        lines += line
        listener.onLogLine(line)
    }

    private fun handleFailure(status: Int) {
        listener.onConnectionState(ConnectionStateLabel.FAILED, advertisedName.ifBlank { null })
        val message = ScaleMessages.userMessage(
            companionHintAlreadyShown = companionHintShown,
            status = status,
        )
        listener.onUserMessage(message)
        if (ScaleMessages.isCompanionLike(status)) {
            companionHintShown = true
        }
        gatt?.close()
        gatt = null
        active = false
    }

    companion object {
        private const val SCAN_TIMEOUT_MS = 30_000L
        private val CLIENT_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
