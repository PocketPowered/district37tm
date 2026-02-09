package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.UserPreview as GraphQLUserPreview
import com.district37.toastmasters.graphql.GetFriendActivityFeedQuery
import com.district37.toastmasters.graphql.GetMyActivityFeedQuery
import com.district37.toastmasters.graphql.GetUserActivityFeedQuery
import com.district37.toastmasters.graphql.GetUserProfileQuery
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.models.UserProfilePreview

/**
 * Transforms GraphQL UserPreview fragment to domain UserProfilePreview model
 */
fun GraphQLUserPreview.toUserProfilePreview(): UserProfilePreview {
    return UserProfilePreview(
        id = id,
        displayName = displayName,
        profileImageUrl = profileImageUrl
    )
}

/**
 * Transforms GraphQL ActivityFeedItem node to domain ActivityFeedItem model
 */
fun GetFriendActivityFeedQuery.Node.toActivityFeedItem(): ActivityFeedItem {
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
        isCurrentUser = isCurrentUser
    )
}

/**
 * Transforms GraphQL UserActivityFeed node to domain ActivityFeedItem model
 */
fun GetUserActivityFeedQuery.Node.toActivityFeedItem(): ActivityFeedItem {
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
        organization = organization?.organizationPreview?.toOrganization()
    )
}

/**
 * Transforms GraphQL MyActivityFeed node to domain ActivityFeedItem model
 */
fun GetMyActivityFeedQuery.Node.toMyActivityFeedItem(): ActivityFeedItem {
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
        organization = organization?.organizationPreview?.toOrganization()
    )
}

/**
 * Transforms GraphQL GetUserProfile ActivityFeed node to domain ActivityFeedItem model
 */
fun GetUserProfileQuery.Node1.toActivityFeedItem(): ActivityFeedItem {
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
        isCurrentUser = isCurrentUser
    )
}
