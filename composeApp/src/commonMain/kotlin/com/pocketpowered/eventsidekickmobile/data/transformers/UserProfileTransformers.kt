package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.GetMyProfileQuery
import com.district37.toastmasters.graphql.GetUserProfileQuery
import com.district37.toastmasters.graphql.UpdateMyProfileMutation
import com.district37.toastmasters.models.ActivityFeedConnection
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.models.FriendRequest
import com.district37.toastmasters.models.FriendRequestConnection
import com.district37.toastmasters.models.MyProfile
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.models.UserPreferences
import kotlinx.datetime.Instant

// ========== GetMyProfile (Unified Self-Profile) Transformers ==========

/**
 * Transforms GraphQL GetMyProfile query result to domain MyProfile
 */
fun GetMyProfileQuery.GetMyProfile.toMyProfile(): MyProfile {
    return MyProfile(
        user = user.toUserFromMyProfile(),
        subscribedEvents = subscribedEvents.toUserEventsConnectionFromMyProfile(),
        attendingEvents = attendingEvents.toUserEventsConnectionFromMyProfile(),
        activityFeed = activityFeed.toActivityFeedConnectionFromMyProfile(),
        incomingFriendRequests = incomingFriendRequests.toFriendRequestConnectionFromMyProfile(),
        friendsCount = friendsCount
    )
}

/**
 * Transforms user from GetMyProfile query to domain User
 */
