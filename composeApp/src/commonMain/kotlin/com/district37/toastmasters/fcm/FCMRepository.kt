package com.district37.toastmasters.fcm

import com.wongislandd.nexus.networking.HttpMethod
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class UpsertPushTokenRequest(
    val token: String,
    val user_id: String,
    val device_info: String? = null,
    val platform: String = "android",
    val active: Boolean = true
)

@Serializable
data class UpsertPushTokenResponse(
    val token: String? = null,
    val user_id: String? = null
)

class FCMRepository(httpClient: HttpClient) : NetworkClient(httpClient) {
    suspend fun registerToken(userId: String, token: String, deviceInfo: String? = null): Resource<Unit> {
        return when (
            val result = makeRequest<List<UpsertPushTokenResponse>>(
                endpoint = "rest/v1/push_tokens",
                httpMethod = HttpMethod.POST
            ) {
                url {
                    parameters.append("on_conflict", "token")
                }
                contentType(ContentType.Application.Json)
                headers {
                    append("Prefer", "resolution=merge-duplicates,return=representation")
                }
                setBody(
                    listOf(
                        UpsertPushTokenRequest(
                            token = token,
                            user_id = userId,
                            device_info = deviceInfo
                        )
                    )
                )
            }
        ) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
            is Resource.NotLoading -> Resource.NotLoading
        }
    }
}
