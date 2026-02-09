package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Domain model for Location
 */
data class Location(
    val id: Int,
    val slug: String? = null,
    val name: String,
    val description: String?,
    val venueId: Int?,
    val locationType: String? = null,
    val capacity: Int? = null,
    val floorLevel: String? = null,
    val createdAt: Instant? = null,
    val images: List<Image> = emptyList(),
    val agendaItemIds: List<Int> = emptyList(),
    // Paged field for agenda items
    val agendaItems: PagedField<AgendaItem> = PagedField(),
    val userEngagement: UserEngagement? = null
)
