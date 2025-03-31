package com.district37.toastmasters.util

import com.district37.toastmasters.models.TimeReference
import com.wongislandd.nexus.util.Transformer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EpochTimeTransformer : Transformer<Long, TimeReference> {
    override fun transform(input: Long): TimeReference {
        val instant = Instant.fromEpochMilliseconds(input)
        val localDateTime = instant.toLocalDateTime(TimeZone.of("UTC"))

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
        return TimeReference(
            timeDisplay = "$month $day, $year at $hour12:$minuteStr $amPm",
            timestamp = input
        )
    }

}