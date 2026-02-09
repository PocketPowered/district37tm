package com.district37.toastmasters.models

/**
 * Domain model for AgendaItem (formerly ScheduleItem)
 * Agenda items are now directly nested under Events, without a Schedule layer.
 */
data class AgendaItem(
    val id: Int,
    val eventId: Int,
    val title: String,
    val description: String?,
    val startTime: LocalizedTime?,
    val endTime: LocalizedTime?,
    val performerIds: List<Int> = emptyList(),
    val locationId: Int?,
    val tag: AgendaItemTag?,
    val performers: List<Performer> = emptyList(),
    val location: Location? = null,
    val event: Event? = null,
    val userEngagement: UserEngagement? = null,
    val permissions: EntityPermissions? = null
)

/**
 * Connection type for paginated AgendaItem results
 */
data class AgendaItemConnection(
    val agendaItems: List<AgendaItem>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int? = null
)

/**
 * Extension property to calculate the duration of an agenda item in minutes.
 * Returns null if either startTime or endTime is not set.
 */
val AgendaItem.durationMinutes: Int?
    get() = if (startTime != null && endTime != null) {
        ((endTime.instant.toEpochMilliseconds() - startTime.instant.toEpochMilliseconds()) / 1000 / 60).toInt()
    } else null
