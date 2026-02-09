package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.GetFriendActivityFeedQuery
import com.district37.toastmasters.graphql.GetMyActivityFeedQuery
import com.district37.toastmasters.graphql.GetUserActivityFeedQuery
import com.district37.toastmasters.data.transformers.toActivityFeedItem
import com.district37.toastmasters.data.transformers.toMyActivityFeedItem
import com.district37.toastmasters.models.ActivityFeedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Activity Feed data
 * Fetches friend activity from GraphQL API using Apollo client
 */
class ActivityFeedRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "ActivityFeedRepository"

    /**
     * Get friend activity feed with pagination
     * @param includeSelf If true, also includes the current user's own activity in the feed
     */
    suspend fun getFriendActivityFeed(
        first: Int = 20,
        after: String? = null,
        includeSelf: Boolean = false
    ): Resource<ActivityFeedConnection> {
        return executeQuery(
            queryName = "getFriendActivityFeed(first=$first, after=$after, includeSelf=$includeSelf)",
            query = {
                apolloClient.query(
                    GetFriendActivityFeedQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after),
                        includeSelf = Optional.present(includeSelf)
                    )
                ).execute()
            },
            transform = { data ->
                val items = data.friendActivityFeed.edges.map { edge ->
                    edge.node.toActivityFeedItem()
                }
                ActivityFeedConnection(
                    items = items,
                    hasNextPage = data.friendActivityFeed.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.friendActivityFeed.pageInfo.paginationInfo.endCursor,
                    totalCount = data.friendActivityFeed.totalCount
                )
            }
        )
    }

    /**
     * Get activity feed for a specific user (must be friends)
     */
    suspend fun getUserActivityFeed(
        userId: String,
        first: Int = 20,
        after: String? = null
    ): Resource<ActivityFeedConnection> {
        return executeQuery(
            queryName = "getUserActivityFeed(userId=$userId, first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetUserActivityFeedQuery(
                        userId = userId,
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val items = data.userActivityFeed.edges.map { edge ->
                    edge.node.toActivityFeedItem()
                }
                ActivityFeedConnection(
                    items = items,
                    hasNextPage = data.userActivityFeed.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.userActivityFeed.pageInfo.paginationInfo.endCursor,
                    totalCount = data.userActivityFeed.totalCount
                )
            }
        )
    }

    /**
     * Get the current user's own activity feed
     */
    suspend fun getMyActivityFeed(
        first: Int = 20,
        after: String? = null
    ): Resource<ActivityFeedConnection> {
        return executeQuery(
            queryName = "getMyActivityFeed(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyActivityFeedQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val items = data.myActivityFeed.edges.map { edge ->
                    edge.node.toMyActivityFeedItem()
                }
                ActivityFeedConnection(
                    items = items,
                    hasNextPage = data.myActivityFeed.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.myActivityFeed.pageInfo.paginationInfo.endCursor,
                    totalCount = data.myActivityFeed.totalCount
                )
            }
        )
    }
}
