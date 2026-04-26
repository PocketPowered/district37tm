package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.AllResourcesQuery
import com.district37.toastmasters.models.ExternalLink
import com.district37.toastmasters.resources.ResourceLinkTransformer
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

data class ResourceWithCategory(
    val resourceType: String,
    val link: ExternalLink
)

class ResourcesRepository(
    private val apolloClient: ApolloClient,
    private val resourceLinkTransformer: ResourceLinkTransformer,
    private val resolver: ActiveConferenceResolver
) {
    suspend fun getAllResources(): Resource<List<ResourceWithCategory>> {
        return try {
            val conference = resolver.resolve()
            if (conference !is Resource.Success) {
                return Resource.Error(ErrorType.NOT_FOUND)
            }
            val response = apolloClient.query(AllResourcesQuery(conference.data.id)).execute()
            if (response.hasErrors()) {
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val resources = response.data?.resourcesCollection?.edges
                ?.asSequence()
                ?.map { it.node }
                ?.mapNotNull { node ->
                    val resourceType = node.resource_type.trim()
                    if (resourceType.isEmpty()) return@mapNotNull null

                    val link = resourceLinkTransformer.transform(
                        displayName = node.display_name,
                        url = node.url,
                        description = node.description
                    ) ?: return@mapNotNull null

                    ResourceWithCategory(
                        resourceType = resourceType,
                        link = link
                    )
                }
                ?.toList()
                ?: emptyList()

            Resource.Success(resources)
        } catch (e: Exception) {
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }
}
