package com.district37.toastmasters.util

import com.district37.toastmasters.models.LocalizedTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateTimeFormatterTest {

    private fun createLocalizedTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timezone: String = "America/New_York"
    ): LocalizedTime {
        val localDateTime = LocalDateTime(year, month, day, hour, minute)
        val tz = TimeZone.of(timezone)
        val instant = localDateTime.toInstant(tz)
        return LocalizedTime(instant, timezone)
    }

    // formatDate tests

    @Test
    fun `formatDate returns correct format for LocalizedTime`() {
        val localizedTime = createLocalizedTime(2025, 11, 15, 14, 30)

        val result = DateTimeFormatter.formatDate(localizedTime)

        assertEquals("Nov 15, 2025", result)
    }

    @Test
    fun `formatDate handles single digit day`() {
        val localizedTime = createLocalizedTime(2025, 1, 5, 10, 0)

        val result = DateTimeFormatter.formatDate(localizedTime)

        assertEquals("Jan 5, 2025", result)
    }

    // formatTime tests

    @Test
    fun `formatTime converts midnight hour 0 to 12 AM`() {
        val localizedTime = createLocalizedTime(2025, 1, 1, 0, 30)

        val result = DateTimeFormatter.formatTime(localizedTime)

        assertEquals("12:30 AM", result)
    }

    @Test
    fun `formatTime converts noon hour 12 to 12 PM`() {
        val localizedTime = createLocalizedTime(2025, 1, 1, 12, 0)

        val result = DateTimeFormatter.formatTime(localizedTime)

        assertEquals("12:00 PM", result)
    }

    @Test
    fun `formatTime converts morning hours correctly`() {
        val localizedTime = createLocalizedTime(2025, 1, 1, 9, 15)

        val result = DateTimeFormatter.formatTime(localizedTime)

        assertEquals("9:15 AM", result)
    }

    @Test
    fun `formatTime converts afternoon hours correctly`() {
        val localizedTime = createLocalizedTime(2025, 1, 1, 16, 45)

        val result = DateTimeFormatter.formatTime(localizedTime)

        assertEquals("4:45 PM", result)
    }

    @Test
    fun `formatTime pads minutes with leading zero`() {
        val localizedTime = createLocalizedTime(2025, 1, 1, 10, 5)

        val result = DateTimeFormatter.formatTime(localizedTime)

        assertEquals("10:05 AM", result)
    }

    // formatTimeRange tests

    @Test
    fun `formatTimeRange returns empty string for null start`() {
        val result = DateTimeFormatter.formatTimeRange(null as LocalizedTime?, null)

        assertEquals("", result)
    }

    @Test
    fun `formatTimeRange returns only start when end is null`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)

        val result = DateTimeFormatter.formatTimeRange(start, null)

        assertEquals("2:00 PM", result)
    }

    @Test
    fun `formatTimeRange returns range when both times provided`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)
        val end = createLocalizedTime(2025, 1, 1, 18, 30)

        val result = DateTimeFormatter.formatTimeRange(start, end)

        assertEquals("2:00 PM - 6:30 PM", result)
    }

    // formatDateRange tests

    @Test
    fun `formatDateRange shows single date for same day`() {
        val start = createLocalizedTime(2025, 10, 15, 10, 0)
        val end = createLocalizedTime(2025, 10, 15, 18, 0)

        val result = DateTimeFormatter.formatDateRange(start, end)

        assertEquals("Oct 15", result)
    }

    @Test
    fun `formatDateRange shows range for different days`() {
        val start = createLocalizedTime(2025, 10, 15, 10, 0)
        val end = createLocalizedTime(2025, 10, 17, 18, 0)

        val result = DateTimeFormatter.formatDateRange(start, end)

        assertEquals("Oct 15 - Oct 17", result)
    }

    @Test
    fun `formatDateRange returns empty for null start`() {
        val result = DateTimeFormatter.formatDateRange(null as LocalizedTime?, null)

        assertEquals("", result)
    }

    @Test
    fun `formatDateRange returns start date when end is null`() {
        val start = createLocalizedTime(2025, 10, 15, 10, 0)

        val result = DateTimeFormatter.formatDateRange(start, null)

        assertEquals("Oct 15", result)
    }

    // formatDateRangeWithYear tests

    @Test
    fun `formatDateRangeWithYear shows year at end for same year`() {
        val start = createLocalizedTime(2025, 10, 15, 10, 0)
        val end = createLocalizedTime(2025, 10, 20, 18, 0)

        val result = DateTimeFormatter.formatDateRangeWithYear(start, end)

        assertEquals("Oct 15 - Oct 20, 2025", result)
    }

    @Test
    fun `formatDateRangeWithYear shows both years for different years`() {
        val start = createLocalizedTime(2025, 12, 28, 10, 0)
        val end = createLocalizedTime(2026, 1, 3, 18, 0)

        val result = DateTimeFormatter.formatDateRangeWithYear(start, end)

        assertEquals("Dec 28, 2025 - Jan 3, 2026", result)
    }

    @Test
    fun `formatDateRangeWithYear shows single date with year for same day`() {
        val start = createLocalizedTime(2025, 7, 4, 10, 0)
        val end = createLocalizedTime(2025, 7, 4, 22, 0)

        val result = DateTimeFormatter.formatDateRangeWithYear(start, end)

        assertEquals("Jul 4, 2025", result)
    }

    @Test
    fun `formatDateRange with showYear true delegates to formatDateRangeWithYear`() {
        val start = createLocalizedTime(2025, 10, 15, 10, 0)
        val end = createLocalizedTime(2025, 10, 20, 18, 0)

        val result = DateTimeFormatter.formatDateRange(start, end, showYear = true)

        assertEquals("Oct 15 - Oct 20, 2025", result)
    }

    // formatDuration tests

    @Test
    fun `formatDuration returns null for null start`() {
        val end = createLocalizedTime(2025, 1, 1, 18, 0)

        val result = DateTimeFormatter.formatDuration(null as LocalizedTime?, end)

        assertNull(result)
    }

    @Test
    fun `formatDuration returns null for null end`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)

        val result = DateTimeFormatter.formatDuration(start, null)

        assertNull(result)
    }

    @Test
    fun `formatDuration formats hours and minutes`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)
        val end = createLocalizedTime(2025, 1, 1, 15, 30)

        val result = DateTimeFormatter.formatDuration(start, end)

        assertEquals("1h 30m", result)
    }

    @Test
    fun `formatDuration formats hours only when no extra minutes`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)
        val end = createLocalizedTime(2025, 1, 1, 16, 0)

        val result = DateTimeFormatter.formatDuration(start, end)

        assertEquals("2h", result)
    }

    @Test
    fun `formatDuration formats minutes only when under 1 hour`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)
        val end = createLocalizedTime(2025, 1, 1, 14, 45)

        val result = DateTimeFormatter.formatDuration(start, end)

        assertEquals("45m", result)
    }

    @Test
    fun `formatDuration returns null for zero or negative duration`() {
        val start = createLocalizedTime(2025, 1, 1, 14, 0)
        val end = createLocalizedTime(2025, 1, 1, 14, 0)

        val result = DateTimeFormatter.formatDuration(start, end)

        assertNull(result)
    }

    // formatDayHeader tests

    @Test
    fun `formatDayHeader returns correct format`() {
        val date = LocalDate(2025, 7, 26)

        val result = DateTimeFormatter.formatDayHeader(date)

        assertEquals("Saturday, July 26", result)
    }

    @Test
    fun `formatDayHeader handles different months and days`() {
        val date = LocalDate(2025, 1, 1)

        val result = DateTimeFormatter.formatDayHeader(date)

        assertEquals("Wednesday, January 1", result)
    }

    // formatSimplifiedDate tests

    @Test
    fun `formatSimplifiedDate returns month and day`() {
        val localizedTime = createLocalizedTime(2025, 10, 15, 12, 0)

        val result = DateTimeFormatter.formatSimplifiedDate(localizedTime)

        assertEquals("Oct 15", result)
    }

    // formatDateWithTime tests

    @Test
    fun `formatDateWithTime returns date and time combined`() {
        val localizedTime = createLocalizedTime(2025, 11, 15, 20, 0)

        val result = DateTimeFormatter.formatDateWithTime(localizedTime)

        assertEquals("Nov 15, 2025 8:00PM", result)
    }

    @Test
    fun `formatDateWithTime handles morning time`() {
        val localizedTime = createLocalizedTime(2025, 3, 5, 9, 30)

        val result = DateTimeFormatter.formatDateWithTime(localizedTime)

        assertEquals("Mar 5, 2025 9:30AM", result)
    }
}
