package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.CreateAgendaItemMutation
import com.district37.toastmasters.graphql.DeleteAgendaItemMutation
import com.district37.toastmasters.graphql.GetAgendaItemQuery
import com.district37.toastmasters.graphql.GetAgendaItemsByEventAndDateQuery
import com.district37.toastmasters.graphql.UpdateAgendaItemMutation
import com.district37.toastmasters.graphql.type.CreateAgendaItemInput
import com.district37.toastmasters.graphql.type.UpdateAgendaItemInput
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.transformers.toAgendaItem
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemConnection
import com.district37.toastmasters.util.Resource
import kotlinx.datetime.LocalDate

/**
 * Repository for AgendaItem data
 * Fetches agenda items from GraphQL API using Apollo client
 */
class AgendaItemRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient), BaseDetailRepository<AgendaItem> {

    override val tag = "AgendaItemRepository"

    /**
     * Get a single agenda item by ID
     */
    suspend fun getAgendaItem(id: Int): Resource<AgendaItem> {
        return executeQuery(
            queryName = "getAgendaItem(id=$id)",
            query = {
                apolloClient.query(GetAgendaItemQuery(id = id)).execute()
            },
            transform = { data ->
                data.agendaItem?.agendaItemDetails?.toAgendaItem()
            }
        )
    }

    override suspend fun getDetails(id: Int): Resource<AgendaItem> = getAgendaItem(id)

    /**
     * Get paginated agenda items for an event on a specific date
     *
     * @param eventId The ID of the event
     * @param date The date to fetch agenda items for (in ISO-8601 format: YYYY-MM-DD)
     * @param cursor Optional cursor for pagination. If null, fetches the first page.
     * @param first Number of items to fetch per page
     * @param myScheduleOnly If true, only return agenda items the user has RSVP'd "GOING" to
     */
    suspend fun getAgendaItemsByEventAndDate(
        eventId: Int,
        date: LocalDate,
        cursor: String? = null,
        first: Int = 10,
        myScheduleOnly: Boolean = false
    ): Resource<AgendaItemConnection> {
        return executeQuery(
            queryName = "getAgendaItemsByEventAndDate(eventId=$eventId, date=$date, cursor=$cursor, myScheduleOnly=$myScheduleOnly)",
            query = {
                apolloClient.query(
                    GetAgendaItemsByEventAndDateQuery(
                        eventId = eventId,
                        date = date,
                        first = Optional.present(first),
                        after = if (cursor != null) Optional.present(cursor) else Optional.absent(),
                        last = Optional.absent(),
                        before = Optional.absent(),
                        myScheduleOnly = Optional.present(myScheduleOnly)
                    )
                ).execute()
            },
            transform = { data ->
                data.agendaItemsByEventAndDate.let { connection ->
                    val agendaItems = connection.edges.mapNotNull { edge ->
                        edge.node.agendaItemPreview.toAgendaItem()
                    }
                    AgendaItemConnection(
                        agendaItems = agendaItems,
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor,
                        totalCount = connection.totalCount
                    )
                }
            }
        )
    }

    /**
     * Create a new agenda item for an event
     *
     * @param input The agenda item creation input
     * @return Resource containing the created AgendaItem on success
     */
    suspend fun createAgendaItem(input: CreateAgendaItemInput): Resource<AgendaItem> {
        return executeMutation(
            mutationName = "createAgendaItem(eventId=${input.eventId})",
            mutation = {
                apolloClient.mutation(CreateAgendaItemMutation(input = input)).execute()
            },
            transform = { data ->
                data.createAgendaItem.agendaItemDetails.toAgendaItem()
            }
        )
    }

    /**
     * Update an existing agenda item
     *
     * @param id The ID of the agenda item to update
     * @param input The agenda item update input
     * @return Resource containing the updated AgendaItem on success
     */
    suspend fun updateAgendaItem(id: Int, input: UpdateAgendaItemInput): Resource<AgendaItem> {
        return executeMutation(
            mutationName = "updateAgendaItem(id=$id)",
            mutation = {
                apolloClient.mutation(UpdateAgendaItemMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updateAgendaItem.agendaItemDetails.toAgendaItem()
            }
        )
    }

    /**
     * Delete an agenda item
     *
     * @param id The ID of the agenda item to delete
     * @return Resource containing true on success
     */
    suspend fun deleteAgendaItem(id: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "deleteAgendaItem(id=$id)",
            mutation = {
                apolloClient.mutation(DeleteAgendaItemMutation(id = id)).execute()
            },
            transform = { data ->
                data.deleteAgendaItem
            }
        )
    }
}
