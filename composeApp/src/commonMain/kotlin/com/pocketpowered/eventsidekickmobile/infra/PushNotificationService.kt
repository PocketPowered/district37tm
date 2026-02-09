package com.district37.toastmasters.infra

/**
 * Platform-specific push notification service
 * - Android: Uses Firebase Cloud Messaging
 * - iOS: Uses Firebase Cloud Messaging via Firebase iOS SDK
 */
expect class PushNotificationService {
    /**
     * Get the current FCM token
     * @return The FCM token or null if not available
     */
    suspend fun getFcmToken(): String?

    /**
     * Delete the current FCM token (call on logout)
     */
    suspend fun deleteFcmToken()

    /**
     * Check if push notifications are enabled/permitted
     */
    suspend fun areNotificationsEnabled(): Boolean

    /**
     * Request notification permissions from the user
     * @return true if permissions were granted
     */
    suspend fun requestNotificationPermission(): Boolean
}
