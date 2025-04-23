package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

/**
 * Represents an individual detailed event
 */
@Serializable
data class BackendEventDetails(
    val id: Int = 0,
    val images: List<String>? = null,
    val title: String? = null,
    val description: String? = null,
    val time: BackendTimeRange? = null,
    val locationInfo: String? = null,
    val agenda: List<BackendAgendaItem>? = null,
    val additionalLinks: List<BackendExternalLink>? = null,
    val dateKey: Long? = null
)