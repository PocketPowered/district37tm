package com.district37.toastmasters.features.create.event.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.event.CreateEventWizardViewModel

/**
 * Step 2: Event Description
 * Collects an optional description of the event.
 */
@Composable
fun EventDescriptionStep(
    viewModel: CreateEventWizardViewModel,
    modifier: Modifier = Modifier
) {
    val description by viewModel.description.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "Tell us about it",
        subtitle = "Write a short description to help attendees know what to expect (optional)",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("Describe your event...") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            maxLines = 8
        )
    }
}
