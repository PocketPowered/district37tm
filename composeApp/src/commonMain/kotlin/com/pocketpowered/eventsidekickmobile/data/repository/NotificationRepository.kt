package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.*
import com.district37.toastmasters.data.transformers.toNotification
import com.district37.toastmasters.data.transformers.toGraphQLPlatform
import com.district37.toastmasters.models.DeviceToken
import com.district37.toastmasters.models.Notification
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.graphql.type.Platform as GraphQLPlatform

/**
 * Repository for notification data operations
 */
class NotificationRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "NotificationRepository"

    /**
     * Get paginated notifications for the current user
     */
    suspend fun getNotifications(
        cursor: String? = null,
        first: Int = 20,
        unreadOnly: Boolean? = null
    ): Resource<PagedConnection<Notification>> {
        return executeQuery(
            queryName = "getNotifications(cursor=$cursor, unreadOnly=$unreadOnly)",
            query = {
                apolloClient.query(
                    GetNotificationsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor),
                        unreadOnly = Optional.presentIfNotNull(unreadOnly)
                    )
                ).execute()
            },
            transform = { data ->
                data.notificationsConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.mapNotNull { edge ->
                            edge.node.notificationPreview.toNotification()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Get count of unread notifications
     */
    suspend fun getUnreadCount(): Resource<Int> {
        return executeQuery(
            queryName = "getUnreadNotificationCount",
            query = {
                apolloClient.query(GetUnreadNotificationCountQuery()).execute()
            },
            transform = { data ->
                data.unreadNotificationCount
            }
        )
    }

    /**
     * Mark a single notification as read
     */
    suspend fun markAsRead(notificationId: Int): Resource<Notification> {
        return executeMutation(
            mutationName = "markNotificationRead(id=$notificationId)",
            mutation = {
                apolloClient.mutation(
                    MarkNotificationReadMutation(notificationId = notificationId)
                ).execute()
            },
            transform = { data ->
                data.markNotificationRead.notificationPreview.toNotification()
            }
        )
    }

    /**
     * Mark all notifications as read
     * @return Number of notifications marked as read
     */
    suspend fun markAllAsRead(): Resource<Int> {
        return executeMutation(
            mutationName = "markAllNotificationsRead",
            mutation = {
                apolloClient.mutation(MarkAllNotificationsReadMutation()).execute()
            },
            transform = { data ->
                data.markAllNotificationsRead
            }
        )
    }

    /**
     * Register a device token for push notifications.
     * Uses retry with exponential backoff since this is a critical operation
     * that should succeed even with temporary network issues.
     */
    suspend fun registerDeviceToken(
        fcmToken: String,
        platform: Platform,
        deviceName: String? = null
    ): Resource<DeviceToken> {
        return executeMutationWithRetry(
            mutationName = "registerDeviceToken",
            mutation = {
                apolloClient.mutation(
                    RegisterDeviceTokenMutation(
                        fcmToken = fcmToken,
                        platform = platform.toGraphQLPlatform(),
                        deviceName = Optional.presentIfNotNull(deviceName)
                    )
                ).execute()
            },
            transform = { data ->
                data.registerDeviceToken.let { token ->
                    DeviceToken(
                        id = token.id,
                        fcmToken = token.fcmToken,
                        platform = if (token.platform == GraphQLPlatform.ANDROID) Platform.ANDROID else Platform.IOS,
                        deviceName = token.deviceName,
                        isActive = token.isActive
                    )
                }
            }
        )
    }

    /**
     * Unregister a device token (call on logout)
     */
    suspend fun unregisterDeviceToken(fcmToken: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "unregisterDeviceToken",
            mutation = {
                apolloClient.mutation(
                    UnregisterDeviceTokenMutation(fcmToken = fcmToken)
                ).execute()
            },
            transform = { data ->
                data.unregisterDeviceToken
            }
        )
    }

    /**
     * Send a test notification to the current user.
     * Used for debugging the push notification flow from Developer Options.
     */
    suspend fun sendTestNotification(): Resource<Boolean> {
        return executeMutation(
            mutationName = "sendTestNotification",
            mutation = {
                apolloClient.mutation(SendTestNotificationMutation()).execute()
            },
            transform = { data ->
                data.sendTestNotification
            }
        )
    }
}
