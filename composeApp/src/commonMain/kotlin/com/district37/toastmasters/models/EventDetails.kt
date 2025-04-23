package com.district37.toastmasters.models

/**
 * Represents an individual detailed event
 */
data class EventDetails(
    val id: Int,
    val images: List<String>,
    val title: String,
    val description: String,
    val time: TimeRange,
    val locationInfo: String,
    val agenda: List<AgendaItem>,
    val additionalLinks: List<ExternalLink>,
)

data class EventPreview(
    val id: Int,
    val image: String,
    val title: String,
    val time: TimeRange,
    val locationInfo: String,
    val isFavorited: Boolean = false,
)

data class AgendaItem(
    val title: String,
    val description: String,
    val time: TimeRange,
    val locationInfo: String
)

data class TimeReference(
    val timeDisplay: String,
    val timestamp: Long
)

data class ExternalLink(
    val displayName: String,
    val url: String
)