fun GetMyProfileQuery.User.toUserFromMyProfile(): User {
    return User(
        id = id,
        email = "",  // Email comes from auth state, not GraphQL
        username = username,
        displayName = displayName,
        bio = bio,
        profileImageUrl = profileImageUrl,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        preferences = preferences?.toUserPreferencesFromMyProfile(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Transforms preferences from GetMyProfile query to domain UserPreferences
 */
fun GetMyProfileQuery.Preferences.toUserPreferencesFromMyProfile(): UserPreferences {
    return UserPreferences(
        theme = theme,
        notificationsEnabled = notificationsEnabled,
        displayDensity = displayDensity
    )
}

/**
 * Transforms subscribed events from GetMyProfile query to domain UserEventsConnection
 */
fun GetMyProfileQuery.SubscribedEvents.toUserEventsConnectionFromMyProfile(): UserEventsConnection {
    return UserEventsConnection(
        events = edges.mapNotNull { edge ->
            edge.node.event?.eventPreview?.toEvent()
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

/**
 * Transforms attending events from GetMyProfile query to domain UserEventsConnection
 */
fun GetMyProfileQuery.AttendingEvents.toUserEventsConnectionFromMyProfile(): UserEventsConnection {
    return UserEventsConnection(
        events = edges.mapNotNull { edge ->
            edge.node.event?.eventPreview?.toEvent()
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

/**
 * Transforms activity feed from GetMyProfile query to domain ActivityFeedConnection
 */
fun GetMyProfileQuery.ActivityFeed.toActivityFeedConnectionFromMyProfile(): ActivityFeedConnection {
    return ActivityFeedConnection(
        items = edges.map { edge ->
            edge.node.toActivityFeedItemFromMyProfile()
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

/**
 * Transforms activity feed item from GetMyProfile query to domain ActivityFeedItem
 */
fun GetMyProfileQuery.Node2.toActivityFeedItemFromMyProfile(): ActivityFeedItem {
    return ActivityFeedItem(
        id = id,
        userId = userId,
        activityType = activityType,
        entityType = entityType,
        entityId = entityId,
        createdAt = createdAt,
        user = user?.userPreview?.toUserProfilePreview(),
        event = event?.eventPreview?.toEvent(),
        agendaItem = agendaItem?.agendaItemPreview?.toAgendaItem(),
        venue = venue?.venuePreview?.toVenue(),
        performer = performer?.performerPreview?.toPerformer(),
        location = location?.locationPreview?.toLocation(),
        organization = organization?.organizationPreview?.toOrganization(),
        isCurrentUser = true  // This is the user's own profile
    )
}

/**
 * Transforms friend requests from GetMyProfile query to domain FriendRequestConnection
 */
fun GetMyProfileQuery.IncomingFriendRequests.toFriendRequestConnectionFromMyProfile(): FriendRequestConnection {
    return FriendRequestConnection(
        requests = edges.map { edge ->
            FriendRequest(
                id = edge.node.id,
                senderId = edge.node.senderId,
                receiverId = edge.node.receiverId,
                createdAt = Instant.parse(edge.node.createdAt.toString()),
                senderDisplayName = edge.node.sender?.userPreview?.displayName,
                senderProfileImageUrl = edge.node.sender?.userPreview?.profileImageUrl
            )
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

// ========== UpdateMyProfile Mutation Transformers ==========

/**
 * Transforms GraphQL UpdateMyProfile mutation result to domain User
 */
fun UpdateMyProfileMutation.UpdateMyUser.toUser(): User {
    return User(
        id = id,
        email = "",  // Email comes from auth state
        username = username,
        displayName = displayName,
        bio = bio,
        profileImageUrl = profileImageUrl,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        preferences = preferences?.toUserPreferences(),
        createdAt = null,
        updatedAt = updatedAt
    )
}

/**
 * Transforms GraphQL mutation preferences to domain UserPreferences
 */
fun UpdateMyProfileMutation.Preferences.toUserPreferences(): UserPreferences {
    return UserPreferences(
        theme = theme,
        notificationsEnabled = notificationsEnabled,
        displayDensity = displayDensity
    )
}

// ========== GetUserProfile (View Other User) Transformers ==========

/**
 * Transforms GraphQL GetUserProfile query result to domain UserProfile
 */
fun GetUserProfileQuery.GetUserProfile.toUserProfile(): com.district37.toastmasters.models.UserProfile {
    return com.district37.toastmasters.models.UserProfile(
        user = user.toUserFromProfile(),
        relationshipStatus = relationshipStatus.toUserRelationshipStatus(),
        attendingEvents = attendingEvents?.toUserEventsConnection(),
        activityFeed = activityFeed?.toActivityFeedConnection()
    )
}

/**
 * Transforms user from GetUserProfile query to domain User
 */
fun GetUserProfileQuery.User.toUserFromProfile(): User {
    return User(
        id = userPreview.id,
        email = "",
        username = username,
        displayName = userPreview.displayName,
        bio = bio,
        profileImageUrl = userPreview.profileImageUrl,
        primaryColor = primaryColor,
        preferences = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Transforms relationship status from GetUserProfile query to domain UserRelationshipStatus
 */
fun GetUserProfileQuery.RelationshipStatus.toUserRelationshipStatus(): com.district37.toastmasters.models.UserRelationshipStatus {
    return if (isFriend) {
        com.district37.toastmasters.models.UserRelationshipStatus.Friends
    } else if (pendingIncomingRequest != null) {
        val request = com.district37.toastmasters.models.FriendRequest(
            id = pendingIncomingRequest.id,
            senderId = pendingIncomingRequest.senderId,
            receiverId = pendingIncomingRequest.receiverId,
            createdAt = pendingIncomingRequest.createdAt
        )
        com.district37.toastmasters.models.UserRelationshipStatus.PendingIncoming(request)
    } else if (pendingOutgoingRequest != null) {
        val request = com.district37.toastmasters.models.FriendRequest(
            id = pendingOutgoingRequest.id,
            senderId = pendingOutgoingRequest.senderId,
            receiverId = pendingOutgoingRequest.receiverId,
            createdAt = pendingOutgoingRequest.createdAt
        )
        com.district37.toastmasters.models.UserRelationshipStatus.PendingOutgoing(request)
    } else {
        com.district37.toastmasters.models.UserRelationshipStatus.NotFriends
    }
}

/**
 * Transforms attending events from GetUserProfile query to domain UserEventsConnection
 */
fun GetUserProfileQuery.AttendingEvents.toUserEventsConnection(): com.district37.toastmasters.models.UserEventsConnection {
    return com.district37.toastmasters.models.UserEventsConnection(
        events = edges.mapNotNull { edge ->
            edge.node.event?.eventPreview?.toEvent()
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

/**
 * Transforms activity feed from GetUserProfile query to domain ActivityFeedConnection
 */
fun GetUserProfileQuery.ActivityFeed.toActivityFeedConnection(): com.district37.toastmasters.models.ActivityFeedConnection {
    return com.district37.toastmasters.models.ActivityFeedConnection(
        items = edges.map { edge ->
            edge.node.toActivityFeedItemFromProfile()
        },
        hasNextPage = pageInfo.paginationInfo.hasNextPage,
        endCursor = pageInfo.paginationInfo.endCursor,
        totalCount = totalCount
    )
}

/**
 * Transforms activity feed item from GetUserProfile query to domain ActivityFeedItem
 */
fun GetUserProfileQuery.Node1.toActivityFeedItemFromProfile(): com.district37.toastmasters.models.ActivityFeedItem {
    return com.district37.toastmasters.models.ActivityFeedItem(
        id = id,
        userId = userId,
        activityType = activityType,
        entityType = entityType,
        entityId = entityId,
        createdAt = createdAt,
        user = user?.userPreview?.toUserProfilePreview(),
        event = event?.eventPreview?.toEvent(),
        agendaItem = agendaItem?.agendaItemPreview?.toAgendaItem(),
        venue = venue?.venuePreview?.toVenue(),
        performer = performer?.performerPreview?.toPerformer(),
        location = location?.locationPreview?.toLocation(),
        organization = organization?.organizationPreview?.toOrganization(),
        isCurrentUser = isCurrentUser
    )
}
