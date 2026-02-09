package com.district37.toastmasters.models

import kotlinx.datetime.LocalDate

/**
 * Represents a date within an event's agenda, along with the count of items on that date.
 * Used for displaying date tabs in the event detail view.
 */
data class AgendaDate(
    val date: LocalDate,
    val itemCount: Int
)
