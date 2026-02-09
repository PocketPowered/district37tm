package com.district37.toastmasters.util

import androidx.compose.ui.graphics.ImageBitmap
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.features.common.PendingImageState
import com.district37.toastmasters.graphql.type.CreateImageInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.FocusRegionInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.models.ImageSelectionResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

/**
 * Reusable feature for multi-image handling in Edit ViewModels
 *
 * Provides state management and operations for:
 * - Managing existing images from the entity
 * - Adding pending images (not yet uploaded)
 * - Marking images for deletion
 * - Uploading pending images (in parallel)
 * - Creating image records after upload
 * - Reordering images
 * - Updating focus regions
 *
 * Usage:
 * ```kotlin
 * class EditEventViewModel(
 *     private val eventRepository: EventRepository,
 *     imageUploadRepository: ImageUploadRepository,
 *     imageRepository: ImageRepository,
 *     eventId: Int
 * ) : BaseEditViewModel<Event>(eventId, eventRepository) {
 *
 *     private val imageHandler = MultiImageHandlingFeature(
 *         imageUploadRepository = imageUploadRepository,
 *         imageRepository = imageRepository,
 *         tag = tag
 *     )
 *
 *     // Expose state
 *     val existingImages = imageHandler.existingImages
 *     val pendingImages = imageHandler.pendingImages
 *     val imagesToDelete = imageHandler.imagesToDelete
 *     val isUploadingImages = imageHandler.isUploadingImages
 *
 *     override fun mapEntityToFields(entity: Event) {
 *         imageHandler.setExistingImages(entity.images)
 *     }
 *
 *     // Delegate image operations
 *     fun addPendingImage(result: ImageSelectionResult) = imageHandler.addPendingImage(result)
 *     fun deletePendingImage(imageId: String) = imageHandler.deletePendingImage(imageId)
 *     fun deleteExistingImage(imageId: Int) = imageHandler.deleteExistingImage(imageId)
 *
 *     override suspend fun submitForm(): Resource<Event> {
 *         // Handle image operations
 *         val imageResult = imageHandler.handleImageSubmission(entityId, EntityType.EVENT)
 *         if (imageResult.error != null) return imageResult.error
 *
 *         // Update entity
 *         val result = eventRepository.updateEvent(entityId, input)
 *
 *         // Create image records for uploaded images
 *         if (result is Resource.Success) {
 *             imageHandler.createImageRecords(entityId, EntityType.EVENT, imageResult.uploadedImages)
 *             imageHandler.clearPendingState()
 *         }
 *
 *         return result
 *     }
 * }
 * ```
 */
