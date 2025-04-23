package com.district37.toastmasters

interface NotificationService {
    suspend fun sendNotification(
        title: String,
        body: String,
        topic: String
    ): String
} 