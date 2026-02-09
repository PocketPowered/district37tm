package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.OmnisearchQuery
import com.district37.toastmasters.data.transformers.toOmnisearchResult
import com.district37.toastmasters.models.OmnisearchResult
import com.district37.toastmasters.util.Resource

/**
 * Repository for search functionality
 */
class SearchRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "SearchRepository"

    /**
     * Perform an omnisearch across events, venues, performers, and users
     *
     * @param query The search query string
     * @param limit Maximum number of results to return (default 20)
     * @param eventId Optional event ID to scope the search within an event (for future use)
     */
    suspend fun omnisearch(
        query: String,
        limit: Int = 20,
        eventId: Int? = null
    ): Resource<OmnisearchResult> {
        return executeQuery(
            queryName = "omnisearch(query=$query, limit=$limit, eventId=$eventId)",
            query = {
                apolloClient.query(
                    OmnisearchQuery(
                        query = query,
                        limit = Optional.presentIfNotNull(limit),
                        eventId = Optional.presentIfNotNull(eventId)
                    )
                ).execute()
            },
            transform = { data ->
                data.omnisearch.toOmnisearchResult()
            }
        )
    }
}
