package com.titipin.app.shared

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Helper — konvert ISO datetime string ke label relatif
// "2026-03-10T16:55:11" → "5 menit lalu", "2 jam lalu", "1 hari lalu", dll
fun timeAgo(isoDateTime: String): String {
    return try {
        // BE return format: "2026-03-10T16:55:11.123456" — trim microseconds
        val cleaned = isoDateTime.substringBefore(".")
        val dateTime = LocalDateTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours   = ChronoUnit.HOURS.between(dateTime, now)
        val days    = ChronoUnit.DAYS.between(dateTime, now)

        when {
            minutes < 1  -> "Baru saja"
            minutes < 60 -> "${minutes}m lalu"
            hours < 24   -> "${hours} jam lalu"
            days < 7     -> "${days} hari lalu"
            else         -> "${days / 7} minggu lalu"
        }
    } catch (_: Exception) {
        "Baru"
    }
}

fun formatDeadlineDisplay(isoDateTime: String, includeYear: Boolean): String {
    return try {
        val cleaned = isoDateTime.substringBefore(".")
        val dateTime = LocalDateTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val locale = Locale("id", "ID")
        val pattern = if (includeYear) "dd MMM yyyy, HH:mm" else "dd MMM, HH:mm"
        dateTime.format(DateTimeFormatter.ofPattern(pattern, locale))
    } catch (_: Exception) {
        val fallback = isoDateTime.substringBefore(".").replace("T", " ")
        if (includeYear) fallback.take(16) else fallback.drop(5).take(11)
    }
}

fun formatDateDisplay(isoDateTime: String, includeYear: Boolean): String {
    return try {
        val cleaned = isoDateTime.substringBefore(".")
        val dateTime = LocalDateTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val locale = Locale("id", "ID")
        val pattern = if (includeYear) "dd MMM yyyy" else "dd MMM"
        dateTime.format(DateTimeFormatter.ofPattern(pattern, locale))
    } catch (_: Exception) {
        isoDateTime.substringBefore("T")
    }
}

fun formatTimeDisplay(isoDateTime: String): String {
    return try {
        val cleaned = isoDateTime.substringBefore(".")
        val dateTime = LocalDateTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        isoDateTime.substringAfter("T").take(5)
    }
}
