package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Status of a calendar sync record for agenda items.
 */
enum class AgendaItemSyncStatus {
    SYNCED,
    DELETED,
    ERROR,
    NEEDS_UPDATE
}

/**
 * Represents a synced agenda item record from the server.
 * This tracks what the server knows about calendar syncs.
 */
data class AgendaItemSyncRecord(
    val id: Long,
    val agendaItemId: Int,
    val eventId: Int,
    val platform: Platform,
    val calendarEventId: String,
    val calendarId: String?,
    val syncedAt: Instant?,
    val lastUpdatedAt: Instant?,
    val lastServerUpdatedAt: Instant?,
    val status: AgendaItemSyncStatus
)

/**
 * Status of a calendar sync record for events.
 */
enum class EventSyncStatus {
    SYNCED,
    DELETED,
    ERROR,
    NEEDS_UPDATE
}

/**
 * Represents a synced event record from the server.
 * This tracks what the server knows about event calendar syncs.
 */
data class EventSyncRecord(
    val id: Long,
    val eventId: Int,
    val platform: Platform,
    val calendarEventId: String,
    val calendarId: String?,
    val syncedAt: Instant?,
    val lastUpdatedAt: Instant?,
    val status: EventSyncStatus,
    val userId: String
)
