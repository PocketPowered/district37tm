package com.district37.toastmasters.features.create.performer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.FormMultilineTextField
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.viewmodel.FormResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePerformerScreen(
    onBackClick: () -> Unit,
    onPerformerCreated: (Int) -> Unit,
    viewModel: CreatePerformerViewModel = koinViewModel()
) {
    val name by viewModel.name.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val performerType by viewModel.performerType.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    // Handle successful creation
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            val performer = (formResult as FormResult.Success).data
            onPerformerCreated(performer.id)
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to create performer") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResult() }) {
                    Text("OK")
                }
            }
        )
    }

    EntityFormScaffold(
        title = "Create Performer",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting,
        submitEnabled = name.isNotBlank()
    ) {
        FormTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = "Performer Name",
            required = true,
            error = fieldErrors["name"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = performerType,
            onValueChange = { viewModel.updatePerformerType(it) },
            label = "Performer Type",
            placeholder = "e.g., Speaker, Artist, Band",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormMultilineTextField(
            value = bio,
            onValueChange = { viewModel.updateBio(it) },
            label = "Bio",
            placeholder = "Tell us about this performer...",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
