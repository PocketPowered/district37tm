package com.district37.toastmasters

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseNotificationService : NotificationService {
    init {
        val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
            ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
        val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

        if (FirebaseApp.getApps().isEmpty()) {
            val options = FirebaseOptions.builder()
                .setCredentials(serviceAccount)
                .setProjectId("district37-toastmasters")
                .build()

            FirebaseApp.initializeApp(options)
        }
    }

    override suspend fun sendNotification(
        title: String,
        body: String,
        topic: String
    ): String = withContext(Dispatchers.IO) {
        val messageBuilder = Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )

        val message = messageBuilder.setTopic(topic).build()
        FirebaseMessaging.getInstance().send(message)
    }
} 