package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.GetCreateHubQuery
import com.district37.toastmasters.graphql.GetMyEditableEventsQuery
import com.district37.toastmasters.graphql.GetMyEditableVenuesQuery
import com.district37.toastmasters.graphql.GetMyEditablePerformersQuery
import com.district37.toastmasters.graphql.GetMyArchivedEventsQuery
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.data.transformers.toVenue
import com.district37.toastmasters.data.transformers.toPerformer
import com.district37.toastmasters.models.CreateHub
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.models.EventsWithPagination
import com.district37.toastmasters.models.VenuesWithPagination
import com.district37.toastmasters.models.PerformersWithPagination
import com.district37.toastmasters.models.OrganizationsWithPagination
import com.district37.toastmasters.data.transformers.toOrganization
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Create Hub data
 * Fetches user's editable entities (owned + collaborated) from GraphQL API
 */
class CreateHubRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "CreateHubRepository"

    /**
     * Get the Create Hub data containing user's editable entities
     * Requires authentication - will fail if user is not logged in
     */
    suspend fun getCreateHub(): Resource<CreateHub> {
        return executeQuery(
            queryName = "getCreateHub",
            query = {
                apolloClient.query(GetCreateHubQuery()).execute()
            },
            transform = { data ->
                CreateHub(
                    myEvents = EventsWithPagination(
                        items = data.createHub.myEvents.items.map { it.eventPreview.toEvent() },
                        totalCount = data.createHub.myEvents.totalCount,
                        hasMore = data.createHub.myEvents.hasMore
                    ),
                    myVenues = VenuesWithPagination(
                        items = data.createHub.myVenues.items.map { it.venuePreview.toVenue() },
                        totalCount = data.createHub.myVenues.totalCount,
                        hasMore = data.createHub.myVenues.hasMore
                    ),
                    myPerformers = PerformersWithPagination(
                        items = data.createHub.myPerformers.items.map { it.performerPreview.toPerformer() },
                        totalCount = data.createHub.myPerformers.totalCount,
                        hasMore = data.createHub.myPerformers.hasMore
                    ),
                    myOrganizations = OrganizationsWithPagination(
                        items = data.createHub.myOrganizations.items.map { it.organizationPreview.toOrganization() },
                        totalCount = data.createHub.myOrganizations.totalCount,
                        hasMore = data.createHub.myOrganizations.hasMore
                    )
                )
            }
        )
    }

    /**
     * Get paginated editable events (owned + collaborated)
     * For "View All" screen
     */
    suspend fun getMyEditableEvents(
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Event>> {
        return executeQuery(
            queryName = "getMyEditableEvents(cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMyEditableEventsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor)
                    )
                ).execute()
            },
            transform = { data ->
                data.myEditableEventsConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.eventPreview.toEvent()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Get paginated editable venues (owned + collaborated)
     * For "View All" screen
     */
    suspend fun getMyEditableVenues(
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Venue>> {
        return executeQuery(
            queryName = "getMyEditableVenues(cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMyEditableVenuesQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor)
                    )
                ).execute()
            },
            transform = { data ->
                data.myEditableVenuesConnection.let { connection ->
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
     * Get paginated editable performers (owned + collaborated)
     * For "View All" screen
     */
    suspend fun getMyEditablePerformers(
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Performer>> {
        return executeQuery(
            queryName = "getMyEditablePerformers(cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMyEditablePerformersQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor)
                    )
                ).execute()
            },
            transform = { data ->
                data.myEditablePerformersConnection.let { connection ->
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
     * Get paginated archived events
     * For "Archived Events" screen in Utilities section
     */
    suspend fun getMyArchivedEvents(
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Event>> {
        return executeQuery(
            queryName = "getMyArchivedEvents(cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMyArchivedEventsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor)
                    )
                ).execute()
            },
            transform = { data ->
                data.myArchivedEventsConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.eventPreview.toEvent()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }
}
