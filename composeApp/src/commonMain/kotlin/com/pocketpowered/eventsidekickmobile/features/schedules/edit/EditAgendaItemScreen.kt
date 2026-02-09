package com.district37.toastmasters.features.schedules.edit

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.forms.DateTimePickerField
import com.district37.toastmasters.components.forms.EntityFormScaffold
import com.district37.toastmasters.components.forms.FormTextField
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.viewmodel.FormResult
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAgendaItemScreen(
    agendaItemId: Int,
    onBackClick: () -> Unit,
    onAgendaItemUpdated: () -> Unit,
    onAddPerformer: () -> Unit,
    onSelectLocation: (venueId: Int?) -> Unit,
    selectedPerformerId: Int? = null,
    selectedPerformerName: String? = null,
    selectedLocationId: Int? = null,
    selectedLocationName: String? = null,
    clearLocation: Boolean = false,
    onPerformerHandled: () -> Unit = {},
    onLocationHandled: () -> Unit = {},
    onClearLocationHandled: () -> Unit = {}
) {
    val viewModel: EditAgendaItemViewModel = koinViewModel(
        key = "edit_agenda_item_$agendaItemId"
    ) { parametersOf(agendaItemId) }

    // Handle selected performer from picker (using ID and name for a lightweight performer object)
    LaunchedEffect(selectedPerformerId) {
        if (selectedPerformerId != null && selectedPerformerName != null) {
            viewModel.addPerformerById(selectedPerformerId, selectedPerformerName)
            onPerformerHandled()
        }
    }

    // Handle selected location from picker (using ID and name for a lightweight location object)
    LaunchedEffect(selectedLocationId) {
        if (selectedLocationId != null && selectedLocationName != null) {
            viewModel.updateLocationById(selectedLocationId, selectedLocationName)
            onLocationHandled()
        }
    }

    // Handle clear location
    LaunchedEffect(clearLocation) {
        if (clearLocation) {
            viewModel.updateLocation(null)
            onClearLocationHandled()
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endTime by viewModel.endTime.collectAsState()
    val eventStartDate by viewModel.eventStartDate.collectAsState()
    val eventEndDate by viewModel.eventEndDate.collectAsState()
    val performers by viewModel.performers.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val venueId by viewModel.venueId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    var tagDropdownExpanded by remember { mutableStateOf(false) }

    // Handle successful update
    LaunchedEffect(formResult) {
        if (formResult is FormResult.Success) {
            onAgendaItemUpdated()
        }
    }

    // Error dialog
    if (formResult is FormResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResult() },
            title = { Text("Error") },
            text = { Text((formResult as FormResult.Error).message ?: "Failed to update agenda item") },
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
                    text = loadError ?: "Failed to load agenda item",
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

    EntityFormScaffold(
        title = "Edit Agenda Item",
        onBackClick = onBackClick,
        onSubmit = { viewModel.submit() },
        isSubmitting = isSubmitting,
        submitEnabled = title.isNotBlank() && startTime != null && endTime != null,
        submitLabel = "Save"
    ) {
        FormTextField(
            value = title,
            onValueChange = { viewModel.updateTitle(it) },
            label = "Title",
            required = true,
            error = fieldErrors["title"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        FormTextField(
            value = description,
            onValueChange = { viewModel.updateDescription(it) },
            label = "Description",
            placeholder = "Optional description",
            enabled = !isSubmitting,
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start Time picker
        DateTimePickerField(
            label = "Start Time",
            selectedDateTime = startTime,
            onDateTimeSelected = { viewModel.updateStartTime(it) },
            minDateTime = eventStartDate,
            maxDateTime = eventEndDate,
            required = true,
            error = fieldErrors["startTime"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // End Time picker
        DateTimePickerField(
            label = "End Time",
            selectedDateTime = endTime,
            onDateTimeSelected = { viewModel.updateEndTime(it) },
            minDateTime = startTime ?: eventStartDate,
            maxDateTime = eventEndDate,
            required = true,
            error = fieldErrors["endTime"],
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tag dropdown
        ExposedDropdownMenuBox(
            expanded = tagDropdownExpanded,
            onExpandedChange = { tagDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedTag?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tag (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                enabled = !isSubmitting
            )

            ExposedDropdownMenu(
                expanded = tagDropdownExpanded,
                onDismissRequest = { tagDropdownExpanded = false }
            ) {
                viewModel.availableTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None") },
                        onClick = {
                            viewModel.updateTag(tag)
                            tagDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performers section
        PerformersSection(
            performers = performers,
            onAddPerformer = onAddPerformer,
            onRemovePerformer = { viewModel.removePerformer(it) },
            enabled = !isSubmitting
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location section
        LocationSection(
            selectedLocation = selectedLocation,
            venueId = venueId,
            onSelectLocation = { onSelectLocation(venueId) },
            onClearLocation = { viewModel.updateLocation(null) },
            enabled = !isSubmitting
        )
    }
}

@Composable
private fun PerformersSection(
    performers: List<Performer>,
    onAddPerformer: () -> Unit,
    onRemovePerformer: (Performer) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Performers",
                style = MaterialTheme.typography.titleMedium
            )
            AssistChip(
                onClick = onAddPerformer,
                label = { Text("Add") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                enabled = enabled
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (performers.isEmpty()) {
            Text(
                text = "No performers added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                performers.forEach { performer ->
                    PerformerChip(
                        performer = performer,
                        onRemove = { onRemovePerformer(performer) },
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformerChip(
    performer: Performer,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = performer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove performer",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    selectedLocation: Location?,
    venueId: Int?,
    onSelectLocation: () -> Unit,
    onClearLocation: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedLocation != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.clickable(enabled = enabled) { onSelectLocation() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocation.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    IconButton(
                        onClick = onClearLocation,
                        enabled = enabled,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear location",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            AssistChip(
                onClick = onSelectLocation,
                label = { Text("Select Location") },
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                enabled = enabled && venueId != null
            )
            if (venueId == null) {
                Text(
                    text = "No venue associated with this event",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
