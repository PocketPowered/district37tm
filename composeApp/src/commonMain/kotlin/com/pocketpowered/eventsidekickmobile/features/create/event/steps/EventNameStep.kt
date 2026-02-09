package com.district37.toastmasters.features.create.event.steps

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
import com.district37.toastmasters.features.create.event.CreateEventWizardViewModel

/**
 * Step 1: Event Name
 * Collects the name of the event with a conversational prompt.
 */
@Composable
fun EventNameStep(
    viewModel: CreateEventWizardViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventName by viewModel.eventName.collectAsState()

    WizardStepContainer(
        prompt = "What's your event called?",
        subtitle = "Give your event a memorable name",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = eventName,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Event name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (eventName.isNotBlank()) {
                        onNext()
                    }
                }
            )
        )
    }
}
