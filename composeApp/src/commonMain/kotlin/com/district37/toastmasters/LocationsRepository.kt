package com.district37.toastmasters

import com.district37.toastmasters.models.Location
import com.wongislandd.nexus.networking.HttpMethod
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class LocationsRepository(okHttpClient: HttpClient) : NetworkClient(okHttpClient) {

    suspend fun getAllLocations(): Resource<List<Location>> {
        return makeRequest(
            "locations",
            HttpMethod.GET
        )
    }

    suspend fun searchLocationsByName(query: String): Resource<List<Location>> {
        return makeRequest(
            "locations/search",
            HttpMethod.GET,
        ) {
            parameter("q", query)
        }
    }
} 