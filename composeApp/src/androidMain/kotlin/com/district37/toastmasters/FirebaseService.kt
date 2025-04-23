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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class FirebaseService : FirebaseMessagingService() {
    private val notificationRepository: NotificationRepository by inject(NotificationRepository::class.java)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d("[FCM] New token: $token")
        // Optionally send token to your server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val relatedEventId = remoteMessage.data["relatedEventId"]

        Logger.d("[FCM] Received notification - Title: $title, Body: $body, EventId: $relatedEventId")

        if (title != null && body != null) {
            try {
                notificationRepository.insertNotification(title, body)
                Logger.d("[FCM] Successfully inserted notification into database")
            } catch (e: Exception) {
                Logger.e("[FCM] Failed to insert notification: ${e.message}")
            }
        }

        showNotification(title ?: "Notification", body ?: "")
    }

    private fun showNotification(title: String, body: String) {
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