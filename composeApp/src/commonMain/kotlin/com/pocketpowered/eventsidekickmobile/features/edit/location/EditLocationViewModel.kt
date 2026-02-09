package com.district37.toastmasters.features.edit.location

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UpdateLocationInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.SingleImageHandlingFeature
import com.district37.toastmasters.viewmodel.BaseEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for editing an existing location
 *
 * Extends BaseEditViewModel to handle loading the location data,
 * and provides form fields for editing location properties.
 *
 * Refactored to use SingleImageHandlingFeature for image management.
 */
class EditLocationViewModel(
    private val locationRepository: LocationRepository,
    imageUploadRepository: ImageUploadRepository,
    imageRepository: ImageRepository,
    locationId: Int
) : BaseEditViewModel<Location>(locationId, locationRepository) {

    override val tag = "EditLocationViewModel"

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

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _locationType = MutableStateFlow("")
    val locationType: StateFlow<String> = _locationType.asStateFlow()

    private val _capacity = MutableStateFlow("")
    val capacity: StateFlow<String> = _capacity.asStateFlow()

    private val _floorLevel = MutableStateFlow("")
    val floorLevel: StateFlow<String> = _floorLevel.asStateFlow()

    override fun mapEntityToFields(entity: Location) {
        _name.value = entity.name
        _description.value = entity.description ?: ""
        _locationType.value = entity.locationType ?: ""
        _capacity.value = entity.capacity?.toString() ?: ""
        _floorLevel.value = entity.floorLevel ?: ""

        // Set existing image in feature
        imageHandler.setExistingImage(entity.images.firstOrNull()?.url)
    }

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateDescription(value: String) {
        _description.update { value }
    }

    fun updateLocationType(value: String) {
        _locationType.update { value }
    }

    fun updateCapacity(value: String) {
        // Only allow numeric input
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _capacity.update { value }
            clearFieldError("capacity")
        }
    }

    fun updateFloorLevel(value: String) {
        _floorLevel.update { value }
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
            setFieldError("name", "Location name is required")
            isValid = false
        }

        if (_capacity.value.isNotEmpty()) {
            val capacityInt = _capacity.value.toIntOrNull()
            if (capacityInt == null || capacityInt < 0) {
                setFieldError("capacity", "Capacity must be a positive number")
                isValid = false
            }
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Location> {
        // Handle image upload via feature
        val imageResult = imageHandler.handleImageSubmission(
            entityId = entityId,
            entityType = "location"
        )

        // If image upload failed, return error
        if (imageResult.error != null) {
            return imageResult.error
        }

        // Update the location
        val input = UpdateLocationInput(
            name = Optional.presentIfNotNull(_name.value.trim().takeIf { it.isNotBlank() }),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            locationType = Optional.presentIfNotNull(_locationType.value.trim().takeIf { it.isNotBlank() }),
            capacity = Optional.presentIfNotNull(_capacity.value.toIntOrNull()),
            floorLevel = Optional.presentIfNotNull(_floorLevel.value.trim().takeIf { it.isNotBlank() })
        )

        val result = locationRepository.updateLocation(entityId, input)

        // Create image record if we have a new image URL
        if (result is Resource.Success && imageResult.imageUrl != null) {
            imageHandler.createImageRecord(
                entityId = entityId,
                entityType = EntityType.LOCATION,
                imageUrl = imageResult.imageUrl,
                altText = _name.value.trim()
            )
            // Don't fail the whole operation if image record creation fails
        }

        return result
    }
}
