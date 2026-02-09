package com.district37.toastmasters.auth

import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.util.Logger

/**
 * Apollo HTTP interceptor that adds authentication headers to GraphQL requests.
 * Handles automatic token refresh on 401 responses.
 */
class AuthInterceptor(
    private val authRepository: AuthRepository
) : HttpInterceptor {

    private val TAG = "AuthInterceptor"

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        // Get valid access token (may trigger proactive refresh if needed)
        val accessToken = authRepository.getValidAccessToken()

        // Add Authorization header if token available
        val authenticatedRequest = if (accessToken != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Logger.e(TAG, "No access token available, sending unauthenticated request")
            request
        }

        // Execute the request
        val response = chain.proceed(authenticatedRequest)

        // Handle 401 responses by trying to refresh and retry
        if (response.statusCode == 401 && accessToken != null) {
            val newTokens = authRepository.refreshTokensIfNeeded()
            if (newTokens != null) {

                // Retry with new token
                val retryRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()

                return chain.proceed(retryRequest)
            } else {
                Logger.e(TAG, "Token refresh failed, returning original 401 response")
            }
        }

        return response
    }
}
