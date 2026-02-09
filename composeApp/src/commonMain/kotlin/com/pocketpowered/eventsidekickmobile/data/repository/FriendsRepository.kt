package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.AcceptFriendRequestMutation
import com.district37.toastmasters.graphql.CancelFriendRequestMutation
import com.district37.toastmasters.graphql.GetMyFriendsQuery
import com.district37.toastmasters.graphql.GetMyIncomingFriendRequestsQuery
import com.district37.toastmasters.graphql.GetRelationshipStatusQuery
import com.district37.toastmasters.graphql.RejectFriendRequestMutation
import com.district37.toastmasters.graphql.RemoveFriendMutation
import com.district37.toastmasters.graphql.SendFriendRequestMutation
import com.district37.toastmasters.models.FriendRequest
import com.district37.toastmasters.models.FriendRequestConnection
import com.district37.toastmasters.models.FriendsConnection
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.UserRelationshipStatus
import com.district37.toastmasters.util.Resource
import kotlinx.datetime.Instant

/**
 * Repository for friend-related operations
 */
class FriendsRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "FriendsRepository"

    /**
     * Get the current user's friends list with pagination info
     */
    suspend fun getMyFriends(
        first: Int = 50,
        after: String? = null
    ): Resource<FriendsConnection> {
        return executeQuery(
            queryName = "getMyFriends(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyFriendsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val friends = data.myFriends.edges.mapNotNull { edge ->
                    edge.node.user?.userPreview?.let { userPreview ->
                        User(
                            id = edge.node.userId,
                            email = "", // Not available in this query
                            username = edge.node.user.username,
                            displayName = userPreview.displayName,
                            profileImageUrl = userPreview.profileImageUrl
                        )
                    }
                }
                FriendsConnection(
                    friends = friends,
                    hasNextPage = data.myFriends.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.myFriends.pageInfo.paginationInfo.endCursor,
                    totalCount = data.myFriends.totalCount
                )
            }
        )
    }

    /**
     * Get incoming friend requests for the current user
     */
    suspend fun getMyIncomingFriendRequests(
        first: Int = 20,
        after: String? = null
    ): Resource<FriendRequestConnection> {
        return executeQuery(
            queryName = "getMyIncomingFriendRequests(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyIncomingFriendRequestsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val requests = data.myIncomingFriendRequests.edges.map { edge ->
                    FriendRequest(
                        id = edge.node.id,
                        senderId = edge.node.senderId,
                        receiverId = edge.node.receiverId,
                        createdAt = Instant.parse(edge.node.createdAt.toString()),
                        senderDisplayName = edge.node.sender?.userPreview?.displayName,
                        senderProfileImageUrl = edge.node.sender?.userPreview?.profileImageUrl
                    )
                }
                FriendRequestConnection(
                    requests = requests,
                    hasNextPage = data.myIncomingFriendRequests.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.myIncomingFriendRequests.pageInfo.paginationInfo.endCursor,
                    totalCount = data.myIncomingFriendRequests.totalCount
                )
            }
        )
    }

    /**
     * Get the relationship status between the current user and another user
     */
    suspend fun getRelationshipStatus(userId: String): Resource<UserRelationshipStatus> {
        return executeQuery(
            queryName = "getRelationshipStatus(userId=$userId)",
            query = {
                apolloClient.query(GetRelationshipStatusQuery(userId = userId)).execute()
            },
            transform = { data ->
                val status = data.relationshipStatus
                when {
                    status.isFriend -> UserRelationshipStatus.Friends
                    status.pendingIncomingRequest != null -> {
                        val request = status.pendingIncomingRequest
                        UserRelationshipStatus.PendingIncoming(
                            FriendRequest(
                                id = request.id,
                                senderId = request.senderId,
                                receiverId = request.receiverId,
                                createdAt = Instant.parse(request.createdAt.toString())
                            )
                        )
                    }
                    status.pendingOutgoingRequest != null -> {
                        val request = status.pendingOutgoingRequest
                        UserRelationshipStatus.PendingOutgoing(
                            FriendRequest(
                                id = request.id,
                                senderId = request.senderId,
                                receiverId = request.receiverId,
                                createdAt = Instant.parse(request.createdAt.toString())
                            )
                        )
                    }
                    else -> UserRelationshipStatus.NotFriends
                }
            }
        )
    }

    /**
     * Send a friend request to a user
     */
    suspend fun sendFriendRequest(receiverId: String): Resource<FriendRequest> {
        return executeMutation(
            mutationName = "sendFriendRequest",
            mutation = { apolloClient.mutation(SendFriendRequestMutation(receiverId = receiverId)).execute() },
            transform = { data ->
                val request = data.sendFriendRequest
                FriendRequest(
                    id = request.id,
                    senderId = request.senderId,
                    receiverId = request.receiverId,
                    createdAt = Instant.parse(request.createdAt.toString())
                )
            }
        )
    }

    /**
     * Cancel a pending friend request
     */
    suspend fun cancelFriendRequest(requestId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "cancelFriendRequest",
            mutation = { apolloClient.mutation(CancelFriendRequestMutation(requestId = requestId)).execute() },
            transform = { data -> data.cancelFriendRequest }
        )
    }

    /**
     * Accept an incoming friend request
     */
    suspend fun acceptFriendRequest(requestId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "acceptFriendRequest",
            mutation = { apolloClient.mutation(AcceptFriendRequestMutation(requestId = requestId)).execute() },
            transform = { data -> data.acceptFriendRequest != null }
        )
    }

    /**
     * Reject an incoming friend request
     */
    suspend fun rejectFriendRequest(requestId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "rejectFriendRequest",
            mutation = { apolloClient.mutation(RejectFriendRequestMutation(requestId = requestId)).execute() },
            transform = { data -> data.rejectFriendRequest }
        )
    }

    /**
     * Remove a friend (unfriend)
     */
    suspend fun removeFriend(friendId: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeFriend",
            mutation = { apolloClient.mutation(RemoveFriendMutation(friendId = friendId)).execute() },
            transform = { data -> data.removeFriend }
        )
    }
}
