package com.district37.toastmasters.data.repository

import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.infra.EffectiveServerUrlProvider
import com.district37.toastmasters.util.AppConstants
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.ErrorType
import com.district37.toastmasters.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
/**
 * Response from the image upload endpoint
 */
@Serializable
data class ImageUploadResponse(
    val url: String,
    val path: String
)

/**
 * Error response from the upload endpoint
 */
@Serializable
data class UploadErrorResponse(
    val error: String,
    val message: String
)

/**
 * Repository for uploading images to the server.
 * Uses multipart form data to upload images via the /storage/upload-image endpoint.
 *
 * This repository is managed as a singleton by the DI container and lives for the
 * application lifetime. The HttpClient is cleaned up when the application terminates.
 */
class ImageUploadRepository(
    private val authRepository: AuthRepository,
    private val serverUrlProvider: EffectiveServerUrlProvider
) {
    private val TAG = "ImageUploadRepository"
    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl: String
        get() = serverUrlProvider.baseUrl

    /**
     * Upload an image to the server.
     *
     * @param imageBytes The raw image bytes to upload
     * @param entityType The type of entity (event, venue, performer)
     * @param entityId Optional entity ID (can be empty for new entities)
     * @param filename Original filename (used for extension detection)
     * @param contentType The MIME type of the image
     * @return Resource containing the uploaded image URL on success
     */
    suspend fun uploadImage(
        imageBytes: ByteArray,
        entityType: String,
        entityId: String? = null,
        filename: String = "image.jpg",
        contentType: String = "image/jpeg"
    ): Resource<String> {
        return try {
            // Get access token
            val accessToken = authRepository.getValidAccessToken()
            if (accessToken == null) {
                Logger.e(TAG, "No valid access token available")
                return Resource.Error(ErrorType.CLIENT_ERROR, "Authentication required to upload images")
            }

            Logger.d(TAG, "Uploading image: entityType=$entityType, entityId=$entityId, filename=$filename, size=${imageBytes.size}")

            val response = httpClient.post("$baseUrl/storage/upload-image") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            })
                            append("entityType", entityType)
                            if (!entityId.isNullOrBlank()) {
                                append("entityId", entityId)
                            }
                        }
                    )
                )
            }

            val bodyText = response.bodyAsText()
            Logger.d(TAG, "Upload response status: ${response.status}, body: $bodyText")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val uploadResponse = json.decodeFromString(ImageUploadResponse.serializer(), bodyText)
                    Logger.d(TAG, "Upload successful: ${uploadResponse.url}")
                    Resource.Success(uploadResponse.url)
                }
                HttpStatusCode.Unauthorized -> {
                    Logger.e(TAG, "Upload failed: Unauthorized")
                    Resource.Error(ErrorType.CLIENT_ERROR, "Authentication required to upload images")
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = try {
                        json.decodeFromString(UploadErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        UploadErrorResponse("validation_error", "Invalid upload request")
                    }
                    Logger.e(TAG, "Upload failed: ${errorResponse.message}")
                    Resource.Error(ErrorType.CLIENT_ERROR, errorResponse.message)
                }
                else -> {
                    val errorResponse = try {
                        json.decodeFromString(UploadErrorResponse.serializer(), bodyText)
                    } catch (e: Exception) {
                        UploadErrorResponse("upload_error", "Failed to upload image")
                    }
                    Logger.e(TAG, "Upload failed: ${errorResponse.message}")
                    Resource.Error(ErrorType.SERVER_ERROR, errorResponse.message)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Upload exception: ${e.message}")
            Resource.Error(ErrorType.NETWORK_ERROR, e.message ?: "Failed to upload image")
        }
    }

    /**
     * Infer MIME type from filename extension
     */
    fun inferContentType(filename: String): String {
        return when {
            filename.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
            filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            filename.endsWith(".png", ignoreCase = true) -> "image/png"
            filename.endsWith(".gif", ignoreCase = true) -> "image/gif"
            filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "image/jpeg"
        }
    }

    /**
     * Upload an image with automatic retry on network errors.
     * Uses exponential backoff with configurable retry parameters.
     */
    suspend fun uploadImageWithRetry(
        imageBytes: ByteArray,
        entityType: String,
        entityId: String? = null,
        filename: String = "image.jpg",
        contentType: String = "image/jpeg",
        maxAttempts: Int = AppConstants.Retry.MAX_ATTEMPTS,
        initialDelayMs: Long = AppConstants.Retry.INITIAL_DELAY_MS,
        maxDelayMs: Long = AppConstants.Retry.MAX_DELAY_MS
    ): Resource<String> {
        var currentDelay = initialDelayMs
        repeat(maxAttempts - 1) { attempt ->
            when (val result = uploadImage(imageBytes, entityType, entityId, filename, contentType)) {
                is Resource.Success -> return result
                is Resource.Error -> {
                    if (result.errorType == ErrorType.NETWORK_ERROR) {
                        Logger.d(TAG, "Upload retry attempt ${attempt + 1} after network error. Waiting ${currentDelay}ms...")
                        delay(currentDelay)
                        currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                    } else {
                        // Don't retry non-network errors
                        return result
                    }
                }
                else -> return result
            }
        }
        // Final attempt
        return uploadImage(imageBytes, entityType, entityId, filename, contentType)
    }
}
