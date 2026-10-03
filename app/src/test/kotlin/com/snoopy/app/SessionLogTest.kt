package com.snoopy.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SessionLogTest {
    @Test
    fun logFileMatchesPlanShape() {
        val started = Instant.parse("2026-10-03T19:51:02.100Z")
        val ended = Instant.parse("2026-10-03T19:51:18.400Z")
        val lines = listOf(
            LogLine(Instant.parse("2026-10-03T19:51:03.010Z"), Direction.IN, "Weight: 72.4".toByteArray()),
            LogLine(Instant.parse("2026-10-03T19:51:03.020Z"), Direction.OUT, byteArrayOf(0x01, 0x00)),
        )
        val file = SessionLog.formatFile(
            started = started,
            ended = ended,
            advertisedName = "Dr Trust",
            weightLike = listOf("72.4"),
            lines = lines,
        )
        assertTrue(file.startsWith("snoopy 1\n"))
        assertTrue(file.contains("started: 2026-10-03T19:51:02.100Z\n"))
        assertTrue(file.contains("ended: 2026-10-03T19:51:18.400Z\n"))
        assertTrue(file.contains("advertised_name: Dr Trust\n"))
        assertTrue(file.contains("weight_like: 72.4\n"))
        assertTrue(file.contains("time\tdirection\thex\ttext\n"))
        assertTrue(file.contains("2026-10-03T19:51:03.010Z\tin\t57 65 69 67 68 74 3a 20 37 32 2e 34\tWeight: 72.4\n"))
        assertTrue(file.contains("2026-10-03T19:51:03.020Z\tout\t01 00\t\n"))
    }

    @Test
    fun weightLikeNoneWhenEmpty() {
        val file = SessionLog.formatFile(
            started = Instant.EPOCH,
            ended = Instant.EPOCH,
            advertisedName = "Scale",
            weightLike = emptyList(),
            lines = emptyList(),
        )
        assertTrue(file.contains("weight_like: none\n"))
    }

    @Test
    fun displayLineIncludesDirectionHexAndText() {
        val line = LogLine(Instant.parse("2026-10-03T19:51:03.010Z"), Direction.IN, "Hi".toByteArray())
        val display = SessionLog.formatDisplayLine(line)
        assertEquals("2026-10-03T19:51:03.010Z  in  48 69  Hi", display)
    }
}
