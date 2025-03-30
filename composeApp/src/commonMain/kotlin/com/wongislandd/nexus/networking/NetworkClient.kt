package com.wongislandd.nexus.networking


import co.touchlab.kermit.Logger
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

enum class HttpMethod {
    GET, POST, PUT, DELETE
}

abstract class NetworkClient(val httpClient: HttpClient) {

    suspend inline fun <reified T> makeRequest(
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.GET,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): Resource<T> {
        return makeNetworkRequest(endpoint, httpMethod, builder)
    }

    suspend inline fun <reified T> makeNetworkRequest(
        endpoint: String,
        httpMethod: HttpMethod,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): Resource<T> {
        try {
            val response = when (httpMethod) {
                HttpMethod.GET -> httpClient.get {
                    url(endpoint)
                    builder()
                }

                HttpMethod.POST -> httpClient.post {
                    url(endpoint)
                    builder()
                }
                else -> throw IllegalArgumentException("Unsupported HTTP method")
            }
            val newValue = when (response.status.value) {
                in 200..299 -> {
                    // This is not making use of ktor client, find a way. It seemed faster.
                    val data: T = response.body()
                    Resource.Success(data)
                }

                400 -> Resource.Error(ErrorType.BAD_REQUEST)
                401 -> Resource.Error(ErrorType.UNAUTHORIZED)
                403 -> Resource.Error(ErrorType.FORBIDDEN)
                404 -> Resource.Error(ErrorType.NOT_FOUND)
                408 -> Resource.Error(ErrorType.REQUEST_TIMEOUT)
                429 -> Resource.Error(ErrorType.RATE_LIMITED)
                in 500..599 -> Resource.Error(ErrorType.INTERNAL_SERVER_ERROR)
                else -> Resource.Error(ErrorType.NETWORK_ERROR)
            }
            return newValue
        } catch (e: UnresolvedAddressException) {
            return Resource.Error(ErrorType.NO_INTERNET)
        } catch (e: SerializationException) {
            return Resource.Error(ErrorType.SERIALIZATION)
        } catch (e: SocketTimeoutException) {
            return Resource.Error(ErrorType.SOCKET_TIMEOUT)
        } catch (e: Exception) {
            Logger.e(tag = "Network Error", null) {
                e.toString()
            }
            return Resource.Error(ErrorType.UNKNOWN, e)
        }
    }
}