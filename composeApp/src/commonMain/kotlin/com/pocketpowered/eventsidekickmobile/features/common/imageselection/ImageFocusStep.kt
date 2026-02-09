package com.district37.toastmasters.features.common.imageselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.images.FocusRegionSelector
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.models.FocusRegion

/**
 * Step 2 of the Image Selection Wizard: Focus Region Selection.
 *
 * Shows an interactive focus region selector for device images.
 * Users can drag corners to resize and drag the center to move the selection box.
 *
 * @param imageBitmap The selected image bitmap
 * @param focusRegion Current focus region
 * @param onFocusRegionChanged Callback when focus region changes
 * @param modifier Modifier for the composable
 */
@Composable
fun ImageFocusStep(
    imageBitmap: ImageBitmap?,
    focusRegion: FocusRegion?,
    onFocusRegionChanged: (FocusRegion?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentFocusRegion by remember(focusRegion) {
        mutableStateOf(focusRegion ?: FocusRegion(0.2f, 0.2f, 0.6f, 0.6f))
    }

    WizardStepContainer(
        prompt = "Set focus area",
        subtitle = "Select the important area of your image",
        scrollable = false, // Fixed-size content
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Drag the box to select the important area",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                imageBitmap?.let { bitmap ->
                    FocusRegionSelector(
                        imageBitmap = bitmap,
                        initialFocusRegion = currentFocusRegion,
                        onFocusRegionChanged = { region ->
                            currentFocusRegion = region
                            onFocusRegionChanged(region)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(
                text = "Tip: Drag corners to resize, drag center to move",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
