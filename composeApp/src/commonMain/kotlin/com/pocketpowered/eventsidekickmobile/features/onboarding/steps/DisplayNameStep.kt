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
 * Step 1: Display Name
 * Collects the user's display name
 */
@Composable
fun DisplayNameStep(
    viewModel: OnboardingWizardViewModel,
    modifier: Modifier = Modifier
) {
    val displayName by viewModel.displayName.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "What should we call you?",
        subtitle = "Enter your display name",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = { viewModel.updateDisplayName(it) },
            placeholder = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
