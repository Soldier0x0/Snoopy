package com.snoopy.app

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

class SessionStorage(private val context: Context) {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)

    fun saveSession(content: String, ended: Instant): File {
        val dir = File(context.filesDir, "sessions").apply { mkdirs() }
        val file = File(dir, "snoopy-${formatter.format(ended)}.txt")
        file.writeText(content)
        return file
    }
}
