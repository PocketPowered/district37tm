package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ActiveConferenceQuery
import com.district37.toastmasters.graphql.AllLocationsQuery
import com.district37.toastmasters.graphql.SearchLocationsQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

class LocationsRepository(private val apolloClient: ApolloClient) {
    private suspend fun getActiveConferenceId(): Long? {
        return try {
            val response = apolloClient.query(ActiveConferenceQuery()).execute()
            if (response.hasErrors()) {
                return null
            }
            response.data?.conferencesCollection?.edges?.firstOrNull()?.node?.id
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAllLocations(): Resource<List<AllLocationsQuery.Node>> {
        return try {
            val conferenceId = getActiveConferenceId() ?: return Resource.Error(ErrorType.NOT_FOUND)
            val response = apolloClient.query(AllLocationsQuery(conferenceId)).execute()
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
            val conferenceId = getActiveConferenceId() ?: return Resource.Error(ErrorType.NOT_FOUND)
            val pattern = if (query.isBlank()) "%" else "%${query.trim()}%"
            val response = apolloClient.query(SearchLocationsQuery(conferenceId, pattern)).execute()
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
