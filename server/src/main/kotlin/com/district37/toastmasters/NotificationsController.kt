package com.district37.toastmasters

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class NotificationRequest(
    val title: String,
    val body: String,
    val topic: String
)

fun Application.notificationsController() {
    val notificationService: NotificationService by inject()

    routing {
        post("/notifications") {
            try {
                val request = NotificationRequest("test", "test", "test2")
                val messageId = notificationService.sendNotification(
                    title = request.title,
                    body = request.body,
                    topic = request.topic
                )
                call.respond(mapOf("messageId" to messageId))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
} 