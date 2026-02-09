package com.district37.toastmasters.features.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.district37.toastmasters.models.Image
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Photo manager component that displays multiple photos in a horizontal scrolling row
 * Supports adding, editing, deleting, and reordering photos
 *
 * @param images List of existing images from the server
 * @param pendingImages List of images pending upload (not yet saved to server)
 * @param onAddPhoto Callback when user wants to add a new photo
 * @param onEditPhoto Callback when user taps on an existing photo to edit
 * @param onDeletePhoto Callback when user wants to delete a photo
 * @param onReorderPhotos Callback when user reorders photos via drag and drop
 * @param isEnabled Whether the component is enabled (not during submission/upload)
 * @param maxPhotos Maximum number of photos allowed (default 5)
 * @param modifier Optional modifier for the component
 */
@Composable
fun PhotoManagerComponent(
    images: List<Image>,
    pendingImages: List<PendingImageState>,
    onAddPhoto: () -> Unit,
    onEditPhoto: (Image) -> Unit,
    onDeletePendingPhoto: (String) -> Unit,
    onReorderPhotos: (List<Any>) -> Unit,
    isEnabled: Boolean = true,
    maxPhotos: Int = 5,
    modifier: Modifier = Modifier
) {
    // Combine images and pending images for display
    val allPhotos = remember(images, pendingImages) {
        (images as List<Any>) + (pendingImages as List<Any>)
    }

    // Drag and drop state
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dropTargetIndex by remember { mutableStateOf<Int?>(null) }

    // Position tracking for drop target calculation
    val itemPositions = remember { mutableStateMapOf<Int, IntOffset>() }
    val itemSizes = remember { mutableStateMapOf<Int, IntSize>() }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header with count
        Text(
            text = "Photos (${allPhotos.size}/$maxPhotos)",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scrolling photo row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo thumbnails
            itemsIndexed(allPhotos) { index, photo ->
                val isDragging = draggedItemIndex == index
                val isDropTarget = dropTargetIndex == index

                PhotoThumbnail(
                    photo = photo,
                    position = index + 1,
                    index = index,
                    isDragging = isDragging,
                    isDropTarget = isDropTarget,
                    isEnabled = isEnabled,
                    allItemPositions = itemPositions,
                    allItemSizes = itemSizes,
                    onTap = {
                        when (photo) {
                            is Image -> onEditPhoto(photo)
                            is PendingImageState -> {
                                // Pending images can only be deleted
                                onDeletePendingPhoto(photo.id)
                            }
                        }
                    },
                    onDragStart = {
                        if (isEnabled) {
                            draggedItemIndex = index
                        }
                    },
                    onDragEnd = {
                        if (draggedItemIndex != null && dropTargetIndex != null) {
                            // Perform reorder
                            val newList = allPhotos.toMutableList()
                            val item = newList.removeAt(draggedItemIndex!!)
                            newList.add(dropTargetIndex!!, item)
                            onReorderPhotos(newList)
                        }
                        draggedItemIndex = null
                        dropTargetIndex = null
                    },
                    onDragOver = { targetIndex ->
                        dropTargetIndex = targetIndex
                    }
                )
            }

            // Add photo button
            item {
                AddPhotoButton(
                    onClick = onAddPhoto,
                    enabled = isEnabled && allPhotos.size < maxPhotos
                )
            }
        }

        // Helper text for empty state
        if (allPhotos.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No photos yet. Tap the + button to add photos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual photo thumbnail with drag-and-drop support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoThumbnail(
    photo: Any,
    position: Int,
    index: Int,
    isDragging: Boolean,
    isDropTarget: Boolean,
    isEnabled: Boolean,
    allItemPositions: Map<Int, IntOffset>,
    allItemSizes: Map<Int, IntSize>,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragOver: (Int) -> Unit
) {
    // Animate elevation and scale during drag
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
    val scale = if (isDragging) 1.05f else 1f
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(100.dp)
            .shadow(elevation, RoundedCornerShape(8.dp))
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .zIndex(if (isDragging) 1f else 0f)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                allItemPositions[index]?.let { existingPos ->
                    if (existingPos.x != position.x.toInt() || existingPos.y != position.y.toInt()) {
                        allItemPositions as MutableMap
                        allItemPositions[index] = IntOffset(position.x.toInt(), position.y.toInt())
                    }
                } ?: run {
                    allItemPositions as MutableMap
                    allItemPositions[index] = IntOffset(position.x.toInt(), position.y.toInt())
                }

                val size = coordinates.size
                allItemSizes[index]?.let { existingSize ->
                    if (existingSize.width != size.width || existingSize.height != size.height) {
                        allItemSizes as MutableMap
                        allItemSizes[index] = size
                    }
                } ?: run {
                    allItemSizes as MutableMap
                    allItemSizes[index] = size
                }
            }
    ) {
        Card(
            onClick = {},
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isDropTarget) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else Modifier
                )
                .combinedClickable(
                    enabled = isEnabled,
                    onClick = onTap,
                    onLongClick = {
                        if (isEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragStart()
                        }
                    }
                )
                .pointerInput(isDragging) {
                    if (isDragging) {
                        detectDragGestures(
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, _ ->
                                change.consume()
                                val dragScreenX = (allItemPositions[index]?.x ?: 0) + change.position.x.toInt()

                                val targetIndex = allItemPositions.entries.firstOrNull { (idx, pos) ->
                                    if (idx == index) return@firstOrNull false
                                    val size = allItemSizes[idx] ?: return@firstOrNull false
                                    dragScreenX >= pos.x && dragScreenX <= pos.x + size.width
                                }?.key

                                targetIndex?.let { onDragOver(it) }
                            }
                        )
                    }
                },
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Image display
                when (photo) {
                    is Image -> {
                        CoilImage(
                            imageModel = { photo.url },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = photo.getCropAlignment(),
                                contentDescription = photo.altText ?: "Photo $position"
                            )
                        )
                    }
                    is PendingImageState -> {
                        photo.bitmap?.let { bitmap ->
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = "Pending photo $position",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Position badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(20.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Dragging indicator
                if (isDragging) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

/**
 * Add photo button
 */
@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(32.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

/**
 * State for pending images (not yet uploaded to server)
 */
data class PendingImageState(
    val id: String,
    val bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    val imageBytes: ByteArray,
    val focusRegion: com.district37.toastmasters.models.FocusRegion?,
    val displayOrder: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PendingImageState

        if (id != other.id) return false
        if (bitmap != other.bitmap) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (focusRegion != other.focusRegion) return false
        if (displayOrder != other.displayOrder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + (focusRegion?.hashCode() ?: 0)
        result = 31 * result + displayOrder
        return result
    }
}
