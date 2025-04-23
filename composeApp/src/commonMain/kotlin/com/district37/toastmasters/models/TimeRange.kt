package com.district37.toastmasters.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TimeRange(
    val startTime: Long,
    val endTime: Long
)

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
    
    val startPeriod = if (startHour < 12) "AM" else "PM"
    val endPeriod = if (endHour < 12) "AM" else "PM"
    
    val formattedStartHour = if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour
    val formattedEndHour = if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour
    
    val timeString = "${formattedStartHour}:${startMinute.toString().padStart(2, '0')}$startPeriod - ${formattedEndHour}:${endMinute.toString().padStart(2, '0')}$endPeriod"
    
    if (!showDate) {
        return timeString
    }
    
    val isSameDay = startYear == endYear && 
                   startMonth == endMonth && 
                   startDay == endDay
    
    return if (isSameDay) {
        "$startMonth $startDay, $startYear $timeString"
    } else {
        "$startMonth $startDay, $startYear ${formattedStartHour}:${startMinute.toString().padStart(2, '0')}$startPeriod - $endMonth $endDay, $endYear ${formattedEndHour}:${endMinute.toString().padStart(2, '0')}$endPeriod"
    }
} 