package com.district37.toastmasters.features.edit.performer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.EntityPickerField
import com.district37.toastmasters.components.forms.FormMultilineTextField
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.ManageCollaboratorsRoute
import androidx.compose.material3.HorizontalDivider
import com.district37.toastmasters.features.common.EditableImagePreview
import com.district37.toastmasters.features.common.imageselection.ImageSelectionWizard
import com.district37.toastmasters.viewmodel.FormResult
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPerformerScreen(
    performerId: Int,
    onBackClick: () -> Unit,
    onPerformerUpdated: () -> Unit
) {
    val navController = LocalNavController.current
    val viewModel: EditPerformerViewModel = koinViewModel(
        key = "edit_performer_$performerId"
    ) { parametersOf(performerId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val name by viewModel.name.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val performerType by viewModel.performerType.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val selectedImageBytes by viewModel.selectedImageBytes.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()

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
            onPerformerUpdated()
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to update performer") },
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
                    text = loadError ?: "Failed to load performer",
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
            title = "Performer Photo"
        )
        return
    }

    EntityFormScaffold(
        title = "Edit Performer",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting || isUploadingImage,
        submitEnabled = name.isNotBlank(),
        submitLabel = if (isUploadingImage) "Uploading..." else "Save"
    ) {
        // Performer Photo Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Performer Photo",
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
            label = "Performer Name",
            required = true,
            error = fieldErrors["name"],
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

        FormTextField(
            value = performerType,
            onValueChange = { viewModel.updatePerformerType(it) },
            label = "Performer Type",
            placeholder = "e.g., Band, DJ, Speaker, etc.",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

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
                        entityType = "performer",
                        entityId = performerId,
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
