package com.district37.toastmasters.features.create.venue.steps

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
import androidx.compose.ui.text.input.KeyboardType
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.venue.CreateVenueWizardViewModel

/**
 * Step 3: Venue Capacity
 * Collects the optional capacity of the venue.
 */
@Composable
fun VenueCapacityStep(
    viewModel: CreateVenueWizardViewModel,
    modifier: Modifier = Modifier
) {
    val capacity by viewModel.capacity.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "What's the capacity?",
        subtitle = "How many people can this venue hold? (optional)",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = capacity,
            onValueChange = { viewModel.updateCapacity(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Maximum attendees") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )
    }
}
