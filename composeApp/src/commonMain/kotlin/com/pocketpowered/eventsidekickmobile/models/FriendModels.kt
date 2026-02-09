package com.district37.toastmasters.models

import com.district37.toastmasters.graphql.type.UserEngagementStatus
import kotlinx.datetime.Instant

/**
 * Represents a pending friend request
 */
data class FriendRequest(
    val id: Int,
    val senderId: String,
    val receiverId: String,
    val createdAt: Instant,
    val senderDisplayName: String? = null,
    val senderProfileImageUrl: String? = null
)

/**
 * Connection wrapper for paginated friend requests
 */
data class FriendRequestConnection(
    val requests: List<FriendRequest>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
)

/**
 * Connection wrapper for paginated friends list
 */
data class FriendsConnection(
    val friends: List<User>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
)

/**
 * Represents the relationship status between current user and another user.
 * Used when viewing another user's profile to determine what actions are available.
 */
sealed class UserRelationshipStatus {
    /** No relationship exists between the users */
    data object NotFriends : UserRelationshipStatus()

    /** Current user has sent a friend request to the other user */
    data class PendingOutgoing(val request: FriendRequest) : UserRelationshipStatus()

    /** Other user has sent a friend request to the current user */
    data class PendingIncoming(val request: FriendRequest) : UserRelationshipStatus()

    /** The users are friends */
    data object Friends : UserRelationshipStatus()
}

/**
 * Represents a friend's RSVP to an event or schedule item
 */
data class FriendRsvp(
    val userId: String,
    val status: UserEngagementStatus,
    val displayName: String?,
    val profileImageUrl: String?
)

/**
 * Connection wrapper for paginated friend RSVPs
 */
data class FriendRsvpConnection(
    val rsvps: List<FriendRsvp>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
) {
    companion object {
        val EMPTY = FriendRsvpConnection(
            rsvps = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}
