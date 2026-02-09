package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Types of notifications that can be received
 */
enum class NotificationType {
    EVENT_RESCHEDULED,
    EVENT_VENUE_CHANGED,
    EVENT_CANCELLED,
    VENUE_ADDRESS_CHANGED,
    PERFORMER_ADDED_TO_EVENT,
    PERFORMER_REMOVED_FROM_EVENT,
    ORGANIZATION_UPDATED,
    AGENDA_ITEM_UPDATED,
    GENERAL,
    UNKNOWN
}

/**
 * Domain model for a notification
 */
data class Notification(
    val id: Int,
    val notificationType: NotificationType,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val deeplink: String?,
    val entityType: String?,
    val entityId: Int?,
    val isRead: Boolean,
    val createdAt: Instant
)

/**
 * Platform enum for device token registration
 */
enum class Platform {
    ANDROID,
    IOS
}

/**
 * Domain model for a registered device token
 */
data class DeviceToken(
    val id: Int,
    val fcmToken: String,
    val platform: Platform,
    val deviceName: String?,
    val isActive: Boolean
)
