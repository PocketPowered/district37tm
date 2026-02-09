package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.transformers.toAgendaItemSyncRecord
import com.district37.toastmasters.graphql.GetAgendaItemSyncsForEventQuery
import com.district37.toastmasters.graphql.GetMyAgendaItemSyncsQuery
import com.district37.toastmasters.graphql.RecordAgendaItemSyncMutation
import com.district37.toastmasters.graphql.RemoveAgendaItemSyncMutation
import com.district37.toastmasters.graphql.type.Platform as GraphQLPlatform
import com.district37.toastmasters.models.AgendaItemSyncRecord
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.util.Resource

/**
 * Repository for AgendaItemSync data
 * Handles communication with the server for calendar sync records
 */
class AgendaItemSyncRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "AgendaItemSyncRepository"

    /**
     * Get all synced agenda items for the current user
     */
    suspend fun getMySyncedAgendaItems(): Resource<List<AgendaItemSyncRecord>> {
        return executeQuery(
            queryName = "getMySyncedAgendaItems",
            query = {
                apolloClient.query(GetMyAgendaItemSyncsQuery()).execute()
            },
            transform = { data ->
                data.myAgendaItemSyncs.map { it.agendaItemSyncFragment.toAgendaItemSyncRecord() }
            }
        )
    }

    /**
     * Get synced agenda items for a specific event
     */
    suspend fun getSyncedAgendaItemsForEvent(eventId: Int): Resource<List<AgendaItemSyncRecord>> {
        return executeQuery(
            queryName = "getSyncedAgendaItemsForEvent(eventId=$eventId)",
            query = {
                apolloClient.query(GetAgendaItemSyncsForEventQuery(eventId = eventId)).execute()
            },
            transform = { data ->
                data.agendaItemSyncsForEvent.map { it.agendaItemSyncFragment.toAgendaItemSyncRecord() }
            }
        )
    }

    /**
     * Record a new calendar sync on the server
     *
     * @param agendaItemId The agenda item that was synced
     * @param eventId The event the agenda item belongs to
     * @param platform The platform (Android/iOS)
     * @param calendarEventId The device calendar event ID
     * @param calendarId Optional calendar ID (for users with multiple calendars)
     */
    suspend fun recordAgendaItemSync(
        agendaItemId: Int,
        eventId: Int,
        platform: Platform,
        calendarEventId: String,
        calendarId: String? = null
    ): Resource<AgendaItemSyncRecord> {
        return executeMutation(
            mutationName = "recordAgendaItemSync(agendaItemId=$agendaItemId, eventId=$eventId)",
            mutation = {
                apolloClient.mutation(
                    RecordAgendaItemSyncMutation(
                        agendaItemId = agendaItemId,
                        eventId = eventId,
                        platform = platform.toGraphQLPlatform(),
                        calendarEventId = calendarEventId,
                        calendarId = if (calendarId != null) Optional.present(calendarId) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.recordAgendaItemSync.agendaItemSyncFragment.toAgendaItemSyncRecord()
            }
        )
    }

    /**
     * Remove a calendar sync record from the server
     * Called when user removes an agenda item from their calendar
     */
    suspend fun removeAgendaItemSync(agendaItemId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeAgendaItemSync(agendaItemId=$agendaItemId)",
            mutation = {
                apolloClient.mutation(RemoveAgendaItemSyncMutation(agendaItemId = agendaItemId)).execute()
            },
            transform = { data ->
                data.removeAgendaItemSync
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
