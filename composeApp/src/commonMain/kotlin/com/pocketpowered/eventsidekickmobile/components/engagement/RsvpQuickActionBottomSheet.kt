package com.district37.toastmasters.components.engagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.AgendaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for the RSVP quick action bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
class RsvpQuickActionState(
    val scope: CoroutineScope,
    val sheetState: SheetState
) {
    var showSheet by mutableStateOf(false)
    var targetAgendaItem by mutableStateOf<AgendaItem?>(null)

    /**
     * Show the bottom sheet for the given agenda item.
     */
    fun show(item: AgendaItem) {
        targetAgendaItem = item
        showSheet = true
    }

    /**
     * Hide the bottom sheet.
     */
    fun hide() {
        scope.launch {
            sheetState.hide()
            showSheet = false
        }
    }
}

/**
 * Remember a RsvpQuickActionState instance for the current composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberRsvpQuickActionState(): RsvpQuickActionState {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    return remember { RsvpQuickActionState(scope, sheetState) }
}

/**
 * RSVP quick action bottom sheet component.
 * Displays a menu with RSVP options (Going, Not Going, Undecided, Clear Status).
 *
 * @param state The RsvpQuickActionState to control visibility
 * @param onStatusSelected Callback when a status is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RsvpQuickActionBottomSheet(
    state: RsvpQuickActionState,
    onStatusSelected: (UserEngagementStatus?) -> Unit
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
                // Header
                Text(
                    text = "RSVP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Going option
                RsvpOptionRow(
                    status = UserEngagementStatus.GOING,
                    text = "Going",
                    onClick = {
                        onStatusSelected(UserEngagementStatus.GOING)
                        state.hide()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Not Going option
                RsvpOptionRow(
                    status = UserEngagementStatus.NOT_GOING,
                    text = "Not Going",
                    onClick = {
                        onStatusSelected(UserEngagementStatus.NOT_GOING)
                        state.hide()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Undecided option
                RsvpOptionRow(
                    status = UserEngagementStatus.UNDECIDED,
                    text = "Undecided",
                    onClick = {
                        onStatusSelected(UserEngagementStatus.UNDECIDED)
                        state.hide()
                    }
                )

                // Divider before Clear Status
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Clear Status option
                RsvpOptionRow(
                    status = null,
                    text = "Clear Status",
                    onClick = {
                        onStatusSelected(null)
                        state.hide()
                    }
                )
            }
        }
    }
}

/**
 * Row item for RSVP options in the bottom sheet.
 */
@Composable
private fun RsvpOptionRow(
    status: UserEngagementStatus?,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status indicator dot
        StatusIndicator(status = status, size = 16)

        // Option text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
