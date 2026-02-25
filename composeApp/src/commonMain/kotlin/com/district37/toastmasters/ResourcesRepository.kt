package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ResourcesByTypeQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

class ResourcesRepository(private val apolloClient: ApolloClient) {

    private suspend fun getResourcesByType(resourceType: String): Resource<List<ResourcesByTypeQuery.Node>> {
        return try {
            val response = apolloClient.query(ResourcesByTypeQuery(resourceType)).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }
            val resources = response.data?.resourcesCollection?.edges?.map { it.node } ?: emptyList()
            Resource.Success(resources)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getAllResources(): Resource<List<ResourcesByTypeQuery.Node>> {
        return getResourcesByType("general")
    }

    suspend fun getAllFirstTimerResources(): Resource<List<ResourcesByTypeQuery.Node>> {
        return getResourcesByType("first_timer")
    }
}
