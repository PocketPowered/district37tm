package com.district37.toastmasters.infra

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android implementation of PushNotificationService using Firebase Cloud Messaging
 */
actual class PushNotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "event_updates"
        const val CHANNEL_NAME = "Event Updates"
        const val CHANNEL_DESCRIPTION = "Notifications about events you follow"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Get the current FCM token
     */
    actual suspend fun getFcmToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    continuation.resume(token)
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null)
                }
        }
    }

    /**
     * Delete the current FCM token
     */
    actual suspend fun deleteFcmToken() {
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().deleteToken()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    /**
     * Check if push notifications are enabled
     */
    actual suspend fun areNotificationsEnabled(): Boolean {
        // For Android 13+, check POST_NOTIFICATIONS permission
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, notifications are enabled by default
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        }
    }

    /**
     * Request notification permission
     * Note: For Android 13+, this needs to be called from an Activity.
     * This implementation returns the current state - actual permission request
     * needs to be done via Activity.
     */
    actual suspend fun requestNotificationPermission(): Boolean {
        // Return current permission state
        // The actual permission request needs to be triggered from the Activity
        return areNotificationsEnabled()
    }
}
