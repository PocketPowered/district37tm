package com.district37.toastmasters

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class RegisterTokenRequest(
    val userId: String,
    val token: String,
    val deviceInfo: String? = null
)

fun Application.userFCMController() {
    val fcmService: FirebaseUserFCMService by inject()

    routing {
        post("/fcm/register") {
            try {
                val request = call.receive<RegisterTokenRequest>()
                val token = fcmService.registerToken(
                    userId = request.userId,
                    token = request.token,
                    deviceInfo = request.deviceInfo
                )
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
} 