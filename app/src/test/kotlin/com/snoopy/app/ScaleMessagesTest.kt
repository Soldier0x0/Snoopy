package com.snoopy.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScaleMessagesTest {
    @Test
    fun companionMessageComesFirst() {
        assertEquals(
            ScaleMessages.COMPANION_APP,
            ScaleMessages.userMessage(companionHintAlreadyShown = false, status = 133),
        )
    }

    @Test
    fun unsupportedModelAfterCompanionHint() {
        assertEquals(
            ScaleMessages.UNSUPPORTED_MODEL,
            ScaleMessages.userMessage(companionHintAlreadyShown = true, status = 133),
        )
    }

    @Test
    fun plainBluetoothFailuresStayShort() {
        assertEquals(ScaleMessages.BLUETOOTH_OFF, ScaleMessages.userMessage(bluetoothEnabled = false))
        assertEquals(ScaleMessages.NO_SCALE, ScaleMessages.userMessage(scanTimedOut = true))
    }

    @Test
    fun messagesNeverMentionDeveloperOptionsOrHci() {
        val banned = listOf("developer", "hci", "computer")
        val all = listOf(
            ScaleMessages.COMPANION_APP,
            ScaleMessages.UNSUPPORTED_MODEL,
            ScaleMessages.BLUETOOTH_OFF,
            ScaleMessages.NO_SCALE,
        )
        for (message in all) {
            val lower = message.lowercase()
            for (word in banned) {
                org.junit.Assert.assertFalse(lower.contains(word))
            }
        }
    }
}
