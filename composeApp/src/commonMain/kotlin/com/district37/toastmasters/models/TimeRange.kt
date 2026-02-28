package com.district37.toastmasters.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TimeRange(
    val startTime: Long,
    val endTime: Long
)

private fun formatHourMinute(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val formattedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "${formattedHour}:${minute.toString().padStart(2, '0')} $period"
}

fun TimeRange.toHumanReadableString(showDate: Boolean = true): String {
    val startDateTime = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(TimeZone.currentSystemDefault())
    val endDateTime = Instant.fromEpochMilliseconds(endTime).toLocalDateTime(TimeZone.currentSystemDefault())
    
    val startMonth = startDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val startDay = startDateTime.dayOfMonth
    val startYear = startDateTime.year
    val startHour = startDateTime.hour
    val startMinute = startDateTime.minute
    
    val endMonth = endDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val endDay = endDateTime.dayOfMonth
    val endYear = endDateTime.year
    val endHour = endDateTime.hour
    val endMinute = endDateTime.minute

    val timeString = "${formatHourMinute(startHour, startMinute)} - ${formatHourMinute(endHour, endMinute)}"
    
    if (!showDate) {
        return timeString
    }
    
    val isSameDay = startYear == endYear && 
                   startMonth == endMonth && 
                   startDay == endDay
    
    return if (isSameDay) {
        "$startMonth $startDay, $startYear $timeString"
    } else {
        "$startMonth $startDay, $startYear ${formatHourMinute(startHour, startMinute)} - $endMonth $endDay, $endYear ${formatHourMinute(endHour, endMinute)}"
    }
} 
