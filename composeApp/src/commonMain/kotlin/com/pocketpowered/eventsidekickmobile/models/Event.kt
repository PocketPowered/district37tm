package com.district37.toastmasters.models

import com.district37.toastmasters.graphql.type.EventType
import kotlinx.datetime.Instant

/**
 * Domain model for Event
 */
data class Event(
    val id: Int,
    val slug: String? = null,
    val name: String,
    val description: String?,
    val eventType: EventType?,
    val startDate: LocalizedTime?,
    val endDate: LocalizedTime?,
    val venueId: Int?,
    val venue: Venue? = null,
    val images: List<Image> = emptyList(),
    // Agenda dates for this event (each date can contain multiple agenda items)
    val agendaDates: List<AgendaDate> = emptyList(),
    override val userEngagement: UserEngagement? = null,
    override val permissions: EntityPermissions? = null,
    val archivedAt: Instant? = null
) : HasPermissions, HasUserEngagement {
    val isArchived: Boolean
        get() = archivedAt != null
}

/**
 * Connection type for paginated Event results
 */
data class EventConnection(
    val events: List<Event>,
    val hasNextPage: Boolean,
    val endCursor: String?
)
