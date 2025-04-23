package com.district37.toastmasters

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
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

@Serializable
data class UpdateTokenRequest(
    val userId: String,
    val oldToken: String,
    val newToken: String,
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

        post("/fcm/update") {
            try {
                val request = call.receive<UpdateTokenRequest>()
                val token = fcmService.updateToken(
                    userId = request.userId,
                    oldToken = request.oldToken,
                    newToken = request.newToken,
                    deviceInfo = request.deviceInfo
                )
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        get("/fcm/tokens/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("userId is required")
                val tokens = fcmService.getTokensForUser(userId)
                call.respond(tokens)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        delete("/fcm/token/{token}") {
            try {
                val token = call.parameters["token"] ?: throw IllegalArgumentException("token is required")
                val success = fcmService.deleteToken(token)
                call.respond(mapOf("success" to success))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        delete("/fcm/user/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("userId is required")
                val success = fcmService.deleteTokensForUser(userId)
                call.respond(mapOf("success" to success))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
} 