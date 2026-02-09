package com.district37.toastmasters.util

import com.district37.toastmasters.models.LocalizedTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Centralized date and time formatting utilities for the app.
 *
 * All methods that accept LocalizedTime will use the embedded timezone for conversion,
 * ensuring times are displayed in the venue's local timezone rather than the device's timezone.
 */
object DateTimeFormatter {

    /**
     * Resolves a timezone string to a TimeZone object, falling back to system default if invalid
     */
    private fun resolveTimezone(timezoneStr: String?): TimeZone {
        return timezoneStr?.let {
            try {
                TimeZone.of(it)
            } catch (e: Exception) {
                TimeZone.currentSystemDefault()
            }
        } ?: TimeZone.currentSystemDefault()
    }

    /**
     * Formats a LocalizedTime to a date string like "Nov 15, 2025"
     */
    fun formatDate(localizedTime: LocalizedTime): String {
        val tz = resolveTimezone(localizedTime.timezone)
        val localDateTime = localizedTime.instant.toLocalDateTime(tz)
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    }

    /**
     * Formats an Instant to a date string like "Nov 15, 2025" using system timezone
     */
    fun formatDate(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    }

    /**
     * Formats a LocalizedTime to a time string with AM/PM like "4:00 PM"
     */
    fun formatTime(localizedTime: LocalizedTime): String {
        val tz = resolveTimezone(localizedTime.timezone)
        val localDateTime = localizedTime.instant.toLocalDateTime(tz)
        val hour = localDateTime.hour
        val minute = localDateTime.minute

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val minuteStr = minute.toString().padStart(2, '0')

        return "$hour12:$minuteStr $amPm"
    }

    /**
     * Formats an Instant to a time string with AM/PM like "4:00 PM" using system timezone
     */
    fun formatTime(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour
        val minute = localDateTime.minute

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val minuteStr = minute.toString().padStart(2, '0')

        return "$hour12:$minuteStr $amPm"
    }

    /**
     * Formats a time range like "4:00 PM - 11:00 PM" using LocalizedTime
     */
    fun formatTimeRange(startTime: LocalizedTime?, endTime: LocalizedTime?): String {
        if (startTime == null) return ""

        val startFormatted = formatTime(startTime)

        return if (endTime != null) {
            val endFormatted = formatTime(endTime)
            "$startFormatted - $endFormatted"
        } else {
            startFormatted
        }
    }

    /**
     * Formats a time range like "4:00 PM - 11:00 PM" using Instant with system timezone
     */
    fun formatTimeRange(startTime: Instant?, endTime: Instant?): String {
        if (startTime == null) return ""

        val startFormatted = formatTime(startTime)

        return if (endTime != null) {
            val endFormatted = formatTime(endTime)
            "$startFormatted - $endFormatted"
        } else {
            startFormatted
        }
    }

    /**
     * Formats a LocalizedTime with date and time like "Nov 15, 2025 8:00PM"
     */
    fun formatDateWithTime(localizedTime: LocalizedTime): String {
        val tz = resolveTimezone(localizedTime.timezone)
        val localDateTime = localizedTime.instant.toLocalDateTime(tz)
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val hour = localDateTime.hour
        val minute = localDateTime.minute

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val minuteStr = minute.toString().padStart(2, '0')

        return "$month ${localDateTime.dayOfMonth}, ${localDateTime.year} $hour12:$minuteStr$amPm"
    }

    /**
     * Formats a date with time like "Nov 15, 2025 8:00PM" using system timezone
     */
    fun formatDateWithTime(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val hour = localDateTime.hour
        val minute = localDateTime.minute

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val minuteStr = minute.toString().padStart(2, '0')

        return "$month ${localDateTime.dayOfMonth}, ${localDateTime.year} $hour12:$minuteStr$amPm"
    }

    /**
     * Formats a LocalizedTime to a simplified date string like "Oct 10"
     */
    fun formatSimplifiedDate(localizedTime: LocalizedTime): String {
        val tz = resolveTimezone(localizedTime.timezone)
        val localDateTime = localizedTime.instant.toLocalDateTime(tz)
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${localDateTime.dayOfMonth}"
    }

    /**
     * Formats an Instant to a simplified date string like "Oct 10" using system timezone
     */
    fun formatSimplifiedDate(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${localDateTime.dayOfMonth}"
    }

    /**
     * Formats a date range like "Oct 10" if same day, or "Oct 10 - Oct 11" if different days
     * @param showYear If true, includes the year in the output (e.g., "Oct 10, 2025")
     */
    fun formatDateRange(startTime: LocalizedTime?, endTime: LocalizedTime?, showYear: Boolean = false): String {
        if (startTime == null) return ""

        // If showYear is true, delegate to formatDateRangeWithYear
        if (showYear) {
            return formatDateRangeWithYear(startTime, endTime)
        }

        val startDate = formatSimplifiedDate(startTime)

        if (endTime == null) {
            return startDate
        }

        // Check if they're on the same day (using the start time's timezone)
        val tz = resolveTimezone(startTime.timezone)
        val startLocal = startTime.instant.toLocalDateTime(tz)
        val endLocal = endTime.instant.toLocalDateTime(tz)

        return if (startLocal.date == endLocal.date) {
            startDate
        } else {
            val endDate = formatSimplifiedDate(endTime)
            "$startDate - $endDate"
        }
    }

