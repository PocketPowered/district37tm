package com.district37.toastmasters.features.create.organization.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardViewModel

/**
 * Step 3: Organization Description
 * Collects an optional description for the organization.
 */
@Composable
fun OrganizationDescriptionStep(
    viewModel: CreateOrganizationWizardViewModel,
    modifier: Modifier = Modifier
) {
    val description by viewModel.description.collectAsState()

    WizardStepContainer(
        prompt = "Tell us about your organization",
        subtitle = "This is optional but helps users understand what you do",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            placeholder = { Text("Describe your organization...") },
            minLines = 3,
            maxLines = 6,
            supportingText = {
                Text("Optional")
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
