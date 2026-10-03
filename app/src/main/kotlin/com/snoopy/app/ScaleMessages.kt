package com.snoopy.app

object ScaleMessages {
    const val COMPANION_APP = "Close the companion app and try again."
    const val UNSUPPORTED_MODEL = "This model cannot be recorded here."
    const val BLUETOOTH_OFF = "Turn Bluetooth on and try again."
    const val NO_SCALE = "No scale found nearby."

    private val companionLikeStatuses = setOf(8, 19, 62, 133, 257)

    fun userMessage(
        bluetoothEnabled: Boolean = true,
        scanTimedOut: Boolean = false,
        companionHintAlreadyShown: Boolean = false,
        status: Int = 0,
    ): String? {
        if (!bluetoothEnabled) return BLUETOOTH_OFF
        if (scanTimedOut) return NO_SCALE
        if (!isCompanionLike(status)) return null
        return if (companionHintAlreadyShown) UNSUPPORTED_MODEL else COMPANION_APP
    }

    fun isCompanionLike(status: Int): Boolean = companionLikeStatuses.contains(status)
}
