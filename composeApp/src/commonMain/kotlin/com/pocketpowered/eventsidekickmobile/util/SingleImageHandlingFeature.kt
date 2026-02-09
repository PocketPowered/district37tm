package com.district37.toastmasters.util

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.graphql.type.CreateImageInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.FocusRegionInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.models.ImageSelectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Reusable feature for single image handling in Edit ViewModels
 *
 * Provides state management and operations for:
 * - Image selection (device or URL)
 * - Image upload to server
 * - Focus region management
 * - Image record creation
 *
 * Usage:
 * ```kotlin
 * class EditSomeViewModel(...) : BaseEditViewModel<T>(...) {
 *     private val imageHandler = SingleImageHandlingFeature(
 *         imageUploadRepository, imageRepository, tag
 *     )
 *
 *     val imageUrl = imageHandler.imageUrl
 *     val selectedImageBytes = imageHandler.selectedImageBytes
 *     val isUploadingImage = imageHandler.isUploadingImage
 *
 *     fun onImageSelected(bytes: ByteArray, focusRegion: FocusRegion?) {
 *         imageHandler.onImageSelected(bytes, focusRegion)
 *     }
 * }
 * ```
 */
class SingleImageHandlingFeature(
    private val imageUploadRepository: ImageUploadRepository,
    private val imageRepository: ImageRepository,
    private val tag: String
) {
    // Image URL state
    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    // Selected image bytes (from device)
    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    // Image upload progress state
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Focus region for smart cropping
    private val _selectedFocusRegion = MutableStateFlow<FocusRegion?>(null)
    val selectedFocusRegion: StateFlow<FocusRegion?> = _selectedFocusRegion.asStateFlow()

    // Track the existing image URL to detect changes
    private var existingImageUrl: String? = null

    /**
     * Set the existing image URL (typically called when loading entity)
     */
    fun setExistingImage(url: String?) {
        existingImageUrl = url
        _imageUrl.value = url ?: ""
    }

    /**
     * Handle image selection from device
     */
    fun onImageSelected(bytes: ByteArray, focusRegion: FocusRegion? = null) {
        _selectedImageBytes.value = bytes
        _selectedFocusRegion.value = focusRegion
        _imageUrl.value = "" // Clear URL when device image is selected
    }

    /**
     * Handle image URL change (user entered URL)
     */
    fun onImageUrlChanged(url: String) {
        _imageUrl.value = url
        _selectedImageBytes.value = null // Clear device image when URL is entered
        _selectedFocusRegion.value = null
    }

    /**
     * Clear all image data
     */
    fun clearImage() {
        _imageUrl.value = ""
        _selectedImageBytes.value = null
        _selectedFocusRegion.value = null
    }

    /**
     * Set image from ImageSelectionResult (from image wizard)
     */
    fun setImageSelection(result: ImageSelectionResult) {
        when {
            result.isDeviceImage -> {
                _selectedImageBytes.value = result.imageBytes
                _selectedFocusRegion.value = result.focusRegion
                _imageUrl.value = ""
            }
            else -> {
                clearImage()
            }
        }
    }

    /**
     * Data class to hold image submission result
     */
    data class ImageHandlingResult(
        val imageUrl: String?,
        val error: Resource.Error?
    )

    /**
     * Handle image submission:
     * 1. Upload device image if selected, or
     * 2. Use URL if entered and different from existing
     *
     * @param entityId The ID of the entity (venue, performer, etc.)
     * @param entityType The type of entity (for upload path)
     * @return Pair of (finalImageUrl, error) - imageUrl is null if no new image
     */
    suspend fun handleImageSubmission(
        entityId: Int,
        entityType: String
    ): ImageHandlingResult {
        val imageBytes = _selectedImageBytes.value
        var finalImageUrl: String? = null

        if (imageBytes != null) {
            _isUploadingImage.update { true }
            Logger.d(tag, "Uploading $entityType image...")

            val uploadResult = imageUploadRepository.uploadImage(
                imageBytes = imageBytes,
                entityType = entityType,
                entityId = entityId.toString(),
                filename = "${entityType}_${entityId}_cover.jpg",
                contentType = "image/jpeg"
            )

            _isUploadingImage.update { false }

            when (uploadResult) {
                is Resource.Success -> {
                    Logger.d(tag, "Image uploaded successfully: ${uploadResult.data}")
                    finalImageUrl = uploadResult.data
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to upload image: ${uploadResult.message}")
                    return ImageHandlingResult(null, uploadResult)
                }
                else -> {}
            }
        } else if (_imageUrl.value.isNotBlank() && _imageUrl.value != existingImageUrl) {
            // URL was entered directly and is different from existing
            finalImageUrl = _imageUrl.value
        }

        return ImageHandlingResult(finalImageUrl, null)
    }

    /**
     * Create image record in the database
     * Called after entity is successfully created/updated
     *
     * @param entityId The ID of the entity
     * @param entityType The GraphQL EntityType enum
     * @param imageUrl The final image URL (from upload or user input)
     * @param altText Optional alt text for accessibility
     * @return Resource containing created Image or Error
     */
    suspend fun createImageRecord(
        entityId: Int,
        entityType: EntityType,
        imageUrl: String,
        altText: String? = null
    ): Resource<Image> {
        val focusRegionInput = _selectedFocusRegion.value?.let {
            Optional.present(FocusRegionInput(
                x = it.x.toDouble(),
                y = it.y.toDouble(),
                width = it.width.toDouble(),
                height = it.height.toDouble()
            ))
        } ?: Optional.absent()

        val imageInput = CreateImageInput(
            url = imageUrl,
            entityType = entityType,
            entityId = entityId,
            displayOrder = Optional.present(0),
            focusRegion = focusRegionInput,
            altText = Optional.presentIfNotNull(altText),
            caption = Optional.absent()
        )

        val result = imageRepository.createImage(imageInput)
        if (result is Resource.Error) {
            Logger.e(tag, "Failed to create image record: ${result.message}")
        } else if (result is Resource.Success) {
            Logger.d(tag, "Image record created successfully")
        }

        return result
    }
}
