package com.district37.toastmasters.features.create.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.graphql.type.CreateOrganizationInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.WizardImageHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Create Organization Wizard flow.
 * Manages a 5-step wizard: Tag -> Name -> Description -> Colors -> Logo
 */
@OptIn(FlowPreview::class)
class CreateOrganizationWizardViewModel(
    private val organizationRepository: OrganizationRepository,
    private val wizardImageHandler: WizardImageHandler
) : ViewModel() {

    companion object {
        private const val TAG = "CreateOrganizationWizardViewModel"
        private const val TAG_REGEX = "^[a-z0-9_]{3,18}$"
        private const val DEBOUNCE_MILLIS = 500L
        const val TOTAL_STEPS = 5
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    val totalSteps = TOTAL_STEPS

    // Step 1: Tag (unique identifier like username)
    private val _tag = MutableStateFlow("")
    val tag: StateFlow<String> = _tag.asStateFlow()

    private val _tagError = MutableStateFlow<String?>(null)
    val tagError: StateFlow<String?> = _tagError.asStateFlow()

    private val _isCheckingTag = MutableStateFlow(false)
    val isCheckingTag: StateFlow<Boolean> = _isCheckingTag.asStateFlow()

    private val _isTagAvailable = MutableStateFlow<Boolean?>(null)
    val isTagAvailable: StateFlow<Boolean?> = _isTagAvailable.asStateFlow()

    // Step 2: Name
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    // Step 3: Description (optional)
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    // Step 4: Colors
    private val _primaryColor = MutableStateFlow<String?>(null)
    val primaryColor: StateFlow<String?> = _primaryColor.asStateFlow()

    private val _secondaryColor = MutableStateFlow<String?>(null)
    val secondaryColor: StateFlow<String?> = _secondaryColor.asStateFlow()

    // Step 5: Logo (optional)
    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    private val _imageFocusRegion = MutableStateFlow<com.district37.toastmasters.models.FocusRegion?>(null)

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _creationResult = MutableStateFlow<Resource<Organization>?>(null)
    val creationResult: StateFlow<Resource<Organization>?> = _creationResult.asStateFlow()

    // Combined tag validation flow
    private val _isTagValid = combine(
        _tag,
        _tagError,
        _isCheckingTag,
        _isTagAvailable
    ) { tag, tagError, isChecking, isAvailable ->
        tag.trim().length >= 3 && tagError == null && !isChecking && isAvailable == true
    }

    // Derived state for whether user can proceed to next step
    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _isTagValid,
        _name,
        _primaryColor,
        _secondaryColor
    ) { step, isTagValid, name, primaryColor, secondaryColor ->
        when (step) {
            0 -> isTagValid // Step 1: Valid tag required
            1 -> name.trim().length >= 3 // Step 2: Name required (at least 3 chars)
            2 -> true // Step 3: Description is optional
            3 -> primaryColor != null && secondaryColor != null // Step 4: Both colors required
            4 -> true // Step 5: Logo is optional
            else -> false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Set up debounced tag availability checking
        viewModelScope.launch {
            _tag
                .debounce(DEBOUNCE_MILLIS)
                .collect { tag ->
                    if (tag.isNotBlank()) {
                        validateAndCheckTag(tag)
                    } else {
                        _tagError.value = null
                        _isTagAvailable.value = null
                    }
                }
        }
    }

    // Navigation
    fun goToNextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.update { it + 1 }
        }
    }

    fun goToPreviousStep(): Boolean {
        return if (_currentStep.value > 0) {
            _currentStep.update { it - 1 }
            true
        } else {
            false
        }
    }

    // Step 1: Tag
    fun updateTag(value: String) {
        // Convert to lowercase automatically and remove invalid characters
        val lowercased = value.lowercase().filter { it.isLetterOrDigit() || it == '_' }
        _tag.value = lowercased

        // Reset states while user is typing
        _isTagAvailable.value = null
        _tagError.value = null
    }

    private suspend fun validateAndCheckTag(tag: String) {
        // Validate format first
        if (!tag.matches(Regex(TAG_REGEX))) {
            _tagError.value = when {
                tag.length < 3 -> "Tag must be at least 3 characters"
                tag.length > 18 -> "Tag must be at most 18 characters"
                else -> "Tag can only contain lowercase letters, numbers, and underscores"
            }
            _isTagAvailable.value = false
            return
        }

        // Format is valid - tag availability check not yet implemented on server
        // For now, just mark as available if format is correct
        _isTagAvailable.value = true
        _tagError.value = null
    }

    // Step 2: Name
    fun updateName(value: String) {
        _name.value = value
    }

    // Step 3: Description
    fun updateDescription(value: String) {
        _description.value = value
    }

    // Step 4: Colors
    fun updatePrimaryColor(color: String) {
        _primaryColor.value = color
    }

    fun updateSecondaryColor(color: String) {
        _secondaryColor.value = color
    }

    // Step 5: Logo
    fun setImageSelection(result: ImageSelectionResult) {
        _selectedImageBytes.value = result.imageBytes
        _imageFocusRegion.value = result.focusRegion
    }

    fun clearImageSelection() {
        _selectedImageBytes.value = null
        _imageFocusRegion.value = null
    }

    // Submission
    fun createOrganization() {
        if (!canProceed.value) return

        viewModelScope.launch {
            _isSubmitting.update { true }
            _creationResult.update { Resource.Loading }

            val input = CreateOrganizationInput(
                tag = _tag.value.trim(),
                name = _name.value.trim(),
                description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
                primaryColor = Optional.presentIfNotNull(_primaryColor.value),
                secondaryColor = Optional.presentIfNotNull(_secondaryColor.value)
            )

            val result = organizationRepository.createOrganization(input)

            if (result is Resource.Success) {
                Logger.d(TAG, "Organization created successfully: ${result.data.id}")

                // Handle logo image upload
                wizardImageHandler.handleImageUploadAndRecord(
                    imageBytes = _selectedImageBytes.value,
                    focusRegion = _imageFocusRegion.value,
                    entityId = result.data.id,
                    entityType = EntityType.ORGANIZATION,
                    entityTypeString = "organization",
                    altText = _name.value.trim(),
                    tag = TAG
                )
            } else if (result is Resource.Error) {
                Logger.e(TAG, "Failed to create organization: ${result.message}")
            }

            _creationResult.update { result }
            _isSubmitting.update { false }
        }
    }

    fun resetCreationResult() {
        _creationResult.update { null }
    }
}
