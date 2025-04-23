package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.DateTabInfo
import com.wongislandd.nexus.util.Transformer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateTransformer : Transformer<List<Long>, List<DateTabInfo>> {

    override fun transform(input: List<Long>): List<DateTabInfo> {
        return input.mapIndexed { index, dateTimestamp ->
            DateTabInfo(
                displayName = dateTimestamp.toHumanReadableDate(),
                dateKey = dateTimestamp,
                isSelected = index == 0
            )
        }
    }
}

private fun Long.toHumanReadableDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year

    return "$month $day, $year"
}