class MultiImageHandlingFeature(
    private val imageUploadRepository: ImageUploadRepository,
    private val imageRepository: ImageRepository,
    private val tag: String
) {
    // Existing images from the entity
    private val _existingImages = MutableStateFlow<List<Image>>(emptyList())
    val existingImages: StateFlow<List<Image>> = _existingImages.asStateFlow()

    // Pending images (selected but not yet uploaded)
    private val _pendingImages = MutableStateFlow<List<PendingImageState>>(emptyList())
    val pendingImages: StateFlow<List<PendingImageState>> = _pendingImages.asStateFlow()

    // Images marked for deletion
    private val _imagesToDelete = MutableStateFlow<Set<Int>>(emptySet())
    val imagesToDelete: StateFlow<Set<Int>> = _imagesToDelete.asStateFlow()

    // Upload progress state
    private val _isUploadingImages = MutableStateFlow(false)
    val isUploadingImages: StateFlow<Boolean> = _isUploadingImages.asStateFlow()

    /**
     * Set the existing images from the loaded entity.
     * Call this in mapEntityToFields.
     */
    fun setExistingImages(images: List<Image>) {
        _existingImages.value = images
    }

    /**
     * Add a pending image from an ImageSelectionResult.
     */
    fun addPendingImage(result: ImageSelectionResult) {
        val nextOrder = _existingImages.value.size + _pendingImages.value.size
        val pendingImage = PendingImageState(
            id = "${Clock.System.now().toEpochMilliseconds()}_$nextOrder",
            bitmap = result.imageBitmap,
            imageBytes = result.imageBytes ?: return, // Skip if no bytes
            focusRegion = result.focusRegion,
            displayOrder = nextOrder
        )
        _pendingImages.update { it + pendingImage }
    }

    /**
     * Add a pending image with raw data.
     */
    fun addPendingImage(
        bitmap: ImageBitmap?,
        imageBytes: ByteArray,
        focusRegion: FocusRegion? = null
    ) {
        val nextOrder = _existingImages.value.size + _pendingImages.value.size
        val pendingImage = PendingImageState(
            id = "${Clock.System.now().toEpochMilliseconds()}_$nextOrder",
            bitmap = bitmap,
            imageBytes = imageBytes,
            focusRegion = focusRegion,
            displayOrder = nextOrder
        )
        _pendingImages.update { it + pendingImage }
    }

    /**
     * Delete a pending image by its temporary ID.
     */
    fun deletePendingImage(imageId: String) {
        _pendingImages.update { images ->
            images.filter { it.id != imageId }
        }
    }

    /**
     * Mark an existing image for deletion.
     * The image is removed from the UI list immediately but not deleted from server until submit.
     */
    fun deleteExistingImage(imageId: Int) {
        _imagesToDelete.update { it + imageId }
        _existingImages.update { images ->
            images.filter { it.id != imageId }
        }
    }

    /**
     * Update the focus region of an existing image.
     * This updates the image on the server immediately.
     */
    suspend fun updateExistingImageFocusRegion(imageId: Int, focusRegion: FocusRegion) {
        val result = imageRepository.updateImageFocusRegion(imageId, focusRegion)
        if (result is Resource.Success) {
            _existingImages.update { images ->
                images.map { image ->
                    if (image.id == imageId) {
                        image.copy(focusRegion = focusRegion)
                    } else {
                        image
                    }
                }
            }
        } else if (result is Resource.Error) {
            Logger.e(tag, "Failed to update image focus region: ${result.message}")
        }
    }

    /**
     * Reorder images (both existing and pending).
     * Takes a list of Any containing Image and PendingImageState objects.
     */
    fun reorderImages(reorderedList: List<Any>) {
        val newExistingImages = mutableListOf<Image>()
        val newPendingImages = mutableListOf<PendingImageState>()

        reorderedList.forEachIndexed { index, item ->
            when (item) {
                is Image -> newExistingImages.add(item)
                is PendingImageState -> newPendingImages.add(item.copy(displayOrder = index))
            }
        }

        _existingImages.value = newExistingImages
        _pendingImages.value = newPendingImages
    }

    /**
     * Result of image submission handling.
     */
    data class MultiImageHandlingResult(
        val uploadedImages: List<UploadedImageInfo>,
        val error: Resource.Error?
    )

    /**
     * Information about an uploaded image.
     */
    data class UploadedImageInfo(
        val url: String,
        val focusRegion: FocusRegion?,
        val displayOrder: Int
    )

    /**
     * Handle image submission:
     * 1. Delete marked images
     * 2. Upload pending images in parallel
     *
     * @param entityId The ID of the entity
     * @param entityType The entity type string for upload path (e.g., "event", "venue")
     * @return Result containing uploaded image info or error
     */
    suspend fun handleImageSubmission(
        entityId: Int,
        entityType: String
    ): MultiImageHandlingResult = coroutineScope {
        // 1. Delete marked images
        _imagesToDelete.value.forEach { imageId ->
            val deleteResult = imageRepository.deleteImage(imageId)
            if (deleteResult is Resource.Error) {
                Logger.e(tag, "Failed to delete image $imageId: ${deleteResult.message}")
                // Continue anyway - don't fail the whole operation
            }
        }

        // 2. Upload pending images in parallel
        _isUploadingImages.value = true
        val pendingImagesList = _pendingImages.value

        val uploadResults = pendingImagesList.map { pendingImage ->
            async {
                Logger.d(tag, "Uploading $entityType image ${pendingImage.displayOrder + 1}...")

                val uploadResult = imageUploadRepository.uploadImage(
                    imageBytes = pendingImage.imageBytes,
                    entityType = entityType,
                    entityId = entityId.toString(),
                    filename = "${entityType}_${entityId}_${Clock.System.now().toEpochMilliseconds()}.jpg",
                    contentType = "image/jpeg"
                )

                when (uploadResult) {
                    is Resource.Success -> {
                        Logger.d(tag, "Image uploaded successfully: ${uploadResult.data}")
                        Result.success(
                            UploadedImageInfo(
                                url = uploadResult.data,
                                focusRegion = pendingImage.focusRegion,
                                displayOrder = pendingImage.displayOrder
                            )
                        )
                    }
                    is Resource.Error -> {
                        Logger.e(tag, "Failed to upload image: ${uploadResult.message}")
                        Result.failure(Exception(uploadResult.message))
                    }
                    else -> Result.failure(Exception("Unexpected result"))
                }
            }
        }.awaitAll()

        _isUploadingImages.value = false

        // Check for any failures
        val failure = uploadResults.firstOrNull { it.isFailure }
        if (failure != null) {
            return@coroutineScope MultiImageHandlingResult(
                uploadedImages = emptyList(),
                error = Resource.Error(
                    errorType = ErrorType.UNKNOWN_ERROR,
                    message = failure.exceptionOrNull()?.message ?: "Failed to upload image"
                )
            )
        }

        // All succeeded
        MultiImageHandlingResult(
            uploadedImages = uploadResults.mapNotNull { it.getOrNull() },
            error = null
        )
    }

    /**
     * Create image records for uploaded images.
     * Call this after the entity is successfully updated.
     *
     * @param entityId The ID of the entity
     * @param entityType The GraphQL EntityType enum
     * @param uploadedImages List of uploaded image info from handleImageSubmission
     */
    suspend fun createImageRecords(
        entityId: Int,
        entityType: EntityType,
        uploadedImages: List<UploadedImageInfo>
    ) {
        uploadedImages.forEach { uploadedImage ->
            val focusRegionInput = uploadedImage.focusRegion?.let {
                Optional.present(
                    FocusRegionInput(
                        x = it.x.toDouble(),
                        y = it.y.toDouble(),
                        width = it.width.toDouble(),
                        height = it.height.toDouble()
                    )
                )
            } ?: Optional.absent()

            val imageInput = CreateImageInput(
                url = uploadedImage.url,
                entityType = entityType,
                entityId = entityId,
                displayOrder = Optional.present(uploadedImage.displayOrder),
                focusRegion = focusRegionInput
            )

            val imageResult = imageRepository.createImage(imageInput)
            if (imageResult is Resource.Error) {
                Logger.e(tag, "Failed to create image record: ${imageResult.message}")
                // Don't fail the whole operation
            }
        }
    }

    /**
     * Clear pending state after successful submission.
     */
    fun clearPendingState() {
        _pendingImages.value = emptyList()
        _imagesToDelete.value = emptySet()
    }

    /**
     * Reset all state (for when the entity is reloaded).
     */
    fun reset() {
        _existingImages.value = emptyList()
        _pendingImages.value = emptyList()
        _imagesToDelete.value = emptySet()
        _isUploadingImages.value = false
    }
}
