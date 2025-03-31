package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

/**
 * Represents an individual detailed event
 */
@Serializable
data class BackendEventDetails(
    val id: Int,
    val images: List<String>,
    val title: String,
    val description: String,
    val time: Long,
    val locationInfo: String,
    val agenda: List<BackendAgendaItem>,
    val additionalLinks: List<BackendExternalLink>,
)