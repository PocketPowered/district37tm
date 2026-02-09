package com.district37.toastmasters.infra

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.district37.toastmasters.infra.adapters.InstantAdapter
import com.district37.toastmasters.infra.adapters.LocalDateAdapter
import com.district37.toastmasters.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Configuration for Apollo GraphQL client
 * Creates and configures the Apollo client for communication with the EventSidekick server
 */
object ApolloClientConfig {

    private const val TAG = "ApolloClient"
    private const val CONNECTION_TEST_TIMEOUT_MS = 5000L

    /**
     * Creates and configures an Apollo client with automatic fallback
     * @param primaryUrl The primary server URL to try first
     * @param fallbackUrl Optional fallback URL if primary fails (typically production)
     * @param enableFallback Whether to enable automatic fallback on connection failure
     * @param authInterceptor Optional HTTP interceptor for authentication
     * @param tokenProvider Function that returns the current auth token (called for each WebSocket connection)
     * @return Configured ApolloClient instance
     */
    suspend fun createApolloClient(
        primaryUrl: String,
        fallbackUrl: String? = null,
        enableFallback: Boolean = true,
        authInterceptor: HttpInterceptor? = null,
        tokenProvider: (suspend () -> String?)? = null
    ): ApolloClient {
        // If fallback is enabled and a fallback URL is provided, test the connection
        if (enableFallback && fallbackUrl != null && primaryUrl != fallbackUrl) {
            val isReachable = testConnection(primaryUrl)

            if (!isReachable) {
                Logger.e(TAG, "Failed to connect to $primaryUrl, falling back to $fallbackUrl")
                return buildClient(fallbackUrl, authInterceptor, tokenProvider)
            }
        }

        return buildClient(primaryUrl, authInterceptor, tokenProvider)
    }

    /**
     * Converts an HTTP URL to a WebSocket URL
     */
    private fun httpToWebSocketUrl(httpUrl: String): String {
        return httpUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")
    }

    /**
     * Builds an Apollo client with the given server URL
     */
    private fun buildClient(
        serverUrl: String,
        authInterceptor: HttpInterceptor? = null,
        tokenProvider: (suspend () -> String?)? = null
    ): ApolloClient {
        val customScalarAdapters = CustomScalarAdapters.Builder()
            .add(com.apollographql.apollo.api.CustomScalarType("Instant", "kotlin.String"), InstantAdapter)
            .add(com.apollographql.apollo.api.CustomScalarType("LocalDate", "kotlin.String"), LocalDateAdapter)
            .build()

        // Convert HTTP URL to WebSocket URL for subscriptions
        val wsUrl = httpToWebSocketUrl(serverUrl)
        Logger.d(TAG, "WebSocket URL for subscriptions: $wsUrl")

        val builder = ApolloClient.Builder()
            .serverUrl(serverUrl)
            .customScalarAdapters(customScalarAdapters)
            // Add WebSocket transport for subscriptions with dynamic auth token
            .subscriptionNetworkTransport(
                WebSocketNetworkTransport.Builder()
                    .serverUrl(wsUrl)
                    .protocol(GraphQLWsProtocol.Factory(
                        connectionPayload = {
                            // Get fresh auth token for each connection
                            val token = tokenProvider?.invoke()
                            if (!token.isNullOrBlank()) {
                                mapOf("Authorization" to "Bearer $token")
                            } else {
                                emptyMap()
                            }
                        }
                    ))
                    .build()
            )

        // Add auth interceptor for HTTP requests if provided
        authInterceptor?.let { builder.addHttpInterceptor(it) }

        return builder.build()
    }

    /**
     * Tests if the server is reachable by making a simple introspection query
     * Uses HttpClient to test the GraphQL endpoint
     */
    private suspend fun testConnection(serverUrl: String): Boolean {
        val httpClient = HttpClient()
        return try {
            withTimeoutOrNull(CONNECTION_TEST_TIMEOUT_MS) {
                try {
                    // Simple introspection query - just checks if server responds
                    val response = httpClient.post(serverUrl) {
                        contentType(ContentType.Application.Json)
                        setBody("""{"query":"{__typename}"}""")
                    }
                    val body = response.bodyAsText()
                    // If we get a response with data, server is reachable
                    body.contains("data") || body.contains("__typename")
                } catch (e: Exception) {
                    Logger.e(TAG, "HTTP test failed: ${e.message.orEmpty()}")
                    false
                } finally {
                    httpClient.close()
                }
            } ?: false
        } catch (e: Exception) {
            Logger.e(TAG, "Connection test failed: ${e.message.orEmpty()}")
            false
        }
    }
}