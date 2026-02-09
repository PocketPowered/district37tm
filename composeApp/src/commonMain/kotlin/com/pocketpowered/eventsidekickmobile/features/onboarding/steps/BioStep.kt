package com.district37.toastmasters.features.onboarding.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.onboarding.OnboardingWizardViewModel

/**
 * Step 3: Bio (Optional)
 * Collects an optional bio from the user
 */
@Composable
fun BioStep(
    viewModel: OnboardingWizardViewModel,
    modifier: Modifier = Modifier
) {
    val bio by viewModel.bio.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "Tell us about yourself",
        subtitle = "Write a short bio (optional)",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = bio,
            onValueChange = { viewModel.updateBio(it) },
            placeholder = { Text("About you...") },
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
