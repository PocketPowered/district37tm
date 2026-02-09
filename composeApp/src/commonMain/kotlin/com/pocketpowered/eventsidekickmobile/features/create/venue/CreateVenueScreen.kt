package com.district37.toastmasters.features.create.venue

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.viewmodel.FormResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateVenueScreen(
    onBackClick: () -> Unit,
    onVenueCreated: (Int) -> Unit,
    viewModel: CreateVenueViewModel = koinViewModel()
) {
    val name by viewModel.name.collectAsState()
    val address by viewModel.address.collectAsState()
    val city by viewModel.city.collectAsState()
    val state by viewModel.state.collectAsState()
    val zipCode by viewModel.zipCode.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    // Handle successful creation
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            val venue = (formResult as FormResult.Success).data
            onVenueCreated(venue.id)
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to create venue") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResult() }) {
                    Text("OK")
                }
            }
        )
    }

    val requiredFieldsFilled = name.isNotBlank() &&
        address.isNotBlank() &&
        city.isNotBlank() &&
        state.isNotBlank() &&
        zipCode.isNotBlank()

    EntityFormScaffold(
        title = "Create Venue",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting,
        submitEnabled = requiredFieldsFilled
    ) {
        FormTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = "Venue Name",
            required = true,
            error = fieldErrors["name"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = address,
            onValueChange = { viewModel.updateAddress(it) },
            label = "Address",
            placeholder = "Street address",
            required = true,
            error = fieldErrors["address"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = city,
            onValueChange = { viewModel.updateCity(it) },
            label = "City",
            required = true,
            error = fieldErrors["city"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = state,
            onValueChange = { viewModel.updateState(it) },
            label = "State",
            required = true,
            error = fieldErrors["state"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = zipCode,
            onValueChange = { viewModel.updateZipCode(it) },
            label = "Zip Code",
            required = true,
            error = fieldErrors["zipCode"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = capacity,
            onValueChange = { viewModel.updateCapacity(it) },
            label = "Capacity",
            placeholder = "Maximum number of attendees",
            keyboardType = KeyboardType.Number,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
