package com.district37.toastmasters.features.create.performer.steps

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
import com.district37.toastmasters.features.create.performer.CreatePerformerWizardViewModel

/**
 * Step 3: Performer Bio
 * Collects an optional biography for the performer.
 */
@Composable
fun PerformerBioStep(
    viewModel: CreatePerformerWizardViewModel,
    modifier: Modifier = Modifier
) {
    val bio by viewModel.bio.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "Tell us about them",
        subtitle = "Add a short bio (optional)",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = bio,
            onValueChange = { viewModel.updateBio(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = { Text("Share their background, achievements, or what makes them special...") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            maxLines = 6
        )
    }
}
