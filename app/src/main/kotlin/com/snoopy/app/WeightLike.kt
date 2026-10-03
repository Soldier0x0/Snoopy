package com.snoopy.app

object WeightLike {
    private val numberPattern = Regex("""\d+(?:\.\d+)?""")

    fun fromTexts(texts: Iterable<String>): List<String> {
        val found = linkedSetOf<String>()
        for (text in texts) {
            val lower = text.lowercase()
            if (isBodyFatLine(lower)) continue
            for (match in numberPattern.findAll(text)) {
                val value = match.value.toDoubleOrNull() ?: continue
                if (isWeightLike(value, lower)) {
                    found.add(trimNumber(match.value))
                }
            }
        }
        return found.toList()
    }

    fun fromLogLines(lines: List<LogLine>): List<String> {
        val texts = lines.map { SessionLog.readableText(it.bytes) }.filter { it.isNotEmpty() }
        return fromTexts(texts)
    }

    private fun isBodyFatLine(lower: String): Boolean =
        lower.contains("body fat") ||
            lower.contains("bodyfat") ||
            lower.contains("fat rate") ||
            lower.contains("fat%") ||
            lower.contains("bmi")

    private fun isWeightLike(value: Double, lower: String): Boolean {
        val prefersLb = lower.contains("lb") || lower.contains("pound")
        val prefersKg = lower.contains("kg") || lower.contains("kilo")
        val inKg = value in 20.0..250.0
        val inLb = value in 44.0..550.0
        return when {
            prefersLb -> inLb
            prefersKg -> inKg
            inKg -> true
            inLb -> true
            else -> false
        }
    }

    private fun trimNumber(raw: String): String {
        val value = raw.toDoubleOrNull() ?: return raw
        return if (value % 1.0 == 0.0) value.toLong().toString() else raw.trimEnd('0').trimEnd('.')
    }
}
