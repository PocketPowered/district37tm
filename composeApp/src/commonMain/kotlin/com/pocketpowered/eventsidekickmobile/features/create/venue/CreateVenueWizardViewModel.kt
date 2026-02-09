package com.district37.toastmasters.features.create.venue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.CreateVenueInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.WizardImageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Create Venue Wizard flow.
 * Manages step navigation and form data collection across multiple steps.
 *
 * Steps:
 * 1. Name - What's the venue called?
 * 2. Location - Where is it located? (address, city, state, zip)
 * 3. Capacity - What is the capacity? (optional)
 */
class CreateVenueWizardViewModel(
    private val venueRepository: VenueRepository,
    private val wizardImageHandler: WizardImageHandler
) : ViewModel() {

    companion object {
        private const val TAG = "CreateVenueWizardViewModel"
        const val TOTAL_STEPS = 4
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    val totalSteps = TOTAL_STEPS

    // Step 1: Venue Name
    private val _venueName = MutableStateFlow("")
    val venueName: StateFlow<String> = _venueName.asStateFlow()

    // Step 2: Location
    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _zipCode = MutableStateFlow("")
    val zipCode: StateFlow<String> = _zipCode.asStateFlow()

    // Step 3: Capacity (optional)
    private val _capacity = MutableStateFlow("")
    val capacity: StateFlow<String> = _capacity.asStateFlow()

    // Step 4: Image (optional) - gallery only with focus region
    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    private val _imageFocusRegion = MutableStateFlow<com.district37.toastmasters.models.FocusRegion?>(null)

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _creationResult = MutableStateFlow<Resource<Venue>?>(null)
    val creationResult: StateFlow<Resource<Venue>?> = _creationResult.asStateFlow()

    // Derived state for whether user can proceed to next step
    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _venueName,
        _address,
        _city,
        _state,
        _zipCode
    ) { values ->
        val step = values[0] as Int
        val name = values[1] as String
        val address = values[2] as String
        val city = values[3] as String
        val state = values[4] as String
        val zipCode = values[5] as String

        when (step) {
            0 -> name.trim().length >= 3 // Step 1: Name required (at least 3 characters)
            1 -> address.isNotBlank() && city.isNotBlank() && state.isNotBlank() && zipCode.isNotBlank() // Step 2: Location required
            2 -> true // Step 3: Capacity is optional
            3 -> true // Step 4: Image URL is optional
            else -> false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Navigation
    fun goToNextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.update { it + 1 }
        }
    }

    /**
     * Go to previous step.
     * @return false if already at first step (caller should handle exit)
     */
    fun goToPreviousStep(): Boolean {
        return if (_currentStep.value > 0) {
            _currentStep.update { it - 1 }
            true
        } else {
            false
        }
    }

    // Step 1: Name
    fun updateName(name: String) {
        _venueName.update { name }
    }

    // Step 2: Location
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

    // Step 3: Capacity
    fun updateCapacity(value: String) {
        // Only allow digits
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _capacity.update { value }
        }
    }

    // Step 4: Image
    fun setImageSelection(result: com.district37.toastmasters.models.ImageSelectionResult) {
        _selectedImageBytes.update { result.imageBytes }
        _imageFocusRegion.update { result.focusRegion }
    }

    fun clearImageSelection() {
        _selectedImageBytes.update { null }
        _imageFocusRegion.update { null }
    }

    // Submission
    fun createVenue() {
        if (!canProceed.value) return

        viewModelScope.launch {
            _isSubmitting.update { true }
            _creationResult.update { Resource.Loading }

            val input = CreateVenueInput(
                name = _venueName.value.trim(),
                address = Optional.presentIfNotNull(_address.value.trim().takeIf { it.isNotBlank() }),
                city = Optional.presentIfNotNull(_city.value.trim().takeIf { it.isNotBlank() }),
                state = Optional.presentIfNotNull(_state.value.trim().takeIf { it.isNotBlank() }),
                zipCode = Optional.presentIfNotNull(_zipCode.value.trim().takeIf { it.isNotBlank() }),
                capacity = Optional.presentIfNotNull(_capacity.value.toIntOrNull())
            )

            val result = venueRepository.createVenue(input)

            if (result is Resource.Success) {
                Logger.d(TAG, "Venue created successfully: ${result.data.id}")

                // Handle image via WizardImageHandler
                wizardImageHandler.handleImageUploadAndRecord(
                    imageBytes = _selectedImageBytes.value,
                    focusRegion = _imageFocusRegion.value,
                    entityId = result.data.id,
                    entityType = EntityType.VENUE,
                    entityTypeString = "venue",
                    altText = _venueName.value.trim(),
                    tag = TAG
                )
            } else if (result is Resource.Error) {
                Logger.e(TAG, "Failed to create venue: ${result.message}")
            }

            _creationResult.update { result }
            _isSubmitting.update { false }
        }
    }

    /**
     * Reset the creation result (e.g., after handling error)
     */
    fun resetCreationResult() {
        _creationResult.update { null }
    }
}
