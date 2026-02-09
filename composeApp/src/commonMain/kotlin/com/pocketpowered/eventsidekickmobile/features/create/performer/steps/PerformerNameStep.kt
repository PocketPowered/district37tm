package com.district37.toastmasters.features.create.performer.steps

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
import com.district37.toastmasters.features.create.performer.CreatePerformerWizardViewModel

/**
 * Step 1: Performer Name
 * Collects the name of the performer with a conversational prompt.
 */
@Composable
fun PerformerNameStep(
    viewModel: CreatePerformerWizardViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performerName by viewModel.performerName.collectAsState()

    WizardStepContainer(
        prompt = "What's the performer's name?",
        subtitle = "Enter the name they go by",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = performerName,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Performer name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (performerName.trim().length >= 3) {
                        onNext()
                    }
                }
            )
        )
    }
}
