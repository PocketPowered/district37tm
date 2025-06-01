package com.district37.toastmasters

import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class FirebaseNotificationService : KoinComponent {
    private val gson = Gson()
    private val httpClient = HttpClient.newBuilder().build()
    private val projectId = "district37-toastmasters"
    private val fcmEndpoint = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
    private val credentials: GoogleCredentials by inject()

    /**
     * Sends a notification along to everyone
     */
    suspend fun sendNotification(
        title: String,
        body: String
    ): String = withContext(Dispatchers.IO) {
        val message = FcmMessage(
            message = MessageData(
                topic = NotifcationTopics.GENERAL.name,
                data = mapOf(
                    "title" to title,
                    "body" to body
                )
            )
        )

        sendFcmMessage(message)
    }

    private suspend fun sendFcmMessage(message: FcmMessage): String {
        credentials.refreshIfExpired()
        val token = credentials.accessToken.tokenValue

        val request = HttpRequest.newBuilder()
            .uri(URI(fcmEndpoint))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(message), StandardCharsets.UTF_8))
            .build()

        val response = withContext(Dispatchers.IO) {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        if (response.statusCode() !in 200..299) {
            throw Exception("Failed to send FCM message: ${response.body()}")
        }

        return response.body()
    }

    private data class FcmMessage(
        val message: MessageData
    )

    private data class MessageData(
        val topic: String,
        val data: Map<String, String>
    )
} 