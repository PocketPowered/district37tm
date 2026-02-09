package com.district37.toastmasters.features.common.imageselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.models.FocusRegion
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Step 3 of the Image Selection Wizard: Caption Input.
 *
 * Allows users to add an optional caption to their selected image.
 * Shows a preview of the image and a text input field.
 * The preview respects the focus region set in the previous step.
 *
 * @param imageBitmap The selected image bitmap
 * @param focusRegion The focus region to apply when displaying the image preview
 * @param caption Current caption text
 * @param onCaptionChanged Callback when caption changes
 * @param modifier Modifier for the composable
 */
@Composable
fun ImageCaptionStep(
    imageBitmap: ImageBitmap?,
    focusRegion: FocusRegion?,
    caption: String,
    onCaptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepContainer(
        prompt = "Add a caption",
        subtitle = "Help others understand your image (optional)",
        scrollable = false, // Fixed-size content with aspectRatio
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = focusRegion?.toBiasAlignment() ?: Alignment.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption input
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = caption,
                    onValueChange = onCaptionChanged,
                    placeholder = { Text("Describe this image...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "A caption helps provide context for the image",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
