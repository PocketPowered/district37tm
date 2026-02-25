package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.AvailableDatesQuery
import com.district37.toastmasters.graphql.EventDetailsQuery
import com.district37.toastmasters.graphql.EventPreviewsByDateQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

class EventRepository(private val apolloClient: ApolloClient) {

    suspend fun getEventDetails(id: Int): Resource<EventDetailsQuery.Node> {
        return try {
            val response = apolloClient.query(EventDetailsQuery(id.toLong())).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val node = response.data?.eventsCollection?.edges?.firstOrNull()?.node
                ?: return Resource.Error(ErrorType.NOT_FOUND)

            Resource.Success(node)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getEventsByKey(dateKey: Long): Resource<List<EventPreviewsByDateQuery.Node>> {
        return try {
            val response = apolloClient.query(EventPreviewsByDateQuery(dateKey)).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val previews = response.data?.eventsCollection?.edges?.map { it.node } ?: emptyList()

            Resource.Success(previews)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getAvailableDates(): Resource<List<Long>> {
        return try {
            val response = apolloClient.query(AvailableDatesQuery()).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val dates = response.data?.conference_datesCollection?.edges?.map { it.node.date_key } ?: emptyList()
            Resource.Success(dates)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }
}
