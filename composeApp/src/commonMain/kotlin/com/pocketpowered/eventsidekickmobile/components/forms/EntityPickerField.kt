package com.district37.toastmasters.components.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable field component for selecting an entity (e.g., Venue, Performer)
 *
 * Displays the selected entity name with a clickable card to open a picker.
 * Optionally shows a clear button to deselect.
 *
 * @param selectedName The name of the currently selected entity, or null if none
 * @param label Label for the field
 * @param placeholder Placeholder text when nothing is selected
 * @param onClick Callback when the field is clicked to open the picker
 * @param onClear Optional callback to clear the selection
 */
@Composable
fun EntityPickerField(
    selectedName: String?,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    error: String? = null,
    required: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        // Label above the card
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() },
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    error != null -> MaterialTheme.colorScheme.error
                    !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected value or placeholder
                Text(
                    text = selectedName ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedName != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Clear button or arrow indicator
                if (selectedName != null && onClear != null && enabled) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear selection",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (enabled) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
