package com.district37.toastmasters.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable confirmation dialog for delete operations.
 * Shows a destructive (red) confirm button to emphasize the action.
 *
 * @param title The dialog title
 * @param message The confirmation message explaining what will be deleted
 * @param confirmText The text for the confirm button (default: "Delete")
 * @param dismissText The text for the dismiss button (default: "Cancel")
 * @param isDeleting Whether deletion is in progress (shows loading indicator)
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    isDeleting: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Convenience composable for event deletion confirmation
 */
@Composable
fun DeleteEventConfirmationDialog(
    eventName: String,
    isDeleting: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteConfirmationDialog(
        title = "Delete Event",
        message = "Are you sure you want to delete \"$eventName\"? This will also delete all schedules and schedule items associated with this event. This action cannot be undone.",
        isDeleting = isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Convenience composable for schedule deletion confirmation
 */
@Composable
fun DeleteScheduleConfirmationDialog(
    scheduleName: String?,
    isDeleting: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayName = scheduleName?.takeIf { it.isNotBlank() } ?: "this schedule"
    DeleteConfirmationDialog(
        title = "Delete Schedule",
        message = "Are you sure you want to delete $displayName? This will also delete all schedule items in this schedule. This action cannot be undone.",
        isDeleting = isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Convenience composable for schedule item deletion confirmation
 */
@Composable
fun DeleteScheduleItemConfirmationDialog(
    scheduleItemTitle: String,
    isDeleting: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteConfirmationDialog(
        title = "Delete Schedule Item",
        message = "Are you sure you want to delete \"$scheduleItemTitle\"? This action cannot be undone.",
        isDeleting = isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * A reusable confirmation dialog for warning operations.
 * Shows a warning-styled confirm button.
 */
@Composable
fun WarningConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Continue",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Confirmation dialog for changing an event's venue.
 * Warns that schedule item locations will be invalidated.
 */
@Composable
fun ChangeVenueConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    WarningConfirmationDialog(
        title = "Change Venue",
        message = "Changing the venue will invalidate all schedule item locations, as they are based on the current venue. Are you sure you want to continue?",
        confirmText = "Change Venue",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * A reusable confirmation dialog for archive operations.
 * Shows a warning-styled confirm button with archive-specific messaging.
 *
 * @param title The dialog title
 * @param message The confirmation message explaining what will be archived
 * @param confirmText The text for the confirm button (default: "Archive")
 * @param dismissText The text for the dismiss button (default: "Cancel")
 * @param isArchiving Whether archiving is in progress (shows loading indicator)
 * @param onConfirm Callback when user confirms archiving
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun ArchiveConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Archive",
    dismissText: String = "Cancel",
    isArchiving: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isArchiving) onDismiss() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isArchiving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                if (isArchiving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isArchiving
            ) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Convenience composable for event archiving confirmation
 */
@Composable
fun ArchiveEventConfirmationDialog(
    eventName: String,
    isArchiving: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ArchiveConfirmationDialog(
        title = "Archive Event",
        message = "Are you sure you want to archive \"$eventName\"? The event will no longer appear in search results or discovery, but will still be accessible via direct link. Archived events cannot be edited. This action cannot be undone.",
        isArchiving = isArchiving,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
