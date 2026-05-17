package com.titipin.app.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun formatDeadlineDisplay_withYear_formatsIndonesianDateTime() {
        val result = formatDeadlineDisplay("2026-05-17T18:52:13.123456", includeYear = true)

        assertEquals("17 Mei 2026, 18:52", result)
    }

    @Test
    fun formatDeadlineDisplay_withoutYear_formatsIndonesianDateTime() {
        val result = formatDeadlineDisplay("2026-05-17T18:52:13", includeYear = false)

        assertEquals("17 Mei, 18:52", result)
    }

    @Test
    fun formatDateDisplay_formatsDateOnly() {
        val result = formatDateDisplay("2026-05-17T18:52:13", includeYear = true)

        assertEquals("17 Mei 2026", result)
    }

    @Test
    fun formatTimeDisplay_formatsHourAndMinute() {
        val result = formatTimeDisplay("2026-05-17T18:52:13")

        assertEquals("18:52", result)
    }

    @Test
    fun formatDeadlineDisplay_invalidInput_returnsReadableFallback() {
        val result = formatDeadlineDisplay("2026-05-17 18:52:13", includeYear = true)

        assertEquals("2026-05-17 18:52", result)
    }
}
