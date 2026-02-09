package com.district37.toastmasters.features.edit.performer

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UpdatePerformerInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.SingleImageHandlingFeature
import com.district37.toastmasters.viewmodel.BaseEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for editing an existing performer
 *
 * Extends BaseEditViewModel to handle loading the performer data,
 * and provides form fields for editing performer properties.
 *
 * Refactored to use SingleImageHandlingFeature for image management.
 */
class EditPerformerViewModel(
    private val performerRepository: PerformerRepository,
    imageUploadRepository: ImageUploadRepository,
    imageRepository: ImageRepository,
    performerId: Int
) : BaseEditViewModel<Performer>(performerId, performerRepository) {

    override val tag = "EditPerformerViewModel"

    // Image handling feature
    private val imageHandler = SingleImageHandlingFeature(
        imageUploadRepository = imageUploadRepository,
        imageRepository = imageRepository,
        tag = tag
    )

    // Expose image state from feature
    val imageUrl = imageHandler.imageUrl
    val selectedImageBytes = imageHandler.selectedImageBytes
    val isUploadingImage = imageHandler.isUploadingImage
    val selectedFocusRegion = imageHandler.selectedFocusRegion

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _performerType = MutableStateFlow("")
    val performerType: StateFlow<String> = _performerType.asStateFlow()

    override fun mapEntityToFields(entity: Performer) {
        _name.value = entity.name
        _bio.value = entity.bio ?: ""
        _performerType.value = entity.performerType ?: ""

        // Set existing image in feature
        imageHandler.setExistingImage(entity.images.firstOrNull()?.url)
    }

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateBio(value: String) {
        _bio.update { value }
    }

    fun updatePerformerType(value: String) {
        _performerType.update { value }
    }

    // Image update functions - delegate to feature
    fun onImageSelected(bytes: ByteArray, focusRegion: FocusRegion? = null) {
        imageHandler.onImageSelected(bytes, focusRegion)
    }

    fun onImageUrlChanged(url: String) {
        imageHandler.onImageUrlChanged(url)
    }

    fun clearImage() {
        imageHandler.clearImage()
    }

    fun setImageSelection(result: ImageSelectionResult) {
        imageHandler.setImageSelection(result)
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            setFieldError("name", "Performer name is required")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Performer> {
        // Handle image upload via feature
        val imageResult = imageHandler.handleImageSubmission(
            entityId = entityId,
            entityType = "performer"
        )

        // If image upload failed, return error
        if (imageResult.error != null) {
            return imageResult.error
        }

        // Update the performer
        val input = UpdatePerformerInput(
            name = Optional.presentIfNotNull(_name.value.trim().takeIf { it.isNotBlank() }),
            bio = Optional.presentIfNotNull(_bio.value.trim().takeIf { it.isNotBlank() }),
            performerType = Optional.presentIfNotNull(_performerType.value.trim().takeIf { it.isNotBlank() })
        )

        val result = performerRepository.updatePerformer(entityId, input)

        // Create image record if we have a new image URL
        if (result is Resource.Success && imageResult.imageUrl != null) {
            imageHandler.createImageRecord(
                entityId = entityId,
                entityType = EntityType.PERFORMER,
                imageUrl = imageResult.imageUrl,
                altText = _name.value.trim()
            )
            // Don't fail the whole operation if image record creation fails
        }

        return result
    }
}
