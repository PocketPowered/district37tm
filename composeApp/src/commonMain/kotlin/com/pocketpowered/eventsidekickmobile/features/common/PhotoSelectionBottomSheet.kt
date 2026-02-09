package com.district37.toastmasters.features.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.util.toImageBitmap
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

/**
 * State holder for the photo selection bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
class PhotoSelectionState(
    val scope: CoroutineScope,
    val sheetState: SheetState
) {
    var showOptionsSheet by mutableStateOf(false)

    // Focus region selection state
    var showFocusRegionSelector by mutableStateOf(false)
    var pendingImageBytes by mutableStateOf<ByteArray?>(null)
    var pendingImageBitmap by mutableStateOf<ImageBitmap?>(null)

    fun showOptions() {
        showOptionsSheet = true
    }

    fun hideOptions() {
        showOptionsSheet = false
    }

    /**
     * Show the focus region selection screen with the given image
     */
    fun showFocusRegionSelection(bytes: ByteArray, bitmap: ImageBitmap) {
        pendingImageBytes = bytes
        pendingImageBitmap = bitmap
        showFocusRegionSelector = true
    }

    /**
     * Hide the focus region selection screen and clear pending image
     */
    fun hideFocusRegionSelection() {
        showFocusRegionSelector = false
        pendingImageBytes = null
        pendingImageBitmap = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPhotoSelectionState(): PhotoSelectionState {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    return remember { PhotoSelectionState(scope, sheetState) }
}

/**
 * Reusable photo selection bottom sheet component
 *
 * Provides options for selecting an image via:
 * - Gallery picker
 * - Remove existing photo
 *
 * After selecting an image from gallery, the user is shown a focus region
 * selector to choose which part of the image should be the focal point when cropping.
 *
 * @param state The PhotoSelectionState to control visibility
 * @param title Title shown in the bottom sheet header
 * @param selectedImageBitmap The currently selected image bitmap from device (if any)
 * @param hasExistingImage Whether there's an existing image that can be removed
 * @param onImageSelected Called when an image is selected from gallery (provides ByteArray and optional FocusRegion)
 * @param onRemove Called when the remove photo option is selected
 * @param enabled Whether the photo selection is enabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSelectionBottomSheet(
    state: PhotoSelectionState,
    title: String = "Change Photo",
    selectedImageBitmap: ImageBitmap? = null,
    hasExistingImage: Boolean = false,
    onImageSelected: (ByteArray, FocusRegion?) -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean = true
) {
    val scope = state.scope

    // Gallery picker launcher using FileKit - shows focus region selector after selection
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { platformFile ->
        platformFile?.let { file ->
            scope.launch {
                // Read bytes using FileKit extension
                val bytes = file.readBytes()
                val bitmap = bytes.toImageBitmap()
                if (bitmap != null) {
                    state.showFocusRegionSelection(bytes, bitmap)
                }
            }
        }
    }

    // Focus region selection screen
    if (state.showFocusRegionSelector && state.pendingImageBitmap != null && state.pendingImageBytes != null) {
        FocusRegionSelectionScreen(
            imageBitmap = state.pendingImageBitmap!!,
            onConfirm = { focusRegion ->
                val bytes = state.pendingImageBytes!!
                state.hideFocusRegionSelection()
                onImageSelected(bytes, focusRegion)
            },
            onSkip = {
                val bytes = state.pendingImageBytes!!
                state.hideFocusRegionSelection()
                onImageSelected(bytes, null)
            },
            onCancel = {
                state.hideFocusRegionSelection()
            }
        )
        return
    }

    // Bottom sheet for image options (Gallery only)
    if (state.showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { state.showOptionsSheet = false },
            sheetState = state.sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Choose from Gallery
                PhotoOptionRow(
                    icon = Icons.Default.PhotoLibrary,
                    text = "Choose from Gallery",
                    onClick = {
                        state.hideOptions()
                        galleryLauncher.launch()
                    }
                )

                // Remove Photo option (only show if there's an existing photo)
                if (hasExistingImage || selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    PhotoOptionRow(
                        icon = Icons.Default.Delete,
                        text = "Remove Photo",
                        textColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            onRemove()
                            state.hideOptions()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Row item for photo options in the bottom sheet
 */
@Composable
private fun PhotoOptionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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
            tint = textColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

/**
 * A clickable image preview box that shows the selected image
 *
 * @param selectedImageBitmap A locally selected image bitmap
 * @param onClick Called when the preview is clicked
 * @param enabled Whether clicking is enabled
 * @param modifier Modifier for the component
 */
@Composable
fun EditableImagePreview(
    selectedImageBitmap: ImageBitmap?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageBitmap != null) {
            Image(
                bitmap = selectedImageBitmap,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            PhotoPlaceholder()
        }

        // Camera icon overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change photo",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun PhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = "No photo",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
