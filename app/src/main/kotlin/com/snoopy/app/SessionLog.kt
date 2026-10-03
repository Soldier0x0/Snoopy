package com.snoopy.app

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class Direction {
    IN,
    OUT,
}

data class LogLine(
    val instant: Instant,
    val direction: Direction,
    val bytes: ByteArray,
)

object SessionLog {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)

    fun formatFile(
        started: Instant,
        ended: Instant,
        advertisedName: String,
        weightLike: List<String>,
        lines: List<LogLine>,
    ): String {
        val header = buildString {
            appendLine("snoopy 1")
            appendLine("started: ${formatter.format(started)}")
            appendLine("ended: ${formatter.format(ended)}")
            appendLine("advertised_name: $advertisedName")
            appendLine("weight_like: ${weightLike.joinToString(",").ifEmpty { "none" }}")
            appendLine()
            appendLine("time\tdirection\thex\ttext")
        }
        return header + lines.joinToString("\n", postfix = if (lines.isEmpty()) "" else "\n") { formatFileLine(it) }
    }

    fun formatFileLine(line: LogLine): String {
        val text = readableText(line.bytes)
        return listOf(
            formatter.format(line.instant),
            line.direction.name.lowercase(),
            toHex(line.bytes),
            text,
        ).joinToString("\t")
    }

    fun formatDisplayLine(line: LogLine): String {
        val text = readableText(line.bytes)
        return "${formatter.format(line.instant)}  ${line.direction.name.lowercase()}  ${toHex(line.bytes)}  $text"
    }

    fun readableText(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        if (bytes.any { it < 0x09 || (it > 0x0d && it < 0x20) || it == 0x7f }) {
            return ""
        }
        return bytes.toString(Charsets.UTF_8)
    }

    fun toHex(bytes: ByteArray): String =
        bytes.joinToString(" ") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
