package com.district37.toastmasters

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import co.touchlab.kermit.Logger
import com.district37.toastmasters.database.NotificationRepository
import com.district37.toastmasters.fcm.FCMRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class FirebaseService : FirebaseMessagingService() {
    private val notificationRepository: NotificationRepository by inject(NotificationRepository::class.java)
    // Since we can just subscribe to topics, do we need to track their FCM token?
    private val fcmRepository: FCMRepository by inject(FCMRepository::class.java)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d("[FCM] New token: $token")
        // Subscribe to topics
        subscribeToTopics()
    }

    private fun subscribeToTopics() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance()
                    .subscribeToTopic(NotifcationTopics.GENERAL.name)
                Logger.d("[FCM] Successfully subscribed to topics")
            } catch (e: Exception) {
                Logger.e("[FCM] Failed to subscribe to topics: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Logger.d("[FCM] Received message - Data: ${remoteMessage.data}")

        // Handle data-only messages
        val data = remoteMessage.data
        val title = data["title"] ?: "Notification"
        val body = data["body"] ?: ""
        val type = data["type"] ?: "notification"
        val relatedEventId = data["relatedEventId"]

        Logger.d("[FCM] Processed data - Title: $title, Body: $body, Type: $type, EventId: $relatedEventId")

        try {
            notificationRepository.insertNotification(title, body)
            Logger.d("[FCM] Successfully inserted notification into database")
        } catch (e: Exception) {
            Logger.e("[FCM] Failed to insert notification: ${e.message}")
        }

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        Logger.d("[FCM] Showing notification - Title: $title, Body: $body")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}