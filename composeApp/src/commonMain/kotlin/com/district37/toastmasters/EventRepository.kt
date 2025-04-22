package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendTabInfo
import com.wongislandd.nexus.networking.HttpMethod
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder

class EventRepository(okHttpClient: HttpClient) : NetworkClient(okHttpClient) {

    suspend fun getEventDetails(id: Int): Resource<BackendEventDetails> {
        return makeRequest(
            "event/${id}",
            HttpMethod.GET
        )
    }

    suspend fun getEventsByKey(dateKey: String): Resource<List<BackendEventPreview>> {
        return makeRequest(
            "events",
            HttpMethod.GET,
        ) {
            url.parameters.append("date", dateKey)
        }
    }

    suspend fun getEventsByIds(ids: List<Int>): Resource<List<BackendEventPreview>> {
        return makeRequest(
            "events/favorites",
            HttpMethod.GET,
        ) {
            url.parameters.append("ids", ids.joinToString(","))
        }
    }

    suspend fun getAvailableTabs(): Resource<List<BackendTabInfo>> {
        return makeRequest(
            "availableTabs",
            HttpMethod.GET
        )
    }
}