package com.district37.toastmasters.util

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.graphql.type.CreateImageInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.Image

/**
 * Utility class for handling image upload and record creation in wizard flows
 *
 * Simplifies the common pattern of:
 * 1. Uploading a device image
 * 2. Creating an image record linked to an entity
 *
 * This is stateless - designed to be injected and used within wizard submission logic
 *
 * Usage:
 * ```kotlin
 * class CreateEventWizardViewModel(
 *     ...,
 *     private val wizardImageHandler: WizardImageHandler
 * ) {
 *     suspend fun submit() {
 *         // Create entity first
 *         val result = eventRepository.createEvent(input)
 *
 *         if (result is Resource.Success) {
 *             // Handle image
 *             wizardImageHandler.handleImageUploadAndRecord(
 *                 imageBytes = _selectedImageBytes.value,
 *                 focusRegion = _imageFocusRegion.value,
 *                 entityId = result.data.id,
 *                 entityType = EntityType.EVENT,
 *                 entityTypeString = "event",
 *                 altText = _eventName.value
 *             )
 *         }
 *     }
 * }
 * ```
 */
class WizardImageHandler(
    private val imageUploadRepository: ImageUploadRepository,
    private val imageRepository: ImageRepository
) {
    /**
     * Result of image handling operation
     */
    data class ImageHandlingResult(
        val imageUrl: String?,
        val error: String?
    )

    /**
     * Handle image upload: upload device image only
     *
     * @param imageBytes Optional device image bytes
     * @param focusRegion Optional focus region for cropping
     * @param entityId ID of the entity to associate with
     * @param entityTypeString Entity type string for upload path ("event", "venue", etc.)
     * @param tag Logging tag
     * @return ImageHandlingResult containing final URL or error
     */
    suspend fun handleImageUpload(
        imageBytes: ByteArray?,
        focusRegion: com.district37.toastmasters.models.FocusRegion?,
        entityId: Int,
        entityTypeString: String,
        tag: String
    ): ImageHandlingResult {
        val finalImageUrl: String? = if (imageBytes != null) {
            val uploadResult = imageUploadRepository.uploadImage(
                imageBytes = imageBytes,
                entityType = entityTypeString,
                entityId = entityId.toString(),
                filename = "${entityTypeString}_${entityId}_cover.jpg",
                contentType = "image/jpeg"
            )

            when (uploadResult) {
                is Resource.Success -> {
                    Logger.d(tag, "Image uploaded successfully: ${uploadResult.data}")
                    uploadResult.data
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to upload image: ${uploadResult.message}")
                    return ImageHandlingResult(null, uploadResult.message)
                }
                else -> null
            }
        } else {
            null
        }

        return ImageHandlingResult(finalImageUrl, null)
    }

    /**
     * Create an image record in the database
     *
     * @param imageUrl The final image URL (from upload or user input)
     * @param entityType The GraphQL EntityType enum
     * @param entityId The entity ID to link to
     * @param altText Optional alt text for accessibility
     * @param tag Logging tag
     * @return Resource containing created Image or Error
     */
    suspend fun createImageRecord(
        imageUrl: String,
        entityType: EntityType,
        entityId: Int,
        altText: String? = null,
        tag: String
    ): Resource<Image> {
        val imageInput = CreateImageInput(
            url = imageUrl,
            entityType = entityType,
            entityId = entityId,
            altText = Optional.presentIfNotNull(altText),
            caption = Optional.absent(),
            displayOrder = Optional.present(0)
        )

        val result = imageRepository.createImage(imageInput)

        when (result) {
            is Resource.Success -> {
                Logger.d(tag, "Image created and linked to entity: ${result.data.id}")
            }
            is Resource.Error -> {
                Logger.e(tag, "Failed to create image record: ${result.message}")
            }
            else -> {}
        }

        return result
    }

    /**
     * Convenience method that handles both upload and record creation
     *
     * @param imageBytes Optional device image bytes
     * @param focusRegion Optional focus region for cropping
     * @param entityId ID of the entity to associate with
     * @param entityType GraphQL EntityType enum
     * @param entityTypeString Entity type string for upload path
     * @param altText Optional alt text
     * @param tag Logging tag
     * @return Resource containing created Image, or Error, or null if no image
     */
    suspend fun handleImageUploadAndRecord(
        imageBytes: ByteArray?,
        focusRegion: com.district37.toastmasters.models.FocusRegion?,
        entityId: Int,
        entityType: EntityType,
        entityTypeString: String,
        altText: String? = null,
        tag: String
    ): Resource<Image>? {
        // Upload image
        val uploadResult = handleImageUpload(
            imageBytes = imageBytes,
            focusRegion = focusRegion,
            entityId = entityId,
            entityTypeString = entityTypeString,
            tag = tag
        )

        // If upload failed, return error
        if (uploadResult.error != null) {
            return Resource.Error(
                errorType = ErrorType.UNKNOWN_ERROR,
                message = uploadResult.error
            )
        }

        // If no image URL, return null (no image provided)
        val finalImageUrl = uploadResult.imageUrl ?: return null

        // Create image record
        return createImageRecord(
            imageUrl = finalImageUrl,
            entityType = entityType,
            entityId = entityId,
            altText = altText,
            tag = tag
        )
    }
}
