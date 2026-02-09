package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.transformers.toEventSyncRecord
import com.district37.toastmasters.graphql.GetEventSyncByEventIdQuery
import com.district37.toastmasters.graphql.GetMyEventSyncsQuery
import com.district37.toastmasters.graphql.RecordEventSyncMutation
import com.district37.toastmasters.graphql.RemoveEventSyncMutation
import com.district37.toastmasters.graphql.type.Platform as GraphQLPlatform
import com.district37.toastmasters.models.EventSyncRecord
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.util.Resource

/**
 * Repository for EventSync data (event-level syncs)
 * Handles communication with the server for event sync records
 */
class EventSyncRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "EventSyncRepository"

    /**
     * Get all synced events for the current user
     */
    suspend fun getMySyncedEvents(): Resource<List<EventSyncRecord>> {
        return executeQuery(
            queryName = "getMySyncedEvents",
            query = {
                apolloClient.query(GetMyEventSyncsQuery()).execute()
            },
            transform = { data ->
                data.myEventSyncs.map { it.eventSyncFragment.toEventSyncRecord() }
            }
        )
    }

    /**
     * Get event sync for a specific event
     */
    suspend fun getSyncByEventId(eventId: Int): Resource<EventSyncRecord?> {
        return executeQuery(
            queryName = "getSyncByEventId(eventId=$eventId)",
            query = {
                apolloClient.query(GetEventSyncByEventIdQuery(eventId = eventId)).execute()
            },
            transform = { data ->
                data.eventSyncByEventId?.eventSyncFragment?.toEventSyncRecord()
            }
        )
    }

    /**
     * Record a new event sync on the server
     *
     * @param eventId The event that was synced
     * @param platform The platform (Android/iOS)
     * @param calendarEventId The device calendar event ID
     * @param calendarId Optional calendar ID (for users with multiple calendars)
     */
    suspend fun recordEventSync(
        eventId: Int,
        platform: Platform,
        calendarEventId: String,
        calendarId: String? = null
    ): Resource<EventSyncRecord> {
        return executeMutation(
            mutationName = "recordEventSync(eventId=$eventId)",
            mutation = {
                apolloClient.mutation(
                    RecordEventSyncMutation(
                        eventId = eventId,
                        platform = platform.toGraphQLPlatform(),
                        calendarEventId = calendarEventId,
                        calendarId = if (calendarId != null) Optional.present(calendarId) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.recordEventSync.eventSyncFragment.toEventSyncRecord()
            }
        )
    }

    /**
     * Remove an event sync record from the server
     * Called when user removes an event from their calendar
     */
    suspend fun removeEventSync(eventId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeEventSync(eventId=$eventId)",
            mutation = {
                apolloClient.mutation(RemoveEventSyncMutation(eventId = eventId)).execute()
            },
            transform = { data ->
                data.removeEventSync
            }
        )
    }

    /**
     * Convert domain Platform to GraphQL Platform
     */
    private fun Platform.toGraphQLPlatform(): GraphQLPlatform {
        return when (this) {
            Platform.ANDROID -> GraphQLPlatform.ANDROID
            Platform.IOS -> GraphQLPlatform.IOS
        }
    }
}
