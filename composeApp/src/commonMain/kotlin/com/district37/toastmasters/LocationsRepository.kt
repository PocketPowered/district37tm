package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.AllLocationsQuery
import com.district37.toastmasters.graphql.SearchLocationsQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

class LocationsRepository(
    private val apolloClient: ApolloClient,
    private val resolver: ActiveConferenceResolver
) {
    suspend fun getAllLocations(): Resource<List<AllLocationsQuery.Node>> {
        return try {
            val conference = resolver.resolve()
            if (conference !is Resource.Success) {
                return Resource.Error(ErrorType.NOT_FOUND)
            }
            val response = apolloClient.query(AllLocationsQuery(conference.data.id)).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }
            val locations = response.data?.locationsCollection?.edges?.map { it.node } ?: emptyList()
            Resource.Success(locations)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun searchLocationsByName(query: String): Resource<List<SearchLocationsQuery.Node>> {
        return try {
            val conference = resolver.resolve()
            if (conference !is Resource.Success) {
                return Resource.Error(ErrorType.NOT_FOUND)
            }
            val pattern = if (query.isBlank()) "%" else "%${query.trim()}%"
            val response = apolloClient.query(SearchLocationsQuery(conference.data.id, pattern)).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }
            val locations = response.data?.locationsCollection?.edges?.map { it.node } ?: emptyList()
            Resource.Success(locations)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }
}
