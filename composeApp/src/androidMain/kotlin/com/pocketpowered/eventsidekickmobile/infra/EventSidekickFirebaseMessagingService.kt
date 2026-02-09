package com.district37.toastmasters.infra

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.district37.toastmasters.MainActivity
import com.district37.toastmasters.R
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.models.Platform
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import java.net.URL

/**
 * Firebase Messaging Service for handling push notifications.
 *
 * This service handles:
 * 1. Incoming push notifications when app is in background
 * 2. Token refresh events to re-register with the server
 */
open class EventSidekickFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationRepository: NotificationRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val calendarSyncManager: CalendarSyncManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Called when a new FCM token is generated.
     * This can happen on fresh install, app data cleared, or token rotation.
     * Only registers if user is authenticated - otherwise token will be registered after login.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        serviceScope.launch {
            try {
                // Only register if user is currently authenticated
                val authState = authRepository.authState.value
                if (authState is AuthState.Authenticated) {
                    notificationRepository.registerDeviceToken(
                        fcmToken = token,
                        platform = Platform.ANDROID,
                        deviceName = Build.MODEL
                    )
                }
                // If not authenticated, token will be registered after login via AuthViewModel
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    /**
     * Called when a message is received.
     * This is called when the app is in the foreground OR when the message
     * contains only data payload (not notification payload).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract notification data
        val data = remoteMessage.data
        val type = data["type"]

        // Handle calendar sync push notifications (silent - no UI)
        when (type) {
            // Agenda item sync notifications
            "CALENDAR_SYNC_DELETE" -> {
                handleCalendarSyncDelete(data)
                return
            }
            "CALENDAR_SYNC_UPDATE" -> {
                handleCalendarSyncUpdate(data)
                return
            }
            "CALENDAR_SYNC_BULK_DELETE" -> {
                handleCalendarSyncBulkDelete(data)
                return
            }
            // Event-level sync notifications
            "CALENDAR_SYNC_EVENT_DELETE" -> {
                handleEventCalendarDelete(data)
                return
            }
            "CALENDAR_SYNC_EVENT_UPDATE" -> {
                handleEventCalendarUpdate(data)
                return
            }
        }

        // Standard notification handling
        val notificationId = data["notification_id"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Event Sidekick"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val deeplink = data["deeplink"]
        val imageUrl = data["imageUrl"]

        // Show notification
        showNotification(notificationId, title, body, deeplink, imageUrl)
    }

    /**
     * Handle CALENDAR_SYNC_DELETE push notification.
     * Silently deletes a calendar event when an agenda item is deleted on server.
     */
    private fun handleCalendarSyncDelete(data: Map<String, String>) {
        serviceScope.launch {
            try {
                val payload = data["payload"] ?: return@launch
                val deletePayload = json.decodeFromString<CalendarSyncDeletePayload>(payload)

                calendarSyncManager.deleteByCalendarEventId(deletePayload.calendarEventId)

                // TODO: Acknowledge sync to server
                // val queueId = data["queueId"]?.toLongOrNull()
                // queueId?.let { notificationRepository.acknowledgeCalendarSync(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle CALENDAR_SYNC_UPDATE push notification.
     * Updates the calendar event immediately with data from the push payload.
     * Reconciliation on app open serves as a safety net if this update fails.
     */
    private fun handleCalendarSyncUpdate(data: Map<String, String>) {
        serviceScope.launch {
            try {
                val payload = data["payload"] ?: return@launch
                val updatePayload = json.decodeFromString<CalendarSyncUpdatePayload>(payload)

                android.util.Log.d("CalendarSync", "Updating calendar event for agenda item ${updatePayload.agendaItemId}")

                // Update the calendar event with the payload data
                calendarSyncManager.updateCalendarEventFromPayload(
                    calendarEventId = updatePayload.calendarEventId,
                    title = updatePayload.title,
                    startTime = updatePayload.startTime,
                    endTime = updatePayload.endTime,
                    timezone = updatePayload.timezone,
                    locationName = updatePayload.locationName
                )

                // TODO: Acknowledge sync to server
                // val queueId = data["queueId"]?.toLongOrNull()
                // queueId?.let { notificationRepository.acknowledgeCalendarSync(it) }
            } catch (e: Exception) {
                android.util.Log.e("CalendarSync", "Failed to update calendar event", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle CALENDAR_SYNC_BULK_DELETE push notification.
     * Silently deletes all calendar events for a deleted event.
     */
    private fun handleCalendarSyncBulkDelete(data: Map<String, String>) {
        serviceScope.launch {
            try {
                val payload = data["payload"] ?: return@launch
                val bulkDeletePayload = json.decodeFromString<CalendarSyncBulkDeletePayload>(payload)

                calendarSyncManager.deleteMultipleByCalendarEventIds(bulkDeletePayload.calendarEventIds)

                // TODO: Acknowledge sync to server
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================================================
    // Event-level sync notification handlers
    // ============================================================

    /**
     * Handle CALENDAR_SYNC_EVENT_DELETE push notification.
     * Silently deletes the calendar event for a deleted event.
     */
    private fun handleEventCalendarDelete(data: Map<String, String>) {
        serviceScope.launch {
            try {
                val payload = data["payload"] ?: return@launch
                val deletePayload = json.decodeFromString<EventCalendarSyncDeletePayload>(payload)

                android.util.Log.d("CalendarSync", "Deleting calendar event for event ${deletePayload.eventId}")

                calendarSyncManager.deleteByCalendarEventId(deletePayload.calendarEventId)

                // TODO: Acknowledge sync to server
            } catch (e: Exception) {
                android.util.Log.e("CalendarSync", "Failed to delete event calendar entry", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle CALENDAR_SYNC_EVENT_UPDATE push notification.
     * Updates the calendar event with data from the push payload.
     */
    private fun handleEventCalendarUpdate(data: Map<String, String>) {
        serviceScope.launch {
            try {
                val payload = data["payload"] ?: return@launch
                val updatePayload = json.decodeFromString<EventCalendarSyncUpdatePayload>(payload)

                android.util.Log.d("CalendarSync", "Updating calendar event for event ${updatePayload.eventId}")

                // Update the calendar event with the payload data
                calendarSyncManager.updateCalendarEventFromPayload(
                    calendarEventId = updatePayload.calendarEventId,
                    title = updatePayload.title,
                    startTime = updatePayload.startTime,
                    endTime = updatePayload.endTime,
                    timezone = updatePayload.timezone,
                    locationName = updatePayload.venueName
                )

                // TODO: Acknowledge sync to server
            } catch (e: Exception) {
                android.util.Log.e("CalendarSync", "Failed to update event calendar entry", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Display a local notification with optional large icon from image URL
     */
    private fun showNotification(
        notificationId: Int,
        title: String,
        body: String,
        deeplink: String?,
        imageUrl: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deeplink?.let { putExtra("deeplink", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show notification immediately without image
        val builder = NotificationCompat.Builder(this, PushNotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())

        // If there's an image URL, download it async and update the notification
        if (!imageUrl.isNullOrBlank()) {
            serviceScope.launch {
                val largeIcon = downloadAndProcessImage(imageUrl)
                if (largeIcon != null) {
                    val updatedNotification = NotificationCompat.Builder(
                        this@EventSidekickFirebaseMessagingService,
                        PushNotificationService.CHANNEL_ID
                    )
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setLargeIcon(largeIcon)
                        .build()
                    notificationManager.notify(notificationId, updatedNotification)
                }
            }
        }
    }

    /**
     * Download image from URL and convert to circular bitmap for notification large icon.
     * Uses a coroutine with timeout to avoid blocking.
     */
    private suspend fun downloadAndProcessImage(imageUrl: String): Bitmap? {
        return withTimeoutOrNull(5000L) { // 5 second timeout
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.getInputStream().use { stream ->
                    val originalBitmap = BitmapFactory.decodeStream(stream)
                    originalBitmap?.let { getCircularBitmap(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Convert a bitmap to a circular bitmap for notification large icon.
     */
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        // Draw circular clip
        canvas.drawOval(rectF, paint)

        // Draw bitmap with circular mask
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Center the bitmap if it's not square
        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2
        val srcRect = Rect(left, top, left + size, top + size)

        canvas.drawBitmap(bitmap, srcRect, rect, paint)

        return output
    }
}

/**
 * Payload for CALENDAR_SYNC_DELETE push notification.
 * Sent when a single agenda item is deleted on the server.
 */
@Serializable
private data class CalendarSyncDeletePayload(
    val agendaItemId: Int,
    val calendarEventId: String,
    val title: String
)

/**
 * Payload for CALENDAR_SYNC_UPDATE push notification.
 * Sent when an agenda item's calendar-relevant fields are updated.
 */
@Serializable
private data class CalendarSyncUpdatePayload(
    val agendaItemId: Int,
    val calendarEventId: String,
    val title: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val timezone: String? = null,
    val locationId: Int? = null,
    val locationName: String? = null
)

/**
 * Payload for CALENDAR_SYNC_BULK_DELETE push notification.
 * Sent when an entire event is deleted, containing all calendar event IDs to remove.
 */
@Serializable
private data class CalendarSyncBulkDeletePayload(
    val eventId: Int,
    val eventTitle: String,
    val agendaItemIds: List<Int>,
    val calendarEventIds: List<String>
)

// ============================================================
// Event-level sync payload classes
// ============================================================

/**
 * Payload for CALENDAR_SYNC_EVENT_DELETE push notification.
 * Sent when an event is deleted and the user had the event synced to their calendar.
 */
@Serializable
private data class EventCalendarSyncDeletePayload(
    val eventId: Int,
    val calendarEventId: String,
    val title: String
)

/**
 * Payload for CALENDAR_SYNC_EVENT_UPDATE push notification.
 * Sent when an event's calendar-relevant fields (time, venue) are updated.
 */
@Serializable
private data class EventCalendarSyncUpdatePayload(
    val eventId: Int,
    val calendarEventId: String,
    val title: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val timezone: String? = null,
    val venueId: Int? = null,
    val venueName: String? = null
)
