package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.GetLocationQuery
import com.district37.toastmasters.graphql.GetLocationDetailsQuery
import com.district37.toastmasters.graphql.SearchLocationsQuery
import com.district37.toastmasters.graphql.UpdateLocationMutation
import com.district37.toastmasters.graphql.type.UpdateLocationInput
import com.district37.toastmasters.data.transformers.toLocation
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Location data
 * Fetches locations from GraphQL API using Apollo client
 */
class LocationRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient), BaseDetailRepository<Location>,
    BasePreviewRepository<Location> {

    override val tag = "LocationRepository"

    /**
     * Get a single location by ID (preview data)
     */
    suspend fun getLocation(id: Int): Resource<Location> {
        return executeQuery(
            queryName = "getLocation(id=$id)",
            query = { apolloClient.query(GetLocationQuery(id = id)).execute() },
            transform = { data -> data.location?.locationPreview?.toLocation() }
        )
    }

    /**
     * Get detailed location data by ID (includes images and all fields)
     */
    suspend fun getLocationDetails(id: Int): Resource<Location> {
        return executeQuery(
            queryName = "getLocationDetails(id=$id)",
            query = { apolloClient.query(GetLocationDetailsQuery(id = id)).execute() },
            transform = { data -> data.location?.locationDetails?.toLocation() }
        )
    }

    /**
     * BaseDetailRepository implementation: Get details for a single location
     */
    override suspend fun getDetails(id: Int): Resource<Location> = getLocationDetails(id)

    override suspend fun getPreview(id: Int): Resource<Location> = getLocation(id)

    /**
     * Search locations with optional venue filter and pagination
     *
     * @param venueId Optional venue ID to filter locations
     * @param searchQuery Optional search query to filter locations by name
     * @param cursor Optional cursor for pagination
     * @param first Number of items to fetch per page
     * @return Resource containing paginated locations
     */
    suspend fun searchLocations(
        venueId: Int? = null,
        searchQuery: String? = null,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Location>> {
        return executeQuery(
            queryName = "searchLocations(venueId=$venueId, query=$searchQuery, cursor=$cursor)",
            query = {
                apolloClient.query(
                    SearchLocationsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor),
                        venueId = Optional.presentIfNotNull(venueId),
                        searchQuery = Optional.presentIfNotNull(searchQuery)
                    )
                ).execute()
            },
            transform = { data ->
                data.locationsConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.locationPreview.toLocation()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Update an existing location
     *
     * @param id The ID of the location to update
     * @param input The update input containing fields to change
     * @return Resource containing the updated Location on success
     */
    suspend fun updateLocation(id: Int, input: UpdateLocationInput): Resource<Location> {
        return executeMutation(
            mutationName = "updateLocation(id=$id)",
            mutation = {
                apolloClient.mutation(UpdateLocationMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updateLocation.locationDetails.toLocation()
            }
        )
    }
}
