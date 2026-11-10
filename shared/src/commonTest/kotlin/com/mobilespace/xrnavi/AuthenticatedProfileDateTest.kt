package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticatedProfileDateTest {
    @Test
    fun formatsDateAndTimestampAsDayMonthYear() {
        assertEquals("03.02.2000", formatAuthenticatedProfileDate("2000-02-03"))
        assertEquals(
            "01.01.2026",
            formatAuthenticatedProfileDate("2026-01-01T00:00:00+00:00"),
        )
    }

    @Test
    fun validatesCalendarDatesAndKeepsMissingValuesExplicit() {
        assertEquals("29.02.2024", formatAuthenticatedProfileDate("2024-02-29"))
        assertEquals("Не вказано", formatAuthenticatedProfileDate("2023-02-29"))
        assertEquals("Не вказано", formatAuthenticatedProfileDate("not-a-date"))
        assertEquals("Не вказано", formatAuthenticatedProfileDate("Не вказано"))
    }
}
