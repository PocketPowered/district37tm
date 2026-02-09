package com.district37.toastmasters.features.create.event

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.components.forms.DateTimePickerField
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.EntityPickerField
import com.district37.toastmasters.components.forms.FormDropdown
import com.district37.toastmasters.components.forms.FormMultilineTextField
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.navigation.VenuePickerRoute
import com.district37.toastmasters.viewmodel.FormResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onEventCreated: (Int) -> Unit,
    viewModel: CreateEventViewModel = koinViewModel()
) {
    val navController = LocalNavController.current

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val eventType by viewModel.eventType.collectAsState()
    val selectedVenueName by viewModel.selectedVenueName.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    // Listen for selected venue from picker
    val selectedVenueId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Int?>("selected_venue_id", null)
        ?.collectAsState()

    val selectedVenueNameFromPicker = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("selected_venue_name", null)
        ?.collectAsState()

    // Update ViewModel when venue is selected from picker
    LaunchedEffect(selectedVenueId?.value, selectedVenueNameFromPicker?.value) {
        val id = selectedVenueId?.value
        val name = selectedVenueNameFromPicker?.value
        if (id != null && name != null) {
            viewModel.selectVenue(id, name)
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
            viewModel.loadAndSelectVenue(id)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("created_venue_id")
        }
    }

    // Handle successful creation
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            val event = (formResult as FormResult.Success).data
            onEventCreated(event.id)
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to create event") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResult() }) {
                    Text("OK")
                }
            }
        )
    }

    EntityFormScaffold(
        title = "Create Event",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting,
        submitEnabled = name.isNotBlank() && selectedVenueName != null && startDate != null && endDate != null
    ) {
        FormTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = "Event Name",
            required = true,
            error = fieldErrors["name"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormMultilineTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            label = "Description",
            placeholder = "Describe your event...",
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormDropdown(
            selectedOption = eventType,
            options = viewModel.eventTypes,
            onOptionSelected = { viewModel.updateEventType(it) },
            label = "Event Type",
            optionLabel = { formatEventType(it) },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        EntityPickerField(
            selectedName = selectedVenueName,
            label = "Venue",
            placeholder = "Select a venue",
            required = true,
            error = fieldErrors["venue"],
            onClick = { navController.navigate(VenuePickerRoute()) },
            onClear = { viewModel.clearVenue() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        DateTimePickerField(
            label = "Start Date",
            selectedDateTime = startDate,
            onDateTimeSelected = { viewModel.updateStartDate(it) },
            maxDateTime = endDate,
            required = true,
            error = fieldErrors["startDate"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        DateTimePickerField(
            label = "End Date",
            selectedDateTime = endDate,
            onDateTimeSelected = { viewModel.updateEndDate(it) },
            minDateTime = startDate,
            required = true,
            error = fieldErrors["endDate"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
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
