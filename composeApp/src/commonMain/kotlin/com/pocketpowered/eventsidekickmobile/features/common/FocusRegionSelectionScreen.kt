package com.district37.toastmasters.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.images.FocusRegionSelector
import com.district37.toastmasters.models.FocusRegion

/**
 * Full-screen modal for selecting a focus region on an image.
 *
 * @param imageBitmap The image to select focus region on
 * @param initialFocusRegion Optional initial focus region
 * @param onConfirm Called when user confirms selection with the focus region
 * @param onSkip Called when user wants to skip focus region selection (uses default center crop)
 * @param onCancel Called when user cancels the selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusRegionSelectionScreen(
    imageBitmap: ImageBitmap,
    initialFocusRegion: FocusRegion? = null,
    onConfirm: (FocusRegion) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    // Track the current focus region
    var currentFocusRegion by remember {
        mutableStateOf(initialFocusRegion ?: FocusRegion(0.2f, 0.2f, 0.6f, 0.6f))
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Focus Area",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instructions
            Text(
                text = "Drag the box to select the important area of your image. This area will stay visible when the image is cropped.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Focus region selector
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                FocusRegionSelector(
                    imageBitmap = imageBitmap,
                    initialFocusRegion = currentFocusRegion,
                    onFocusRegionChanged = { region ->
                        currentFocusRegion = region
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skip button
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip")
                }

                // Confirm button
                Button(
                    onClick = { onConfirm(currentFocusRegion) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Help text
            Text(
                text = "Tip: Drag corners to resize, drag center to move",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
