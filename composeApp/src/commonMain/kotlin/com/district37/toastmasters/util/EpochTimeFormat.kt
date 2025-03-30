package com.district37.toastmasters.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// TODO Can probably be done in the transformation
fun formatEpochMillisToDateTime(epochMillis: Long, zone: String = "UTC"): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val localDateTime = instant.toLocalDateTime(TimeZone.of(zone))

    // Format date manually
    val month = localDateTime.month.name.lowercase()
        .replaceFirstChar { it.uppercase() }  // Capitalize first letter of the month
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year

    // Format time manually (12-hour format with AM/PM)
    val hour = localDateTime.hour
    val minute = localDateTime.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12 // Convert 24-hour to 12-hour format
    val minuteStr = minute.toString().padStart(2, '0') // Ensure two-digit minutes

    // Combine date and time
    return "$month $day, $year at $hour12:$minuteStr $amPm"
}