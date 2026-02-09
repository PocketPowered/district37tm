package com.district37.toastmasters.features.create.performer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.graphql.type.CreatePerformerInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.Performer
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
 * Common performer types for selection
 */
enum class PerformerTypeOption(val displayName: String) {
    SPEAKER("Speaker"),
    MUSICIAN("Musician"),
    BAND("Band"),
    DJ("DJ"),
    COMEDIAN("Comedian"),
    ACTOR("Actor"),
    DANCER("Dancer"),
    ARTIST("Artist"),
    HOST("Host/MC"),
    PANELIST("Panelist"),
    INSTRUCTOR("Instructor"),
    ATHLETE("Athlete"),
    OTHER("Other")
}

/**
 * ViewModel for the Create Performer Wizard flow.
 * Manages step navigation and form data collection across multiple steps.
 *
 * Steps:
 * 1. Name - What's the performer's name?
 * 2. Type - What type of performer?
 * 3. Bio - Tell us about them (optional)
 */
class CreatePerformerWizardViewModel(
    private val performerRepository: PerformerRepository,
    private val wizardImageHandler: WizardImageHandler
) : ViewModel() {

    companion object {
        private const val TAG = "CreatePerformerWizardViewModel"
        const val TOTAL_STEPS = 4
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    val totalSteps = TOTAL_STEPS

    // Step 1: Performer Name
    private val _performerName = MutableStateFlow("")
    val performerName: StateFlow<String> = _performerName.asStateFlow()

    // Step 2: Performer Type
    private val _performerType = MutableStateFlow<PerformerTypeOption?>(null)
    val performerType: StateFlow<PerformerTypeOption?> = _performerType.asStateFlow()
    val availablePerformerTypes: List<PerformerTypeOption> = PerformerTypeOption.entries

    // Step 3: Bio (optional)
    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    // Step 4: Image (optional) - gallery only with focus region
    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    private val _imageFocusRegion = MutableStateFlow<com.district37.toastmasters.models.FocusRegion?>(null)

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _creationResult = MutableStateFlow<Resource<Performer>?>(null)
    val creationResult: StateFlow<Resource<Performer>?> = _creationResult.asStateFlow()

    // Derived state for whether user can proceed to next step
    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _performerName
    ) { step, name ->
        when (step) {
            0 -> name.trim().length >= 3 // Step 1: Name required (at least 3 characters)
            1 -> true // Step 2: Type is optional
            2 -> true // Step 3: Bio is optional
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
        _performerName.update { name }
    }

    // Step 2: Performer Type
    fun selectPerformerType(type: PerformerTypeOption?) {
        _performerType.update { type }
    }

    // Step 3: Bio
    fun updateBio(value: String) {
        _bio.update { value }
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
    fun createPerformer() {
        if (!canProceed.value) return

        viewModelScope.launch {
            _isSubmitting.update { true }
            _creationResult.update { Resource.Loading }

            val input = CreatePerformerInput(
                name = _performerName.value.trim(),
                performerType = Optional.presentIfNotNull(_performerType.value?.displayName),
                bio = Optional.presentIfNotNull(_bio.value.trim().takeIf { it.isNotBlank() })
            )

            val result = performerRepository.createPerformer(input)

            if (result is Resource.Success) {
                Logger.d(TAG, "Performer created successfully: ${result.data.id}")

                // Handle image via WizardImageHandler
                wizardImageHandler.handleImageUploadAndRecord(
                    imageBytes = _selectedImageBytes.value,
                    focusRegion = _imageFocusRegion.value,
                    entityId = result.data.id,
                    entityType = EntityType.PERFORMER,
                    entityTypeString = "performer",
                    altText = _performerName.value.trim(),
                    tag = TAG
                )
            } else if (result is Resource.Error) {
                Logger.e(TAG, "Failed to create performer: ${result.message}")
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
