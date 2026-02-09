package com.district37.toastmasters.features.create.organization.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardViewModel

/**
 * Step 2: Organization Name
 * Collects the organization's display name.
 */
@Composable
fun OrganizationNameStep(
    viewModel: CreateOrganizationWizardViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()

    WizardStepContainer(
        prompt = "What's your organization called?",
        subtitle = "This is the display name that users will see",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            placeholder = { Text("Organization Name") },
            singleLine = true,
            supportingText = {
                Text("At least 3 characters")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (name.trim().length >= 3) {
                        onNext()
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
