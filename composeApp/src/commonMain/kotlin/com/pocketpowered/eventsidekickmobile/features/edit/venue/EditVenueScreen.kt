package com.district37.toastmasters.features.edit.venue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.features.common.EditableImagePreview
import com.district37.toastmasters.features.common.imageselection.ImageSelectionWizard
import com.district37.toastmasters.navigation.EditLocationRoute
import com.district37.toastmasters.navigation.ManageCollaboratorsRoute
import com.district37.toastmasters.components.forms.EntityPickerField
import com.district37.toastmasters.viewmodel.FormResult
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVenueScreen(
    venueId: Int,
    onBackClick: () -> Unit,
    onVenueUpdated: () -> Unit
) {
    val navController = LocalNavController.current
    val viewModel: EditVenueViewModel = koinViewModel(
        key = "edit_venue_$venueId"
    ) { parametersOf(venueId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val name by viewModel.name.collectAsState()
    val address by viewModel.address.collectAsState()
    val city by viewModel.city.collectAsState()
    val state by viewModel.state.collectAsState()
    val zipCode by viewModel.zipCode.collectAsState()
    val capacity by viewModel.capacity.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val selectedImageBytes by viewModel.selectedImageBytes.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()
    val venueLocations by viewModel.venueLocations.collectAsState()
    val isLoadingLocations by viewModel.isLoadingLocations.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    // Image selection wizard state
    var showImageWizard by remember { mutableStateOf(false) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Sync bitmap with ViewModel state
    LaunchedEffect(selectedImageBytes) {
        selectedImageBitmap = selectedImageBytes?.toImageBitmap()
    }

    // Handle successful update
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            onVenueUpdated()
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to update venue") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResult() }) {
                    Text("OK")
                }
            }
        )
    }

    // Loading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    if (loadError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = loadError ?: "Failed to load venue",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Full screen image selection wizard
    if (showImageWizard) {
        ImageSelectionWizard(
            onComplete = { result ->
                viewModel.setImageSelection(result)
                selectedImageBitmap = result.imageBitmap
                showImageWizard = false
            },
            onCancel = { showImageWizard = false },
            title = "Venue Photo"
        )
        return
    }

    EntityFormScaffold(
        title = "Edit Venue",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting || isUploadingImage,
        submitEnabled = name.isNotBlank(),
        submitLabel = if (isUploadingImage) "Uploading..." else "Save"
    ) {
        // Venue Photo Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Venue Photo",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            EditableImagePreview(
                selectedImageBitmap = selectedImageBitmap,
                onClick = { showImageWizard = true },
                enabled = !isSubmitting && !isUploadingImage
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showImageWizard = true },
                enabled = !isSubmitting && !isUploadingImage
            ) {
                Text(if (imageUrl.isNotBlank() || selectedImageBitmap != null) "Change Photo" else "Add Photo")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = city,
            onValueChange = { viewModel.updateCity(it) },
            label = "City",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = state,
            onValueChange = { viewModel.updateState(it) },
            label = "State",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = zipCode,
            onValueChange = { viewModel.updateZipCode(it) },
            label = "Zip Code",
            enabled = !isSubmitting,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = capacity,
            onValueChange = { viewModel.updateCapacity(it) },
            label = "Capacity",
            placeholder = "Maximum number of attendees",
            error = fieldErrors["capacity"],
            enabled = !isSubmitting,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        // Locations Section
        if (venueLocations.isNotEmpty() || isLoadingLocations) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingLocations) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                venueLocations.forEach { location ->
                    LocationListItem(
                        name = location.name,
                        locationType = location.locationType,
                        onClick = {
                            navController.navigate(EditLocationRoute(location.id))
                        }
                    )
                }
            }
        }

        // Collaborators section
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        EntityPickerField(
            selectedName = "Manage collaborators",
            label = "Collaborators",
            placeholder = "Manage collaborators",
            onClick = {
                navController.navigate(
                    ManageCollaboratorsRoute(
                        entityType = "venue",
                        entityId = venueId,
                        entityName = name
                    )
                )
            },
            onClear = null,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LocationListItem(
    name: String,
    locationType: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (!locationType.isNullOrBlank()) {
                        Text(
                            text = locationType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Edit location",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
