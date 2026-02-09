package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.CreatePerformerMutation
import com.district37.toastmasters.graphql.GetPerformerQuery
import com.district37.toastmasters.graphql.GetPerformerDetailsQuery
import com.district37.toastmasters.graphql.GetMorePerformerEventsQuery
import com.district37.toastmasters.graphql.GetPerformerAgendaItemsQuery
import com.district37.toastmasters.graphql.SearchPerformersQuery
import com.district37.toastmasters.graphql.UpdatePerformerMutation
import com.district37.toastmasters.graphql.type.CreatePerformerInput
import com.district37.toastmasters.graphql.type.UpdatePerformerInput
import com.district37.toastmasters.data.transformers.toAgendaItem
import com.district37.toastmasters.data.transformers.toPerformer
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.models.AgendaItemConnection
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Performer data
 * Fetches performers from GraphQL API using Apollo client
 */
class PerformerRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient), BaseDetailRepository<Performer>,
    BasePreviewRepository<Performer> {

    override val tag = "PerformerRepository"

    /**
     * Get a single performer by ID (preview data)
     */
    suspend fun getPerformer(id: Int): Resource<Performer> {
        return executeQuery(
            queryName = "getPerformer(id=$id)",
            query = { apolloClient.query(GetPerformerQuery(id = id)).execute() },
            transform = { data -> data.performer?.performerPreview?.toPerformer() }
        )
    }

    /**
     * Get detailed performer data by ID (includes images and all fields)
     */
    suspend fun getPerformerDetails(id: Int): Resource<Performer> {
        return executeQuery(
            queryName = "getPerformerDetails(id=$id)",
            query = { apolloClient.query(GetPerformerDetailsQuery(id = id)).execute() },
            transform = { data -> data.performer?.performerDetails?.toPerformer() }
        )
    }

    /**
     * BaseDetailRepository implementation: Get details for a single performer
     */
    override suspend fun getDetails(id: Int): Resource<Performer> = getPerformerDetails(id)

    override suspend fun getPreview(id: Int): Resource<Performer> = getPerformer(id)

    /**
     * Get paginated events for a performer
     * Returns events along with pagination info (hasNextPage, endCursor)
     *
     * @param performerId The ID of the performer
     * @param cursor Optional cursor for pagination. If null, fetches the first page.
     * @param first Number of items to fetch per page
     */
    suspend fun getPerformerEvents(
        performerId: Int,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Event>> {
        return executeQuery(
            queryName = "getPerformerEvents(performerId=$performerId, cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMorePerformerEventsQuery(
                        id = performerId,
                        first = Optional.present(first),
                        after = if (cursor != null) Optional.present(cursor) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.performer?.eventsConnection?.let { eventsConnection ->
                    PagedConnection(
                        items = eventsConnection.edges.mapNotNull { edge ->
                            edge.node.eventPreview.toEvent()
                        },
                        hasNextPage = eventsConnection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = eventsConnection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Search performers with pagination
     *
     * @param searchQuery Optional search query to filter performers by name
     * @param cursor Optional cursor for pagination
     * @param first Number of items to fetch per page
     * @return Resource containing paginated performers
     */
    suspend fun searchPerformers(
        searchQuery: String? = null,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Performer>> {
        return executeQuery(
            queryName = "searchPerformers(query=$searchQuery, cursor=$cursor)",
            query = {
                apolloClient.query(
                    SearchPerformersQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor),
                        searchQuery = Optional.presentIfNotNull(searchQuery)
                    )
                ).execute()
            },
            transform = { data ->
                data.performersConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.performerPreview.toPerformer()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Create a new performer
     *
     * @param input The performer creation input
     * @return Resource containing the created Performer on success
     */
    suspend fun createPerformer(input: CreatePerformerInput): Resource<Performer> {
        return executeMutation(
            mutationName = "createPerformer(name=${input.name})",
            mutation = {
                apolloClient.mutation(CreatePerformerMutation(input = input)).execute()
            },
            transform = { data ->
                data.createPerformer.performerDetails.toPerformer()
            }
        )
    }

    /**
     * Update an existing performer
     *
     * @param id The performer ID to update
     * @param input The performer update input
     * @return Resource containing the updated Performer on success
     */
    suspend fun updatePerformer(id: Int, input: UpdatePerformerInput): Resource<Performer> {
        return executeMutation(
            mutationName = "updatePerformer(id=$id)",
            mutation = {
                apolloClient.mutation(UpdatePerformerMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updatePerformer.performerDetails.toPerformer()
            }
        )
    }

    /**
     * Get paginated agenda items for a performer
     * Returns agenda items along with pagination info
     *
     * @param performerId The ID of the performer
     * @param cursor Optional cursor for pagination. If null, fetches the first page.
     * @param first Number of items to fetch per page
     */
    suspend fun getPerformerAgendaItems(
        performerId: Int,
        cursor: String? = null,
        first: Int = 20
    ): Resource<AgendaItemConnection> {
        return executeQuery(
            queryName = "getPerformerAgendaItems(performerId=$performerId, cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetPerformerAgendaItemsQuery(
                        performerId = performerId,
                        first = Optional.present(first),
                        after = if (cursor != null) Optional.present(cursor) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.agendaItemsConnection.let { connection ->
                    AgendaItemConnection(
                        agendaItems = connection.edges.map { edge ->
                            edge.node.agendaItemDetails.toAgendaItem()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor,
                        totalCount = connection.totalCount
                    )
                }
            }
        )
    }
}