    /**
     * Formats a date range like "Oct 10" if same day, or "Oct 10 - Oct 11" if different days using system timezone
     * @param showYear If true, includes the year in the output (e.g., "Oct 10, 2025")
     */
    fun formatDateRange(startTime: Instant?, endTime: Instant?, showYear: Boolean = false): String {
        if (startTime == null) return ""

        // If showYear is true, delegate to formatDateRangeWithYear
        if (showYear) {
            return formatDateRangeWithYear(startTime, endTime)
        }

        val startDate = formatSimplifiedDate(startTime)

        if (endTime == null) {
            return startDate
        }

        // Check if they're on the same day
        val startLocal = startTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val endLocal = endTime.toLocalDateTime(TimeZone.currentSystemDefault())

        return if (startLocal.date == endLocal.date) {
            startDate
        } else {
            val endDate = formatSimplifiedDate(endTime)
            "$startDate - $endDate"
        }
    }

    /**
     * Formats a date range with year like "Oct 10, 2025" if same day, or "Oct 10 - Oct 11, 2025" if different days
     */
    fun formatDateRangeWithYear(startTime: LocalizedTime?, endTime: LocalizedTime?): String {
        if (startTime == null) return ""

        val tz = resolveTimezone(startTime.timezone)
        val startLocal = startTime.instant.toLocalDateTime(tz)
        val startMonth = startLocal.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

        if (endTime == null) {
            return "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year}"
        }

        val endLocal = endTime.instant.toLocalDateTime(tz)
        val endMonth = endLocal.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

        return when {
            // Same day
            startLocal.date == endLocal.date -> {
                "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year}"
            }
            // Same year - show year only at the end
            startLocal.year == endLocal.year -> {
                "$startMonth ${startLocal.dayOfMonth} - $endMonth ${endLocal.dayOfMonth}, ${endLocal.year}"
            }
            // Different years - show both
            else -> {
                "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year} - $endMonth ${endLocal.dayOfMonth}, ${endLocal.year}"
            }
        }
    }

    /**
     * Formats a date range with year using system timezone
     */
    fun formatDateRangeWithYear(startTime: Instant?, endTime: Instant?): String {
        if (startTime == null) return ""

        val startLocal = startTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val startMonth = startLocal.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

        if (endTime == null) {
            return "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year}"
        }

        val endLocal = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val endMonth = endLocal.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

        return when {
            // Same day
            startLocal.date == endLocal.date -> {
                "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year}"
            }
            // Same year - show year only at the end
            startLocal.year == endLocal.year -> {
                "$startMonth ${startLocal.dayOfMonth} - $endMonth ${endLocal.dayOfMonth}, ${endLocal.year}"
            }
            // Different years - show both
            else -> {
                "$startMonth ${startLocal.dayOfMonth}, ${startLocal.year} - $endMonth ${endLocal.dayOfMonth}, ${endLocal.year}"
            }
        }
    }

    /**
     * Formats duration between two LocalizedTime values like "1h 30m" or "30m"
     */
    fun formatDuration(startTime: LocalizedTime?, endTime: LocalizedTime?): String? {
        if (startTime == null || endTime == null) return null

        val durationMillis = (endTime.instant - startTime.instant).inWholeMilliseconds
        val totalMinutes = (durationMillis / 1000 / 60).toInt()

        if (totalMinutes <= 0) return null

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    /**
     * Formats duration between two times like "1h 30m" or "30m"
     * Shows only minutes if under 60 minutes
     */
    fun formatDuration(startTime: Instant?, endTime: Instant?): String? {
        if (startTime == null || endTime == null) return null

        val durationMillis = (endTime - startTime).inWholeMilliseconds
        val totalMinutes = (durationMillis / 1000 / 60).toInt()

        if (totalMinutes <= 0) return null

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    /**
     * Gets the LocalDate from a LocalizedTime for grouping purposes
     */
    fun getLocalDate(localizedTime: LocalizedTime): LocalDate {
        val tz = resolveTimezone(localizedTime.timezone)
        return localizedTime.instant.toLocalDateTime(tz).date
    }

    /**
     * Gets the LocalDate from an Instant for grouping purposes
     */
    fun getLocalDate(instant: Instant): LocalDate {
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    /**
     * Formats a LocalDate to a day header string like "Saturday, July 26"
     */
    fun formatDayHeader(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayOfWeek, $month ${date.dayOfMonth}"
    }

    /**
     * Formats an Instant as relative time like "2m ago", "1h ago", "3d ago"
     */
    fun formatRelativeTime(instant: Instant): String {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val diffMillis = now - instant.toEpochMilliseconds()
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        val diffWeeks = diffDays / 7

        return when {
            diffSeconds < 60 -> "just now"
            diffMinutes < 60 -> "${diffMinutes}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            diffDays < 7 -> "${diffDays}d ago"
            diffWeeks < 4 -> "${diffWeeks}w ago"
            else -> formatSimplifiedDate(instant)
        }
    }
}
