package com.district37.toastmasters.features.edit.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.components.common.ArchivedBanner
import com.district37.toastmasters.components.common.ArchiveEventConfirmationDialog
import com.district37.toastmasters.components.common.ChangeVenueConfirmationDialog
import com.district37.toastmasters.components.common.DeleteEventConfirmationDialog
import com.district37.toastmasters.components.forms.DateTimePickerField
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.EntityPickerField
import com.district37.toastmasters.components.forms.FormDropdown
import com.district37.toastmasters.components.forms.FormMultilineTextField
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.features.common.EditableImagePreview
import com.district37.toastmasters.features.common.imageselection.ImageSelectionWizard
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.navigation.VenuePickerRoute
import com.district37.toastmasters.navigation.ManageCollaboratorsRoute
import com.district37.toastmasters.viewmodel.FormResult
import com.district37.toastmasters.util.toImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: Int,
    onBackClick: () -> Unit,
    onEventUpdated: () -> Unit,
    onEventDeleted: () -> Unit
) {
    val navController = LocalNavController.current
    val viewModel: EditEventViewModel = koinViewModel(
        key = "edit_event_$eventId"
    ) { parametersOf(eventId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val eventType by viewModel.eventType.collectAsState()
    val selectedVenueName by viewModel.selectedVenueName.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val existingImages by viewModel.existingImages.collectAsState()
    val pendingImages by viewModel.pendingImages.collectAsState()
    val isUploadingImages by viewModel.isUploadingImages.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    // Delete state
    val canDelete by viewModel.canDelete.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Archive state
    val isArchived by viewModel.isArchived.collectAsState()
    val archivedAt by viewModel.archivedAt.collectAsState()
    val isArchiving by viewModel.isArchiving.collectAsState()
    val archiveSuccess by viewModel.archiveSuccess.collectAsState()
    val showArchiveDialog by viewModel.showArchiveDialog.collectAsState()

    // Image selection wizard state
    var showImageWizard by remember { mutableStateOf(false) }

    // Photo editing state
    var selectedImageForEdit by remember { mutableStateOf<com.district37.toastmasters.models.Image?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    // State for venue change confirmation dialog
    var showVenueChangeConfirmation by remember { mutableStateOf(false) }
    var pendingVenueId by remember { mutableStateOf<Int?>(null) }
    var pendingVenueName by remember { mutableStateOf<String?>(null) }

    // Listen for selected venue from picker
    val selectedVenueIdFromPicker = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Int?>("selected_venue_id", null)
        ?.collectAsState()

    val selectedVenueNameFromPicker = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("selected_venue_name", null)
        ?.collectAsState()

    // Handle venue selection from picker (show confirmation dialog)
    LaunchedEffect(selectedVenueIdFromPicker?.value, selectedVenueNameFromPicker?.value) {
        val id = selectedVenueIdFromPicker?.value
        val name = selectedVenueNameFromPicker?.value
        if (id != null && name != null) {
            // Store pending venue and show confirmation dialog
            pendingVenueId = id
            pendingVenueName = name
            showVenueChangeConfirmation = true
            // Clear the saved state
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("selected_venue_id")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_venue_name")
        }
    }

    // Listen for venue created from nested flow
    val createdVenueId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Int?>("created_venue_id", null)
        ?.collectAsState()

    LaunchedEffect(createdVenueId?.value) {
        createdVenueId?.value?.let { id ->
            // Load venue info and show confirmation dialog
            pendingVenueId = id
            // We'll set the name after loading
            showVenueChangeConfirmation = true
            viewModel.loadAndSelectVenue(id)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("created_venue_id")
        }
    }

    // Venue change confirmation dialog
    if (showVenueChangeConfirmation) {
        ChangeVenueConfirmationDialog(
            onConfirm = {
                // Apply the venue change
                val id = pendingVenueId
                val name = pendingVenueName
                if (id != null && name != null) {
                    viewModel.selectVenue(id, name)
                }
                showVenueChangeConfirmation = false
                pendingVenueId = null
                pendingVenueName = null
            },
            onDismiss = {
                // Cancel the venue change
                showVenueChangeConfirmation = false
                pendingVenueId = null
                pendingVenueName = null
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteEventConfirmationDialog(
            eventName = name,
            isDeleting = isDeleting,
            onConfirm = { viewModel.deleteEvent() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Archive confirmation dialog
    if (showArchiveDialog) {
        ArchiveEventConfirmationDialog(
            eventName = name,
            isArchiving = isArchiving,
            onConfirm = { viewModel.confirmArchive() },
            onDismiss = { viewModel.cancelArchive() }
        )
    }

    // Navigate back after successful deletion
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onEventDeleted()
        }
    }

    // Navigate back after successful archiving
    LaunchedEffect(archiveSuccess) {
        if (archiveSuccess) {
            onEventDeleted() // Reuse the same callback - event is no longer editable
        }
    }

    // Handle successful update
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            onEventUpdated()
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to update event") },
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
                    text = loadError ?: "Failed to load event",
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
                viewModel.addPendingImage(result)
                showImageWizard = false
            },
            onCancel = { showImageWizard = false },
            title = "Add Event Photo"
        )
        return
    }

    EntityFormScaffold(
        title = "Edit Event",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting || isUploadingImages,
        submitEnabled = !isArchived && name.isNotBlank() && selectedVenueName != null && startDate != null && endDate != null,
        submitLabel = if (isUploadingImages) "Uploading..." else "Save"
    ) {
        // Archived banner
        if (isArchived && archivedAt != null) {
            ArchivedBanner(
                entityType = "event",
                archivedAt = archivedAt!!,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Event Photos Section
        com.district37.toastmasters.features.common.PhotoManagerComponent(
            images = existingImages,
            pendingImages = pendingImages,
            onAddPhoto = {
                val totalPhotos = existingImages.size + pendingImages.size
                if (totalPhotos < 5) {
                    showImageWizard = true
                }
            },
            onEditPhoto = { image ->
                selectedImageForEdit = image
                showEditSheet = true
            },
            onDeletePendingPhoto = { imageId ->
                viewModel.deletePendingImage(imageId)
            },
            onReorderPhotos = { reorderedList ->
                viewModel.reorderImages(reorderedList)
            },
            isEnabled = !isArchived && !isSubmitting && !isUploadingImages,
            maxPhotos = 5,
            modifier = Modifier.fillMaxWidth()
        )

        // Photo edit bottom sheet
        if (showEditSheet && selectedImageForEdit != null) {
            com.district37.toastmasters.features.common.PhotoEditBottomSheet(
                image = selectedImageForEdit!!,
                onDelete = {
                    viewModel.deleteExistingImage(selectedImageForEdit!!.id)
                    showEditSheet = false
                    selectedImageForEdit = null
                },
                onDismiss = {
                    showEditSheet = false
                    selectedImageForEdit = null
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FormTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = "Event Name",
            required = true,
            error = fieldErrors["name"],
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormMultilineTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            label = "Description",
            placeholder = "Describe your event...",
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormDropdown(
            selectedOption = eventType,
            options = viewModel.eventTypes,
            onOptionSelected = { viewModel.updateEventType(it) },
            label = "Event Type",
            optionLabel = { formatEventType(it) },
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        EntityPickerField(
            selectedName = selectedVenueName,
            label = "Venue",
            placeholder = "Select a venue",
            required = true,
            error = fieldErrors["venue"],
            onClick = { navController.navigate(VenuePickerRoute()) },
            onClear = null, // Don't allow clearing venue
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        DateTimePickerField(
            label = "Start Date",
            selectedDateTime = startDate,
            onDateTimeSelected = { viewModel.updateStartDate(it) },
            required = true,
            error = fieldErrors["startDate"],
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        DateTimePickerField(
            label = "End Date",
            selectedDateTime = endDate,
            onDateTimeSelected = { viewModel.updateEndDate(it) },
            required = true,
            error = fieldErrors["endDate"],
            enabled = !isArchived && !isSubmitting,
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
                        entityType = "event",
                        entityId = eventId,
                        entityName = name
                    )
                )
            },
            onClear = null,
            enabled = !isArchived && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        // Archive section - only show if user can delete and event is not already archived
        if (canDelete && !isArchived) {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = { viewModel.requestArchive() },
                enabled = !isSubmitting && !isArchiving,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Archive Event")
            }
        }

    }
}

/**
 * Format EventType enum to human-readable string
 */
private fun formatEventType(eventType: EventType): String {
    return when (eventType) {
        EventType.CONFERENCE -> "Conference"
        EventType.CONCERT -> "Concert"
        EventType.FESTIVAL -> "Festival"
        EventType.WORKSHOP -> "Workshop"
        EventType.SYMPOSIUM -> "Symposium"
        EventType.FORUM -> "Forum"
        EventType.EXPO -> "Expo"
        EventType.PITCH_EVENT -> "Pitch Event"
        EventType.HACKATHON -> "Hackathon"
        else -> eventType.rawValue
    }
}
