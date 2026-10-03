package com.snoopy.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightLikeTest {
    @Test
    fun findsKgWeightInReadableText() {
        val found = WeightLike.fromTexts(listOf("Weight: 72.4"))
        assertEquals(listOf("72.4"), found)
    }

    @Test
    fun findsLbWeightWhenMarked() {
        val found = WeightLike.fromTexts(listOf("Body: 160 lb"))
        assertEquals(listOf("160"), found)
    }

    @Test
    fun ignoresBinaryOnlyPayloads() {
        val found = WeightLike.fromTexts(listOf(""))
        assertTrue(found.isEmpty())
    }

    @Test
    fun ignoresOutOfRangeNumbers() {
        val found = WeightLike.fromTexts(listOf("count: 3", "Weight: 12.0"))
        assertTrue(found.isEmpty())
    }

    @Test
    fun ignoresBodyFatLabels() {
        val found = WeightLike.fromTexts(listOf("body fat: 24.5%", "fat rate 18.2"))
        assertTrue(found.isEmpty())
    }
}
