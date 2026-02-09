package com.district37.toastmasters.features.common.imageselection

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.ImageSelectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Steps in the image selection wizard.
 */
enum class ImageSelectionStep {
    SOURCE,   // Step 1: Choose image source (Gallery, Camera, URL)
    FOCUS,    // Step 2: Set focus region
    CAPTION   // Step 3: Add optional caption
}

/**
 * Represents the image source type.
 */
enum class ImageSourceType {
    DEVICE  // Gallery selection (has bytes)
}

/**
 * ViewModel for the Image Selection Wizard.
 * Manages the 3-step flow for selecting and configuring an image.
 *
 * Step 1: Source Selection - Choose image via Gallery
 * Step 2: Focus Region - Select focus region for images
 * Step 3: Caption - Add optional caption
 *
 * This ViewModel does NOT handle image upload - the caller receives the
 * [ImageSelectionResult] and handles upload as needed.
 */
class ImageSelectionWizardViewModel : ViewModel() {

    companion object {
        const val TOTAL_STEPS = 3
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(ImageSelectionStep.SOURCE)
    val currentStep: StateFlow<ImageSelectionStep> = _currentStep.asStateFlow()

    val totalSteps = TOTAL_STEPS

    // Image data - device selection
    private val _imageBytes = MutableStateFlow<ByteArray?>(null)
    val imageBytes: StateFlow<ByteArray?> = _imageBytes.asStateFlow()

    private val _imageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val imageBitmap: StateFlow<ImageBitmap?> = _imageBitmap.asStateFlow()

    // Focus region
    private val _focusRegion = MutableStateFlow<FocusRegion?>(null)
    val focusRegion: StateFlow<FocusRegion?> = _focusRegion.asStateFlow()

    // Caption (optional)
    private val _caption = MutableStateFlow("")
    val caption: StateFlow<String> = _caption.asStateFlow()

    /**
     * Returns the current image source type, or null if no image selected.
     */
    val imageSourceType: StateFlow<ImageSourceType?> = _imageBytes.map { bytes ->
        if (bytes != null) ImageSourceType.DEVICE else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Whether an image has been selected.
     */
    val hasImage: StateFlow<Boolean> = _imageBytes.map { bytes ->
        bytes != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Whether the user can proceed to the next step.
     */
    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _imageBytes
    ) { step, bytes ->
        when (step) {
            ImageSelectionStep.SOURCE -> bytes != null
            ImageSelectionStep.FOCUS -> true // Focus region has a default, always can proceed
            ImageSelectionStep.CAPTION -> true // Caption is optional
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Current step index (0-based) for the stepper UI.
     */
    val currentStepIndex: StateFlow<Int> = _currentStep.map { step ->
        when (step) {
            ImageSelectionStep.SOURCE -> 0
            ImageSelectionStep.FOCUS -> 1
            ImageSelectionStep.CAPTION -> 2
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // ==================== Navigation ====================

    /**
     * Advance to the next step.
     */
    fun goToNextStep() {
        _currentStep.update { step ->
            when (step) {
                ImageSelectionStep.SOURCE -> ImageSelectionStep.FOCUS
                ImageSelectionStep.FOCUS -> ImageSelectionStep.CAPTION
                ImageSelectionStep.CAPTION -> step // Already at last step
            }
        }
    }

    /**
     * Go back to the previous step.
     * @return false if already at first step (caller should handle exit)
     */
    fun goToPreviousStep(): Boolean {
        return when (_currentStep.value) {
            ImageSelectionStep.SOURCE -> false // Already at first step
            ImageSelectionStep.FOCUS -> {
                _currentStep.update { ImageSelectionStep.SOURCE }
                true
            }
            ImageSelectionStep.CAPTION -> {
                _currentStep.update { ImageSelectionStep.FOCUS }
                true
            }
        }
    }

    // ==================== Source Step Actions ====================

    /**
     * Handle image selected from gallery.
     */
    fun onImageSelected(bytes: ByteArray, bitmap: ImageBitmap?) {
        _imageBytes.update { bytes }
        _imageBitmap.update { bitmap }
        // Reset focus region for new image
        _focusRegion.update { null }
    }

    /**
     * Clear the current image selection.
     */
    fun clearImage() {
        _imageBytes.update { null }
        _imageBitmap.update { null }
        _focusRegion.update { null }
    }

    // ==================== Focus + Caption Step Actions ====================

    /**
     * Set the focus region for the image.
     */
    fun setFocusRegion(region: FocusRegion?) {
        _focusRegion.update { region }
    }

    /**
     * Update the caption.
     */
    fun updateCaption(value: String) {
        _caption.update { value }
    }

    // ==================== Result ====================

    /**
     * Build the final result from current state.
     */
    fun buildResult(): ImageSelectionResult? {
        val bytes = _imageBytes.value ?: return null
        val bitmap = _imageBitmap.value
        val region = _focusRegion.value
        val captionText = _caption.value.trim().takeIf { it.isNotBlank() }

        return ImageSelectionResult(
            imageBytes = bytes,
            imageBitmap = bitmap,
            focusRegion = region,
            caption = captionText
        )
    }

    /**
     * Reset the wizard to initial state.
     */
    fun reset() {
        _currentStep.update { ImageSelectionStep.SOURCE }
        _imageBytes.update { null }
        _imageBitmap.update { null }
        _focusRegion.update { null }
        _caption.update { "" }
    }
}
