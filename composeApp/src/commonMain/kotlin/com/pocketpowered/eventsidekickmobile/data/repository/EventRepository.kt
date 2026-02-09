package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.ArchiveEventMutation
import com.district37.toastmasters.graphql.CreateEventMutation
import com.district37.toastmasters.graphql.CreateTestUpcomingEventMutation
import com.district37.toastmasters.graphql.DeleteEventMutation
import com.district37.toastmasters.graphql.GetEventQuery
import com.district37.toastmasters.graphql.GetEventsQuery
import com.district37.toastmasters.graphql.GetExplorePageQuery
import com.district37.toastmasters.graphql.UpdateEventMutation
import com.district37.toastmasters.graphql.type.CreateEventInput
import com.district37.toastmasters.graphql.type.UpdateEventInput
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.models.CarouselDisplayFormat
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.EventCarousel
import com.district37.toastmasters.models.EventConnection
import com.district37.toastmasters.models.ExplorePage
import com.district37.toastmasters.models.NearbyCity
import com.district37.toastmasters.util.Resource

/**
 * Repository for Event data
 * Fetches events from GraphQL API using Apollo client
 */
class EventRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient), BaseDetailRepository<Event>,
    BasePreviewRepository<Event> {

    override val tag = "EventRepository"

    /**
     * Get the explore page with event carousels.
     * All carousels (including upcoming events and nearby city) are in the carousels list.
     * The server controls the order and display format of each carousel.
     *
     * @param eventsPerCarousel Number of events to fetch per carousel
     * @param latitude Optional user latitude for location-based features
     * @param longitude Optional user longitude for location-based features
     * @param radiusMiles Optional radius in miles for nearby city matching (default 25)
     */
    suspend fun getExplorePage(
        eventsPerCarousel: Int = 7,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusMiles: Double? = null
    ): Resource<ExplorePage> {
        return executeQuery(
            queryName = "getExplorePage(eventsPerCarousel=$eventsPerCarousel, lat=$latitude, lon=$longitude)",
            query = {
                apolloClient.query(
                    GetExplorePageQuery(
                        eventsPerCarousel = eventsPerCarousel,
                        latitude = Optional.presentIfNotNull(latitude),
                        longitude = Optional.presentIfNotNull(longitude),
                        radiusMiles = Optional.presentIfNotNull(radiusMiles)
                    )
                ).execute()
            },
            transform = { data ->
                val carousels = data.explorePage.carousels.map { carousel ->
                    val events = carousel.events.map { it.eventPreview.toEvent() }
                    EventCarousel(
                        title = carousel.title,
                        events = events,
                        hasMore = carousel.hasMore,
                        totalCount = carousel.totalCount,
                        displayFormat = carousel.displayFormat.toLocalDisplayFormat(),
                        eventType = events.firstOrNull()?.eventType
                    )
                }

                // Transform nearby city if present (for metadata display)
                val nearbyCity = data.explorePage.nearbyCity?.let { city ->
                    NearbyCity(
                        city = city.city,
                        state = city.state,
                        eventCount = city.eventCount,
                        distanceMiles = city.distanceMiles
                    )
                }

                ExplorePage(
                    carousels = carousels,
                    nearbyCity = nearbyCity
                )
            }
        )
    }

    /**
     * Convert GraphQL CarouselDisplayFormat enum to local model enum.
     */
    private fun com.district37.toastmasters.graphql.type.CarouselDisplayFormat.toLocalDisplayFormat(): CarouselDisplayFormat {
        return when (this) {
            com.district37.toastmasters.graphql.type.CarouselDisplayFormat.HERO -> CarouselDisplayFormat.HERO
            com.district37.toastmasters.graphql.type.CarouselDisplayFormat.MEDIUM -> CarouselDisplayFormat.MEDIUM
            else -> CarouselDisplayFormat.MEDIUM // Default fallback for unknown values
        }
    }

    /**
     * Get a paginated list of events
     */
    suspend fun getEvents(
        first: Int? = 10,
        after: String? = null,
        searchQuery: String? = null,
        eventType: com.district37.toastmasters.graphql.type.EventType? = null
    ): Resource<EventConnection> {
        return executeQuery(
            queryName = "getEvents(first=$first, after=$after, searchQuery=$searchQuery, eventType=$eventType)",
            query = {
                apolloClient.query(
                    GetEventsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after),
                        last = Optional.absent(),
                        before = Optional.absent(),
                        searchQuery = Optional.presentIfNotNull(searchQuery),
                        eventType = Optional.presentIfNotNull(eventType)
                    )
                ).execute()
            },
            transform = { data ->
                data.eventsConnection.let { connection ->
                    val events = connection.edges.map { edge ->
                        edge.node.eventPreview.toEvent()
                    }
                    EventConnection(
                        events = events,
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Get a single event by ID
     */
    suspend fun getEvent(id: Int): Resource<Event> {
        return executeQuery(
            queryName = "getEvent(id=$id)",
            query = {
                apolloClient.query(GetEventQuery(id = id)).execute()
            },
            transform = { data ->
                data.event?.eventDetails?.toEvent()
            }
        )
    }

    /**
     * BaseDetailRepository implementation: Get details for a single event
     */
    override suspend fun getDetails(id: Int): Resource<Event> = getEvent(id)

    override suspend fun getPreview(id: Int): Resource<Event> = getEvent(id)

    /**
     * Create a new event
     *
     * @param input The event creation input containing name, description, and eventType
     * @return Resource containing the created Event on success
     */
    suspend fun createEvent(input: CreateEventInput): Resource<Event> {
        return executeMutation(
            mutationName = "createEvent(name=${input.name})",
            mutation = {
                apolloClient.mutation(CreateEventMutation(input = input)).execute()
            },
            transform = { data ->
                data.createEvent.eventDetails.toEvent()
            }
        )
    }

    /**
     * Update an existing event
     *
     * @param id The ID of the event to update
     * @param input The event update input containing fields to update
     * @return Resource containing the updated Event on success
     */
    suspend fun updateEvent(id: Int, input: UpdateEventInput): Resource<Event> {
        return executeMutation(
            mutationName = "updateEvent(id=$id)",
            mutation = {
                apolloClient.mutation(UpdateEventMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updateEvent.eventDetails.toEvent()
            }
        )
    }

    /**
     * Delete an event
     *
     * @param id The ID of the event to delete
     * @return Resource containing true on success
     */
    suspend fun deleteEvent(id: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "deleteEvent(id=$id)",
            mutation = {
                apolloClient.mutation(DeleteEventMutation(id = id)).execute()
            },
            transform = { data ->
                data.deleteEvent
            }
        )
    }

    /**
     * Archive an event by setting its archived_at timestamp.
     * Archived events remain accessible via direct link but don't appear in discovery flows.
     * They cannot be edited once archived.
     *
     * @param id The ID of the event to archive
     * @return Resource containing the archived Event on success
     */
    suspend fun archiveEvent(id: Int): Resource<Event> {
        return executeMutation(
            mutationName = "archiveEvent(id=$id)",
            mutation = {
                apolloClient.mutation(ArchiveEventMutation(id = id)).execute()
            },
            transform = { data ->
                data.archiveEvent.eventDetails.toEvent()
            }
        )
    }

    /**
     * Create a test event happening in 24 hours for testing the upcoming events carousel.
     * Automatically saves the event for the authenticated user.
     * This is a developer/testing endpoint.
     *
     * @return Resource containing the created Event on success
     */
    suspend fun createTestUpcomingEvent(): Resource<Event> {
        return executeMutation(
            mutationName = "createTestUpcomingEvent",
            mutation = {
                apolloClient.mutation(CreateTestUpcomingEventMutation()).execute()
            },
            transform = { data ->
                data.createTestUpcomingEvent.eventDetails.toEvent()
            }
        )
    }
}
