package com.district37.toastmasters.features.create.organization

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
import com.district37.toastmasters.features.create.organization.steps.OrganizationColorSchemeStep
import com.district37.toastmasters.features.create.organization.steps.OrganizationDescriptionStep
import com.district37.toastmasters.features.create.organization.steps.OrganizationNameStep
import com.district37.toastmasters.features.create.organization.steps.OrganizationTagStep
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel

/**
 * Multi-step wizard screen for creating a new organization.
 * Guides the user through collecting organization information step by step.
 *
 * Steps:
 * 1. Tag - Unique identifier (like username)
 * 2. Name - Organization display name
 * 3. Description - Optional description
 * 4. Colors - Primary and secondary brand colors
 * 5. Logo - Optional logo image
 *
 * @param viewModel The wizard view model
 * @param onOrganizationCreated Callback when organization is successfully created with the new organization's ID
 * @param onCancel Callback when user cancels or exits the wizard
 */
@Composable
fun CreateOrganizationWizardScreen(
    viewModel: CreateOrganizationWizardViewModel = koinViewModel(),
    onOrganizationCreated: (organizationId: Int) -> Unit,
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
                onOrganizationCreated(result.data.id)
            }
            is Resource.Error -> {
                errorMessage = result.message ?: "Failed to create organization. Please try again."
            }
            else -> { }
        }
    }

    val isLastStep = currentStep == viewModel.totalSteps - 1
    val nextLabel = if (isLastStep) "Create Organization" else "Continue"

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
            title = "Organization Logo"
        )
        return
    }

    WizardScaffold(
        currentStep = currentStep,
        totalSteps = viewModel.totalSteps,
        title = "Create Organization",
        onBack = {
            errorMessage = null
            if (!viewModel.goToPreviousStep()) {
                onCancel()
            }
        },
        onNext = {
            errorMessage = null
            if (isLastStep) {
                viewModel.createOrganization()
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
            0 -> OrganizationTagStep(viewModel = viewModel)
            1 -> OrganizationNameStep(
                viewModel = viewModel,
                onNext = { viewModel.goToNextStep() }
            )
            2 -> OrganizationDescriptionStep(viewModel = viewModel)
            3 -> OrganizationColorSchemeStep(viewModel = viewModel)
            4 -> {
                // Logo selection step
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add a Logo (Optional)",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Upload your organization's logo to make it stand out",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    selectedImageBytes?.toImageBitmap()?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Selected organization logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearImageSelection() }) {
                            Text("Remove Logo")
                        }
                    } ?: run {
                        Button(onClick = { showImageWizard = true }) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Logo")
                        }
                    }
                }
            }
        }
    }
}
