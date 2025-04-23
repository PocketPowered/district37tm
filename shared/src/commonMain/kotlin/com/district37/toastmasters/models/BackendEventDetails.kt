package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

/**
 * Represents an individual detailed event
 */
@Serializable
data class BackendEventDetails(
    val id: Int = 0,
    val images: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val locationInfo: String = "",
    val agenda: List<BackendAgendaItem> = emptyList(),
    val additionalLinks: List<BackendExternalLink> = emptyList(),
    val dateKey: String? = null
)