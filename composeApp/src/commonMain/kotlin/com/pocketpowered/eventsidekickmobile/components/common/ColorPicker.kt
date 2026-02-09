package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Preset color palette for organization branding.
 * These colors are designed to work well as primary and secondary brand colors.
 */
private val presetColors = listOf(
    "#E53935", // Red
    "#D81B60", // Pink
    "#8E24AA", // Purple
    "#5E35B1", // Deep Purple
    "#3949AB", // Indigo
    "#1E88E5", // Blue
    "#039BE5", // Light Blue
    "#00ACC1", // Cyan
    "#00897B", // Teal
    "#43A047", // Green
    "#7CB342", // Light Green
    "#C0CA33", // Lime
    "#FDD835", // Yellow
    "#FFB300", // Amber
    "#FB8C00", // Orange
    "#F4511E", // Deep Orange
    "#6D4C41", // Brown
    "#757575", // Grey
    "#546E7A", // Blue Grey
    "#212121"  // Black
)

/**
 * Converts a hex color string to a Compose Color.
 * Handles both "#RRGGBB" and "RRGGBB" formats.
 */
private fun hexToColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF$cleanHex").toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Validates if a string is a valid hex color (with or without #).
 * Returns the normalized hex string with # prefix, or null if invalid.
 */
private fun validateAndNormalizeHex(input: String): String? {
    val cleanInput = input.trim().removePrefix("#").uppercase()
    if (cleanInput.length != 6) return null
    if (!cleanInput.all { it in '0'..'9' || it in 'A'..'F' }) return null
    return "#$cleanInput"
}

/**
 * A color picker component that displays a grid of preset color swatches
 * plus a custom hex input field for any color.
 *
 * @param selectedColor The currently selected color in hex format (e.g., "#FF5733")
 * @param onColorSelected Callback when a color is selected, provides the hex string
 * @param label Label text displayed above the color grid
 * @param modifier Modifier for the root component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    selectedColor: String?,
    onColorSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    // Track the custom hex input separately
    var customHexInput by remember { mutableStateOf("") }
    var isCustomColorValid by remember { mutableStateOf(false) }

    // Check if current selection is a preset color
    val isPresetSelected = selectedColor != null &&
        presetColors.any { it.equals(selectedColor, ignoreCase = true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Preset colors grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presetColors.forEach { colorHex ->
                ColorSwatch(
                    colorHex = colorHex,
                    isSelected = selectedColor?.equals(colorHex, ignoreCase = true) == true,
                    onClick = {
                        customHexInput = "" // Clear custom input when preset selected
                        onColorSelected(colorHex)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom hex input
        Text(
            text = "Or enter a custom color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = customHexInput,
                onValueChange = { input ->
                    // Strip # from input - we'll display it as a fixed prefix
                    val withoutHash = input.removePrefix("#")
                    // Only allow valid hex characters, max 6
                    val filtered = withoutHash.filter { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                        .take(6)
                    customHexInput = filtered

                    // Validate and apply if valid (6 hex chars)
                    if (filtered.length == 6) {
                        val normalized = "#${filtered.uppercase()}"
                        isCustomColorValid = true
                        onColorSelected(normalized)
                    } else {
                        isCustomColorValid = false
                    }
                },
                placeholder = { Text("FF5733") },
                prefix = { Text("#") },
                singleLine = true,
                isError = customHexInput.isNotEmpty() && !isCustomColorValid,
                supportingText = if (customHexInput.isNotEmpty() && !isCustomColorValid) {
                    { Text("Enter 6 hex characters (e.g., FF5733)") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                modifier = Modifier.weight(1f)
            )

            // Custom color preview swatch
            if (isCustomColorValid && !isPresetSelected) {
                selectedColor?.let { hex ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(hexToColor(hex))
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isLightColor(hex)) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Show selected color preview
        selectedColor?.let { hex ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(hexToColor(hex))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hex.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isLightColor(hex)) Color.Black else Color.White
                )
            }
        }
    }
}

/**
 * A single color swatch that can be selected.
 */
@Composable
private fun ColorSwatch(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(hexToColor(colorHex))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isLightColor(colorHex)) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Determines if a hex color is considered "light" for contrast purposes.
 */
private fun isLightColor(hex: String): Boolean {
    val cleanHex = hex.removePrefix("#")
    if (cleanHex.length < 6) return false

    return try {
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)

        // Using relative luminance formula
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
        luminance > 186
    } catch (e: Exception) {
        false
    }
}
