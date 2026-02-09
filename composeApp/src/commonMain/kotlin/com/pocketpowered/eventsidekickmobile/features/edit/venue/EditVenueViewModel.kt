package com.district37.toastmasters.features.edit.venue

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UpdateVenueInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.SingleImageHandlingFeature
import com.district37.toastmasters.viewmodel.BaseEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for editing an existing venue
 *
 * Extends BaseEditViewModel to handle loading the venue data,
 * and provides form fields for editing venue properties.
 *
 * Refactored to use SingleImageHandlingFeature for image management.
 */
class EditVenueViewModel(
    private val venueRepository: VenueRepository,
    private val locationRepository: LocationRepository,
    imageUploadRepository: ImageUploadRepository,
    imageRepository: ImageRepository,
    venueId: Int
) : BaseEditViewModel<Venue>(venueId, venueRepository) {

    override val tag = "EditVenueViewModel"

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

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _zipCode = MutableStateFlow("")
    val zipCode: StateFlow<String> = _zipCode.asStateFlow()

    private val _capacity = MutableStateFlow("")
    val capacity: StateFlow<String> = _capacity.asStateFlow()

    // Venue locations
    private val _venueLocations = MutableStateFlow<List<Location>>(emptyList())
    val venueLocations: StateFlow<List<Location>> = _venueLocations.asStateFlow()

    private val _isLoadingLocations = MutableStateFlow(false)
    val isLoadingLocations: StateFlow<Boolean> = _isLoadingLocations.asStateFlow()

    override fun mapEntityToFields(entity: Venue) {
        _name.value = entity.name
        _address.value = entity.address ?: ""
        _city.value = entity.city ?: ""
        _state.value = entity.state ?: ""
        _zipCode.value = entity.zipCode ?: ""
        _capacity.value = entity.capacity?.toString() ?: ""

        // Set existing image in feature
        imageHandler.setExistingImage(entity.images.firstOrNull()?.url)

        // Load locations for this venue
        loadVenueLocations()
    }

    private fun loadVenueLocations() {
        viewModelScope.launch {
            _isLoadingLocations.value = true
            when (val result = locationRepository.searchLocations(venueId = entityId)) {
                is Resource.Success -> {
                    _venueLocations.value = result.data.items
                    Logger.d(tag, "Loaded ${result.data.items.size} locations for venue $entityId")
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to load locations: ${result.message}")
                }
                else -> {}
            }
            _isLoadingLocations.value = false
        }
    }

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateAddress(value: String) {
        _address.update { value }
    }

    fun updateCity(value: String) {
        _city.update { value }
    }

    fun updateState(value: String) {
        _state.update { value }
    }

    fun updateZipCode(value: String) {
        _zipCode.update { value }
    }

    fun updateCapacity(value: String) {
        // Only allow numeric input
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _capacity.update { value }
            clearFieldError("capacity")
        }
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
            setFieldError("name", "Venue name is required")
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

    override suspend fun submitForm(): Resource<Venue> {
        // Handle image upload via feature
        val imageResult = imageHandler.handleImageSubmission(
            entityId = entityId,
            entityType = "venue"
        )

        // If image upload failed, return error
        if (imageResult.error != null) {
            return imageResult.error
        }

        // Update the venue
        val input = UpdateVenueInput(
            name = Optional.presentIfNotNull(_name.value.trim().takeIf { it.isNotBlank() }),
            address = Optional.presentIfNotNull(_address.value.trim().takeIf { it.isNotBlank() }),
            city = Optional.presentIfNotNull(_city.value.trim().takeIf { it.isNotBlank() }),
            state = Optional.presentIfNotNull(_state.value.trim().takeIf { it.isNotBlank() }),
            zipCode = Optional.presentIfNotNull(_zipCode.value.trim().takeIf { it.isNotBlank() }),
            capacity = Optional.presentIfNotNull(_capacity.value.toIntOrNull())
        )

        val result = venueRepository.updateVenue(entityId, input)

        // Create image record if we have a new image URL
        if (result is Resource.Success && imageResult.imageUrl != null) {
            imageHandler.createImageRecord(
                entityId = entityId,
                entityType = EntityType.VENUE,
                imageUrl = imageResult.imageUrl,
                altText = _name.value.trim()
            )
            // Don't fail the whole operation if image record creation fails
        }

        return result
    }
}
