package com.district37.toastmasters.features.create.performer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardScaffold
import com.district37.toastmasters.features.common.imageselection.ImageSelectionWizard
import com.district37.toastmasters.features.create.performer.steps.PerformerBioStep
import com.district37.toastmasters.features.create.performer.steps.PerformerNameStep
import com.district37.toastmasters.features.create.performer.steps.PerformerTypeStep
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel

/**
 * Multi-step wizard screen for creating a new performer.
 * Guides the user through collecting performer information step by step.
 *
 * Steps:
 * 1. Name - What's the performer's name?
 * 2. Type - What type of performer?
 * 3. Bio - Tell us about them (optional)
 *
 * @param viewModel The wizard view model
 * @param onPerformerCreated Callback when performer is successfully created with the new performer's ID
 * @param onCancel Callback when user cancels or exits the wizard
 */
@Composable
fun CreatePerformerWizardScreen(
    viewModel: CreatePerformerWizardViewModel = koinViewModel(),
    onPerformerCreated: (performerId: Int) -> Unit,
    onCancel: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val creationResult by viewModel.creationResult.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()

    // Local error state for display
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Handle creation result
    LaunchedEffect(creationResult) {
        when (val result = creationResult) {
            is Resource.Success -> {
                errorMessage = null
                onPerformerCreated(result.data.id)
            }
            is Resource.Error -> {
                errorMessage = result.message ?: "Failed to create performer. Please try again."
            }
            else -> { }
        }
    }

    val isLastStep = currentStep == viewModel.totalSteps - 1
    val nextLabel = if (isLastStep) "Create Performer" else "Continue"

    // Image selection wizard state
    var showImageWizard by remember { mutableStateOf(false) }

    // Full-screen image wizard
    if (showImageWizard) {
        ImageSelectionWizard(
            onComplete = { result ->
                viewModel.setImageSelection(result)
                showImageWizard = false
            },
            onCancel = { showImageWizard = false },
            title = "Performer Photo"
        )
        return
    }

    WizardScaffold(
        currentStep = currentStep,
        totalSteps = viewModel.totalSteps,
        title = "Create Performer",
        onBack = {
            errorMessage = null // Clear error when navigating back
            if (!viewModel.goToPreviousStep()) {
                // At first step, exit wizard
                onCancel()
            }
        },
        onNext = {
            errorMessage = null // Clear error when retrying
            if (isLastStep) {
                viewModel.createPerformer()
            } else {
                viewModel.goToNextStep()
            }
        },
        nextEnabled = canProceed,
        nextLabel = nextLabel,
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        onDismissError = {
            errorMessage = null
            viewModel.resetCreationResult()
        }
    ) {
        val selectedImageBytes by viewModel.selectedImageBytes.collectAsState()

        when (currentStep) {
            0 -> PerformerNameStep(
                viewModel = viewModel,
                onNext = { viewModel.goToNextStep() }
            )
            1 -> PerformerTypeStep(
                viewModel = viewModel
            )
            2 -> PerformerBioStep(
                viewModel = viewModel
            )
            3 -> {
                // Image selection step
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add an Image (Optional)",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    selectedImageBytes?.toImageBitmap()?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Selected performer image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearImageSelection() }) {
                            Text("Remove Photo")
                        }
                    } ?: run {
                        Button(onClick = { showImageWizard = true }) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Photo")
                        }
                    }
                }
            }
        }
    }
}
