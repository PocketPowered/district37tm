package com.district37.toastmasters.features.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.Image

/**
 * Bottom sheet for editing an existing photo
 * Currently supports deleting photos.
 *
 * TODO: Add focus region editing support by:
 * 1. Loading the image bitmap from URL asynchronously
 * 2. Launching ImageSelectionWizard or FocusRegionSelector with loaded bitmap
 * 3. Updating the image via ImageRepository.updateImageFocusRegion()
 *
 * @param image The image to edit
 * @param onDelete Callback when user wants to delete the photo
 * @param onDismiss Callback when user dismisses the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditBottomSheet(
    image: Image,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            // Header
            Text(
                text = "Edit Photo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Note: Edit Focus Region is disabled for existing images as it requires
            // loading the image bitmap asynchronously. This will be added in a future update.

            // Delete option
            ListItem(
                headlineContent = { Text("Delete Photo") },
                supportingContent = { Text("Remove this photo from the ${getEntityTypeName(image)}") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete photo",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    showDeleteConfirmation = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Delete Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Photo?") },
            text = { Text("Are you sure you want to delete this photo? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Helper function to get entity type name from image
 * This is a placeholder - in a real implementation, you might want to pass the entity type
 */
private fun getEntityTypeName(image: Image): String {
    // This could be enhanced to use the actual entity type if available
    return "entity"
}
