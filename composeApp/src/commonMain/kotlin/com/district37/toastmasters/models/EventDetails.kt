package com.district37.toastmasters.models

/**
 * Represents an individual detailed event
 */
data class EventDetails(
    val id: Int,
    val images: List<String>?,
    val title: String,
    val description: String,
    val time: TimeRange,
    val locationInfo: String,
    val agenda: List<AgendaItem>,
    val additionalLinks: List<ExternalLink>,
)