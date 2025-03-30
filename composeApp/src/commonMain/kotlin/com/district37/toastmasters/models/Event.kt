package com.district37.toastmasters.models

/**
 * Represents an individual detailed event
 */
data class Event(
    val id: Int,
    val images: List<String>,
    val title: String,
    val description: String,
    val time: Long,
    val locationInfo: String,
    val agenda: List<AgendaItem>,
    val additionalLinks: List<ExternalLink>,
)

data class EventPreview(
    val id: Int,
    val image: String,
    val title: String,
    val time: Long,
    val locationInfo: String,
)

data class AgendaItem(
    val title: String,
    val description: String,
    val time: Long,
    val locationInfo: String
)

data class ExternalLink(
    val displayName: String,
    val url: String
)