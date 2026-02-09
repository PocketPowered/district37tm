package com.district37.toastmasters.features.common.imageselection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.district37.toastmasters.components.wizard.WizardScaffold
import com.district37.toastmasters.models.ImageSelectionResult

/**
 * Full-screen wizard for selecting and configuring an image.
 *
 * This is a unified component that handles the complete image selection flow:
 * 1. **Source Selection** - Choose image from Gallery
 * 2. **Focus Region** - Set focus region for images
 * 3. **Caption** - Add optional caption (can be skipped)
 *
 * The wizard returns an [ImageSelectionResult] containing all necessary metadata
 * for the caller to use (upload, display, etc.).
 *
 * ## Usage
 * ```kotlin
 * var showImageWizard by remember { mutableStateOf(false) }
 *
 * if (showImageWizard) {
 *     ImageSelectionWizard(
 *         onComplete = { result ->
 *             viewModel.setImageSelection(result)
 *             showImageWizard = false
 *         },
 *         onCancel = { showImageWizard = false }
 *     )
 * }
 * ```
 *
 * @param onComplete Called when the user completes the wizard with their selection
 * @param onCancel Called when the user cancels or exits the wizard
 * @param title Title shown in the wizard header (defaults to "Add Image")
 */
@Composable
fun ImageSelectionWizard(
    onComplete: (ImageSelectionResult) -> Unit,
    onCancel: () -> Unit,
    title: String = "Add Image"
) {
    val viewModel: ImageSelectionWizardViewModel = viewModel { ImageSelectionWizardViewModel() }

    val currentStep by viewModel.currentStep.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()
    val hasImage by viewModel.hasImage.collectAsState()

    // Image state
    val imageBytes by viewModel.imageBytes.collectAsState()
    val imageBitmap by viewModel.imageBitmap.collectAsState()
    val focusRegion by viewModel.focusRegion.collectAsState()
    val caption by viewModel.caption.collectAsState()
    val imageSourceType by viewModel.imageSourceType.collectAsState()

    val isLastStep = currentStep == ImageSelectionStep.CAPTION
    val nextLabel = if (isLastStep) "Done" else "Continue"

    WizardScaffold(
        currentStep = currentStepIndex,
        totalSteps = viewModel.totalSteps,
        title = title,
        onBack = {
            if (!viewModel.goToPreviousStep()) {
                onCancel()
            }
        },
        onNext = {
            if (isLastStep) {
                viewModel.buildResult()?.let { onComplete(it) }
            } else {
                viewModel.goToNextStep()
            }
        },
        nextEnabled = canProceed,
        nextLabel = nextLabel,
        isSubmitting = false,
        showSkipButton = currentStep == ImageSelectionStep.CAPTION,
        skipLabel = "Skip",
        onSkip = {
            // Skip caption and complete wizard
            viewModel.buildResult()?.let { onComplete(it) }
        },
        errorMessage = null,
        onDismissError = { }
    ) {
        when (currentStep) {
            ImageSelectionStep.SOURCE -> {
                ImageSourceStep(
                    imageBitmap = imageBitmap,
                    onImageSelected = { bytes, bitmap ->
                        viewModel.onImageSelected(bytes, bitmap)
                    },
                    onClearImage = {
                        viewModel.clearImage()
                    }
                )
            }

            ImageSelectionStep.FOCUS -> {
                ImageFocusStep(
                    imageBitmap = imageBitmap,
                    focusRegion = focusRegion,
                    onFocusRegionChanged = { region ->
                        viewModel.setFocusRegion(region)
                    }
                )
            }

            ImageSelectionStep.CAPTION -> {
                ImageCaptionStep(
                    imageBitmap = imageBitmap,
                    focusRegion = focusRegion,
                    caption = caption,
                    onCaptionChanged = { value ->
                        viewModel.updateCaption(value)
                    }
                )
            }
        }
    }
}
