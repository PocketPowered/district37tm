package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.AgendaItemSyncFragment
import com.district37.toastmasters.graphql.fragment.EventSyncFragment
import com.district37.toastmasters.graphql.type.CalendarSyncStatus as GraphQLCalendarSyncStatus
import com.district37.toastmasters.graphql.type.Platform as GraphQLPlatform
import com.district37.toastmasters.models.AgendaItemSyncRecord
import com.district37.toastmasters.models.AgendaItemSyncStatus
import com.district37.toastmasters.models.EventSyncRecord
import com.district37.toastmasters.models.EventSyncStatus
import com.district37.toastmasters.models.Platform

/**
 * Transforms GraphQL AgendaItemSyncFragment to domain AgendaItemSyncRecord model
 */
fun AgendaItemSyncFragment.toAgendaItemSyncRecord(): AgendaItemSyncRecord {
    // Server returns BigInt as Long or Int - cast appropriately
    val idLong = (id as? Long) ?: (id as Int).toLong()

    return AgendaItemSyncRecord(
        id = idLong,
        agendaItemId = agendaItemId,
        eventId = eventId,
        platform = platform.toPlatform(),
        calendarEventId = calendarEventId,
        calendarId = calendarId,
        syncedAt = syncedAt,
        lastUpdatedAt = lastUpdatedAt,
        lastServerUpdatedAt = lastServerUpdatedAt,
        status = status.toAgendaItemSyncStatus()
    )
}

/**
 * Convert GraphQL Platform to domain Platform
 */
fun GraphQLPlatform.toPlatform(): Platform {
    return when (this) {
        GraphQLPlatform.ANDROID -> Platform.ANDROID
        GraphQLPlatform.IOS -> Platform.IOS
        else -> Platform.ANDROID // Default fallback
    }
}

/**
 * Convert GraphQL CalendarSyncStatus to domain AgendaItemSyncStatus
 * Note: Server uses shared CalendarSyncStatus enum for both EventSync and AgendaItemSync
 */
fun GraphQLCalendarSyncStatus.toAgendaItemSyncStatus(): AgendaItemSyncStatus {
    return when (this) {
        GraphQLCalendarSyncStatus.SYNCED -> AgendaItemSyncStatus.SYNCED
        GraphQLCalendarSyncStatus.DELETED -> AgendaItemSyncStatus.DELETED
        GraphQLCalendarSyncStatus.ERROR -> AgendaItemSyncStatus.ERROR
        GraphQLCalendarSyncStatus.NEEDS_UPDATE -> AgendaItemSyncStatus.NEEDS_UPDATE
        else -> AgendaItemSyncStatus.SYNCED // Default fallback
    }
}

/**
 * Transforms GraphQL EventSyncFragment to domain EventSyncRecord model
 */
fun EventSyncFragment.toEventSyncRecord(): EventSyncRecord {
    // Server returns BigInt as Long or Int - cast appropriately
    val idLong = (id as? Long) ?: (id as Int).toLong()

    return EventSyncRecord(
        id = idLong,
        eventId = eventId,
        platform = platform.toPlatform(),
        calendarEventId = calendarEventId,
        calendarId = calendarId,
        syncedAt = syncedAt,
        lastUpdatedAt = lastUpdatedAt,
        status = status.toEventSyncStatus(),
        userId = userId
    )
}

/**
 * Convert GraphQL CalendarSyncStatus to domain EventSyncStatus
 */
fun GraphQLCalendarSyncStatus.toEventSyncStatus(): EventSyncStatus {
    return when (this) {
        GraphQLCalendarSyncStatus.SYNCED -> EventSyncStatus.SYNCED
        GraphQLCalendarSyncStatus.DELETED -> EventSyncStatus.DELETED
        GraphQLCalendarSyncStatus.ERROR -> EventSyncStatus.ERROR
        GraphQLCalendarSyncStatus.NEEDS_UPDATE -> EventSyncStatus.NEEDS_UPDATE
        else -> EventSyncStatus.SYNCED // Default fallback
    }
}
