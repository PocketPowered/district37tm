package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import com.wongislandd.nexus.networking.HttpMethod
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient

class ResourcesRepository(okHttpClient: HttpClient) : NetworkClient(okHttpClient) {

    suspend fun getAllResources(): Resource<List<BackendExternalLink>> {
        return makeRequest(
            "resources",
            HttpMethod.GET
        )
    }
} 