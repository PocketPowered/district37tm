package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.*
import com.district37.toastmasters.graphql.type.CreateVenueInput
import com.district37.toastmasters.graphql.type.UpdateVenueInput
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.data.transformers.toVenue
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Venue data
 * Fetches venues and their events from GraphQL API using Apollo client
 */
class VenueRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient), BaseDetailRepository<Venue>,
    BasePreviewRepository<Venue> {

    override val tag = "VenueRepository"

    /**
     * Get a single venue by ID with full details including images
     */
    suspend fun getVenue(id: Int): Resource<Venue> {
        return executeQuery(
            queryName = "getVenue(id=$id)",
            query = { apolloClient.query(GetVenueQuery(id = id)).execute() },
            transform = { data -> data.venue?.venueDetails?.toVenue() }
        )
    }

    /**
     * BaseDetailRepository implementation: Get details for a single venue
     */
    override suspend fun getDetails(id: Int): Resource<Venue> = getVenue(id)

    override suspend fun getPreview(id: Int): Resource<Venue> = getVenue(id)

    /**
     * Get paginated events for a venue
     * Returns events along with pagination info (hasNextPage, endCursor)
     *
     * @param venueId The ID of the venue
     * @param cursor Optional cursor for pagination. If null, fetches the first page.
     * @param first Number of items to fetch per page
     */
    suspend fun getVenueEvents(
        venueId: Int,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Event>> {
        return executeQuery(
            queryName = "getVenueEvents(venueId=$venueId, cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMoreVenueEventsQuery(
                        id = venueId,
                        first = Optional.present(first),
                        after = if (cursor != null) Optional.present(cursor) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.venue?.eventsConnection?.let { eventsConnection ->
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
     * Search venues with pagination
     *
     * @param searchQuery Optional search query to filter venues by name/address
     * @param cursor Optional cursor for pagination
     * @param first Number of items to fetch per page
     * @return Resource containing paginated venues
     */
    suspend fun searchVenues(
        searchQuery: String? = null,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Venue>> {
        return executeQuery(
            queryName = "searchVenues(query=$searchQuery, cursor=$cursor)",
            query = {
                apolloClient.query(
                    SearchVenuesQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor),
                        searchQuery = Optional.presentIfNotNull(searchQuery)
                    )
                ).execute()
            },
            transform = { data ->
                data.venuesConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.venuePreview.toVenue()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Create a new venue
     *
     * @param input The venue creation input
     * @return Resource containing the created Venue on success
     */
    suspend fun createVenue(input: CreateVenueInput): Resource<Venue> {
        return executeMutation(
            mutationName = "createVenue(name=${input.name})",
            mutation = {
                apolloClient.mutation(CreateVenueMutation(input = input)).execute()
            },
            transform = { data ->
                data.createVenue.venueDetails.toVenue()
            }
        )
    }

    /**
     * Update an existing venue
     *
     * @param id The venue ID to update
     * @param input The venue update input
     * @return Resource containing the updated Venue on success
     */
    suspend fun updateVenue(id: Int, input: UpdateVenueInput): Resource<Venue> {
        return executeMutation(
            mutationName = "updateVenue(id=$id)",
            mutation = {
                apolloClient.mutation(UpdateVenueMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updateVenue.venueDetails.toVenue()
            }
        )
    }
}
