package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.NotificationPreview
import com.district37.toastmasters.graphql.type.NotificationType as GraphQLNotificationType
import com.district37.toastmasters.graphql.type.Platform as GraphQLPlatform
import com.district37.toastmasters.models.Notification
import com.district37.toastmasters.models.NotificationType
import com.district37.toastmasters.models.Platform
import kotlinx.datetime.Instant

/**
 * Transforms GraphQL NotificationPreview fragment to domain Notification model
 */
fun NotificationPreview.toNotification(): Notification {
    return Notification(
        id = id,
        notificationType = notificationType.toNotificationType(),
        title = title,
        body = body,
        imageUrl = imageUrl,
        deeplink = deepLink,
        entityType = entityType,
        entityId = entityId,
        isRead = isRead,
        createdAt = createdAt ?: Instant.fromEpochMilliseconds(0)
    )
}

/**
 * Convert GraphQL NotificationType to domain NotificationType
 */
fun GraphQLNotificationType.toNotificationType(): NotificationType {
    return when (this) {
        GraphQLNotificationType.EVENT_RESCHEDULED -> NotificationType.EVENT_RESCHEDULED
        GraphQLNotificationType.EVENT_VENUE_CHANGED -> NotificationType.EVENT_VENUE_CHANGED
        GraphQLNotificationType.EVENT_CANCELLED -> NotificationType.EVENT_CANCELLED
        GraphQLNotificationType.VENUE_ADDRESS_CHANGED -> NotificationType.VENUE_ADDRESS_CHANGED
        GraphQLNotificationType.PERFORMER_ADDED_TO_EVENT -> NotificationType.PERFORMER_ADDED_TO_EVENT
        GraphQLNotificationType.PERFORMER_REMOVED_FROM_EVENT -> NotificationType.PERFORMER_REMOVED_FROM_EVENT
        GraphQLNotificationType.ORGANIZATION_UPDATED -> NotificationType.ORGANIZATION_UPDATED
        GraphQLNotificationType.AGENDA_ITEM_UPDATED -> NotificationType.AGENDA_ITEM_UPDATED
        GraphQLNotificationType.GENERAL -> NotificationType.GENERAL
        else -> NotificationType.UNKNOWN
    }
}

/**
 * Convert domain Platform to GraphQL Platform for mutations
 */
fun Platform.toGraphQLPlatform(): GraphQLPlatform {
    return when (this) {
        Platform.ANDROID -> GraphQLPlatform.ANDROID
        Platform.IOS -> GraphQLPlatform.IOS
    }
}
