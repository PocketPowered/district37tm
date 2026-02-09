package com.district37.toastmasters.features.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for the create options bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
class CreateOptionsState(
    private val scope: CoroutineScope,
    val sheetState: SheetState
) {
    var showSheet by mutableStateOf(false)

    fun show() {
        showSheet = true
    }

    fun hide() {
        scope.launch {
            sheetState.hide()
            showSheet = false
        }
    }
}

/**
 * Remember a CreateOptionsState instance for the current composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberCreateOptionsState(): CreateOptionsState {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    return remember { CreateOptionsState(scope, sheetState) }
}

/**
 * Create options bottom sheet component.
 * Displays a menu with creation options (Event, Venue, Performer, Organization).
 *
 * @param state The CreateOptionsState to control visibility
 * @param onCreateEvent Callback when Create Event is selected
 * @param onCreateVenue Callback when Create Venue is selected
 * @param onCreatePerformer Callback when Create Performer is selected
 * @param onCreateOrganization Callback when Create Organization is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOptionsBottomSheet(
    state: CreateOptionsState,
    onCreateEvent: () -> Unit,
    onCreateVenue: () -> Unit,
    onCreatePerformer: () -> Unit,
    onCreateOrganization: () -> Unit
) {
    if (state.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { state.showSheet = false },
            sheetState = state.sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Create",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                CreateOptionRow(
                    icon = Icons.Default.DateRange,
                    text = "Event",
                    subtitle = "Add a new event like a conference, concert, or festival",
                    onClick = {
                        onCreateEvent()
                        state.hide()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CreateOptionRow(
                    icon = Icons.Default.Home,
                    text = "Venue",
                    subtitle = "Add a new venue where events take place",
                    onClick = {
                        onCreateVenue()
                        state.hide()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CreateOptionRow(
                    icon = Icons.Default.Person,
                    text = "Performer",
                    subtitle = "Add a new performer, speaker, or artist",
                    onClick = {
                        onCreatePerformer()
                        state.hide()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CreateOptionRow(
                    icon = Icons.Default.Business,
                    text = "Organization",
                    subtitle = "Create a new organization to manage events together",
                    onClick = {
                        onCreateOrganization()
                        state.hide()
                    }
                )
            }
        }
    }
}

/**
 * Row item for create options in the bottom sheet.
 */
@Composable
private fun CreateOptionRow(
    icon: ImageVector,
    text: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
