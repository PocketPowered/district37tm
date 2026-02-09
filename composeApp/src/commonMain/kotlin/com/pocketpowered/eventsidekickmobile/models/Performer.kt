package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Domain model for Performer
 */
data class Performer(
    val id: Int,
    val slug: String? = null,
    val name: String,
    val bio: String?,
    val performerType: String? = null,
    val createdAt: Instant? = null,
    val images: List<Image> = emptyList(),
    // Paged field for agenda items (performances)
    val agendaItems: PagedField<AgendaItem> = PagedField(),
    // Paged field for events
    val events: PagedField<Event> = PagedField(),
    override val userEngagement: UserEngagement? = null,
    override val permissions: EntityPermissions? = null
) : HasPermissions, HasUserEngagement
