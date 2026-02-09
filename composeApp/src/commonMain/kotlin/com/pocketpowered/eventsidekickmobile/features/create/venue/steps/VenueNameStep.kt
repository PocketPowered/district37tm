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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.venue.CreateVenueWizardViewModel

/**
 * Step 1: Venue Name
 * Collects the name of the venue with a conversational prompt.
 */
@Composable
fun VenueNameStep(
    viewModel: CreateVenueWizardViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val venueName by viewModel.venueName.collectAsState()

    WizardStepContainer(
        prompt = "What's the venue called?",
        subtitle = "Give the venue a recognizable name",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = venueName,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Venue name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (venueName.trim().length >= 3) {
                        onNext()
                    }
                }
            )
        )
    }
}
