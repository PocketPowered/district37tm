package com.district37.toastmasters.features.edit.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.district37.toastmasters.components.common.BrandingColorRow
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.EntityPickerField
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.features.common.EditableImagePreview
import com.district37.toastmasters.features.common.imageselection.ImageSelectionWizard
import com.district37.toastmasters.navigation.ManageOrganizationMembersRoute
import com.district37.toastmasters.viewmodel.FormResult
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrganizationScreen(
    organizationId: Int,
    onBackClick: () -> Unit,
    onOrganizationUpdated: () -> Unit,
    onOrganizationDeleted: () -> Unit
) {
    val navController = LocalNavController.current
    val viewModel: EditOrganizationViewModel = koinViewModel(
        key = "edit_organization_$organizationId"
    ) { parametersOf(organizationId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val name by viewModel.name.collectAsState()
    val tag by viewModel.tag_.collectAsState()
    val description by viewModel.description.collectAsState()
    val website by viewModel.website.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val primaryColor by viewModel.primaryColor.collectAsState()
    val secondaryColor by viewModel.secondaryColor.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val selectedImageBytes by viewModel.selectedImageBytes.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val canDelete by viewModel.canDelete.collectAsState()
    val myRole by viewModel.myRole.collectAsState()

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
            onOrganizationUpdated()
        }
    }

    // Handle successful delete
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onOrganizationDeleted()
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to update organization") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResult() }) {
                    Text("OK")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Organization") },
            text = { Text("Are you sure you want to delete this organization? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
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
                    text = loadError ?: "Failed to load organization",
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
            title = "Organization Logo"
        )
        return
    }

    EntityFormScaffold(
        title = "Edit Organization",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting || isUploadingImage || isDeleting,
        submitEnabled = name.isNotBlank() && tag.isNotBlank(),
        submitLabel = when {
            isUploadingImage -> "Uploading..."
            isDeleting -> "Deleting..."
            else -> "Save"
        }
    ) {
        // Logo Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Organization Logo",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            EditableImagePreview(
                selectedImageBitmap = selectedImageBitmap,
                onClick = { showImageWizard = true },
                enabled = !isSubmitting && !isUploadingImage && !isDeleting
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showImageWizard = true },
                enabled = !isSubmitting && !isUploadingImage && !isDeleting
            ) {
                Text(if (imageUrl.isNotBlank() || selectedImageBitmap != null) "Change Logo" else "Add Logo")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Basic Info Section
        FormTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = "Organization Name",
            required = true,
            error = fieldErrors["name"],
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = tag,
            onValueChange = { viewModel.updateTag(it) },
            label = "Tag",
            required = true,
            placeholder = "3-18 characters (letters, numbers, underscores)",
            error = fieldErrors["tag"],
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            label = "Description",
            placeholder = "Describe your organization",
            singleLine = false,
            maxLines = 5,
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Contact Info Section
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormTextField(
            value = website,
            onValueChange = { viewModel.updateWebsite(it) },
            label = "Website",
            placeholder = "https://example.com",
            keyboardType = KeyboardType.Uri,
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = "Email",
            placeholder = "contact@example.com",
            keyboardType = KeyboardType.Email,
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = phone,
            onValueChange = { viewModel.updatePhone(it) },
            label = "Phone",
            placeholder = "+1 (555) 123-4567",
            keyboardType = KeyboardType.Phone,
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Branding Section
        Text(
            text = "Branding",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        BrandingColorRow(
            primaryColor = primaryColor,
            onPrimaryColorChange = { viewModel.updatePrimaryColor(it) },
            secondaryColor = secondaryColor,
            onSecondaryColorChange = { viewModel.updateSecondaryColor(it) },
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        // Members Section
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        EntityPickerField(
            selectedName = "Manage members",
            label = "Organization Members",
            placeholder = "Manage members",
            onClick = {
                navController.navigate(
                    ManageOrganizationMembersRoute(
                        organizationId = organizationId,
                        organizationName = name
                    )
                )
            },
            onClear = null,
            enabled = !isSubmitting && !isDeleting,
            modifier = Modifier.fillMaxWidth()
        )

        // Delete Section (only for owners)
        if (canDelete) {
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.requestDelete() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !isSubmitting && !isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isDeleting) "Deleting..." else "Delete Organization")
            }
        }
    }
}
