package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Converts a hex color string to a Compose Color.
 * Handles both "#RRGGBB" and "RRGGBB" formats.
 */
fun hexToComposeColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF$cleanHex").toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * A clickable color field that displays the current color and opens a color picker dialog.
 *
 * @param selectedColor The currently selected color in hex format (e.g., "#FF5733")
 * @param onColorSelected Callback when a color is selected, provides the hex string
 * @param label Label text displayed above the color swatch
 * @param enabled Whether the field is interactive
 * @param modifier Modifier for the root component
 */
@Composable
fun ColorPickerField(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Clickable color swatch
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedColor.isNotBlank()) {
                        hexToComposeColor(selectedColor)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (enabled) {
                        Modifier.clickable { showColorPicker = true }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selectedColor.isBlank()) {
                Text(
                    text = "Tap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Show hex value below the swatch
        if (selectedColor.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedColor.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Color picker dialog
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Select $label") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    ColorPicker(
                        selectedColor = selectedColor.ifBlank { null },
                        onColorSelected = onColorSelected,
                        label = "Choose a color"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Done")
                }
            }
        )
    }
}

/**
 * A row displaying two color picker fields side by side.
 *
 * @param primaryColor The primary color hex value
 * @param onPrimaryColorChange Callback when primary color changes
 * @param secondaryColor The secondary color hex value
 * @param onSecondaryColorChange Callback when secondary color changes
 * @param enabled Whether the fields are interactive
 * @param modifier Modifier for the root component
 */
@Composable
fun BrandingColorRow(
    primaryColor: String,
    onPrimaryColorChange: (String) -> Unit,
    secondaryColor: String,
    onSecondaryColorChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        ColorPickerField(
            selectedColor = primaryColor,
            onColorSelected = onPrimaryColorChange,
            label = "Primary Color",
            enabled = enabled
        )

        ColorPickerField(
            selectedColor = secondaryColor,
            onColorSelected = onSecondaryColorChange,
            label = "Secondary Color",
            enabled = enabled
        )
    }
}
