package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.GetFriendRsvpsForEntityQuery
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.FriendRsvp
import com.district37.toastmasters.models.FriendRsvpConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Friend RSVP data
 * Fetches friend RSVPs for events and schedule items from GraphQL API
 */
class FriendRsvpRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "FriendRsvpRepository"

    /**
     * Get friend RSVPs for a specific entity (preview - first 3)
     */
    suspend fun getFriendRsvpsPreview(
        entityType: EntityType,
        entityId: Int
    ): Resource<FriendRsvpConnection> {
        return getFriendRsvps(entityType, entityId, first = 3)
    }

    /**
     * Get paginated friend RSVPs for a specific entity
     */
    suspend fun getFriendRsvps(
        entityType: EntityType,
        entityId: Int,
        first: Int = 10,
        after: String? = null
    ): Resource<FriendRsvpConnection> {
        return executeQuery(
            queryName = "getFriendRsvps(type=$entityType, id=$entityId, first=$first)",
            query = {
                apolloClient.query(
                    GetFriendRsvpsForEntityQuery(
                        entityType = entityType,
                        entityId = entityId,
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val rsvps = data.friendRsvpsForEntity.edges.mapNotNull { edge ->
                    val node = edge.node
                    // Only include RSVPs with non-null status
                    node.status?.let { status ->
                        FriendRsvp(
                            userId = node.userId,
                            status = status,
                            displayName = node.displayName,
                            profileImageUrl = node.profileImageUrl
                        )
                    }
                }

                FriendRsvpConnection(
                    rsvps = rsvps,
                    hasNextPage = data.friendRsvpsForEntity.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.friendRsvpsForEntity.pageInfo.paginationInfo.endCursor,
                    totalCount = data.friendRsvpsForEntity.totalCount
                )
            }
        )
    }
}
