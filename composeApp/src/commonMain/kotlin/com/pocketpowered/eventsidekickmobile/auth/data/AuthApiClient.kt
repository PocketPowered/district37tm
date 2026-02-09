package com.district37.toastmasters.auth.data

import com.district37.toastmasters.auth.models.AuthErrorResponse
import com.district37.toastmasters.auth.models.AuthenticationException
import com.district37.toastmasters.auth.models.OAuthUrlRequest
import com.district37.toastmasters.auth.models.OAuthUrlResponse
import com.district37.toastmasters.auth.models.RefreshTokenRequest
import com.district37.toastmasters.auth.models.RefreshTokenResponse
import com.district37.toastmasters.auth.models.TokenExpiredException
import com.district37.toastmasters.infra.EffectiveServerUrlProvider
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AuthApiClient(
    private val serverUrlProvider: EffectiveServerUrlProvider
) {

    private val TAG = "AuthApiClient"
    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl: String
        get() = serverUrlProvider.baseUrl

    suspend fun getOAuthUrl(redirectUrl: String): Result<String> {
        return try {
            val response = httpClient.post("$baseUrl/auth/oauth-url") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(OAuthUrlRequest.serializer(), OAuthUrlRequest(redirectUrl)))
            }
            val bodyText = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val oauthResponse = json.decodeFromString(OAuthUrlResponse.serializer(), bodyText)
                Result.success(oauthResponse.url)
            } else {
                val errorResponse = try {
                    json.decodeFromString(AuthErrorResponse.serializer(), bodyText)
                } catch (e: Exception) {
                    AuthErrorResponse("unknown_error", "Failed to get OAuth URL")
                }
                Result.failure(AuthenticationException(errorResponse.message))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get OAuth URL: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(accessToken: String): Result<User> {
        return try {
            val response = httpClient.get("$baseUrl/auth/me") {
                header("Authorization", "Bearer $accessToken")
            }
            val bodyText = response.bodyAsText()
            when (response.status) {
                HttpStatusCode.OK -> {
                    val user = json.decodeFromString(User.serializer(), bodyText)
                    Result.success(user)
                }
                HttpStatusCode.Unauthorized -> {
                    val errorResponse = try {
                        json.decodeFromString(AuthErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        AuthErrorResponse("auth_error", "Unauthorized")
                    }
                    if (errorResponse.error == "token_expired") {
                        Result.failure(TokenExpiredException(errorResponse.message))
                    } else {
                        Result.failure(AuthenticationException(errorResponse.message))
                    }
                }
                else -> {
                    val errorResponse = try {
                        json.decodeFromString(AuthErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        AuthErrorResponse("unknown_error", "Failed to validate token")
                    }
                    Result.failure(AuthenticationException(errorResponse.message))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get current user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            val response = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(RefreshTokenRequest.serializer(), RefreshTokenRequest(refreshToken)))
            }
            val bodyText = response.bodyAsText()
            when (response.status) {
                HttpStatusCode.OK -> {
                    val refreshResponse = json.decodeFromString(RefreshTokenResponse.serializer(), bodyText)
                    Result.success(refreshResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    val errorResponse = try {
                        json.decodeFromString(AuthErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        AuthErrorResponse("refresh_failed", "Failed to refresh token")
                    }
                    Result.failure(AuthenticationException(errorResponse.message))
                }
                else -> {
                    val errorResponse = try {
                        json.decodeFromString(AuthErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        AuthErrorResponse("unknown_error", "Failed to refresh token")
                    }
                    Result.failure(AuthenticationException(errorResponse.message))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to refresh token: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Logout and invalidate the session on the server.
     * POST /auth/logout
     */
    suspend fun logout(accessToken: String): Result<Unit> {
        return try {
            val response = httpClient.post("$baseUrl/auth/logout") {
                header("Authorization", "Bearer $accessToken")
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                // Even if server logout fails, we still want to clear local tokens
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to logout on server: ${e.message}")
            // Don't fail the logout flow if server is unreachable
            Result.success(Unit)
        }
    }

    fun close() {
        httpClient.close()
    }
}
