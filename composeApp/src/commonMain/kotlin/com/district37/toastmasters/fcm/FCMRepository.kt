package com.district37.toastmasters.fcm

import com.wongislandd.nexus.networking.HttpMethod
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class RegisterTokenRequest(
    val userId: String,
    val token: String,
    val deviceInfo: String? = null
)

class FCMRepository(httpClient: HttpClient) : NetworkClient(httpClient) {
    suspend fun registerToken(userId: String, token: String, deviceInfo: String? = null): Resource<Unit> {
        return makeRequest(
            "fcm/register",
            HttpMethod.POST
        ) {
            contentType(ContentType.Application.Json)
            setBody(RegisterTokenRequest(userId, token, deviceInfo))
        }
    }
} 