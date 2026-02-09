package com.district37.toastmasters.components.images

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.FocusRegion
import kotlin.math.roundToInt

/**
 * Handle positions for resizing the focus region
 */
private enum class DragHandle {
    NONE,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER  // For moving the entire box
}

private const val HANDLE_RADIUS_DP = 12f
private const val MIN_REGION_SIZE = 0.1f  // Minimum 10% of image dimension

/**
 * Interactive focus region selector for images.
 * Allows users to drag a bounding box to specify the important area of an image.
 *
 * @param imageBitmap The image to display
 * @param initialFocusRegion Initial focus region (defaults to center 60%)
 * @param onFocusRegionChanged Callback when region changes
 * @param modifier Modifier for the component
 */
@Composable
fun FocusRegionSelector(
    imageBitmap: ImageBitmap,
    initialFocusRegion: FocusRegion? = null,
    onFocusRegionChanged: (FocusRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val handleRadiusPx = with(density) { HANDLE_RADIUS_DP.dp.toPx() }

    // Component size in pixels
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    // Current focus region in normalized coordinates (0-1)
    var focusRegion by remember {
        mutableStateOf(initialFocusRegion ?: FocusRegion(0.2f, 0.2f, 0.6f, 0.6f))
    }

    // Track which handle is being dragged
    var activeHandle by remember { mutableStateOf(DragHandle.NONE) }

    // Convert normalized coordinates to pixel coordinates
    fun normalizedToPixel(region: FocusRegion): Rect {
        return Rect(
            left = region.x * componentSize.width,
            top = region.y * componentSize.height,
            right = (region.x + region.width) * componentSize.width,
            bottom = (region.y + region.height) * componentSize.height
        )
    }

    // Convert pixel coordinates back to normalized
    fun pixelToNormalized(rect: Rect): FocusRegion {
        val x = (rect.left / componentSize.width).coerceIn(0f, 1f - MIN_REGION_SIZE)
        val y = (rect.top / componentSize.height).coerceIn(0f, 1f - MIN_REGION_SIZE)
        val width = (rect.width / componentSize.width).coerceIn(MIN_REGION_SIZE, 1f - x)
        val height = (rect.height / componentSize.height).coerceIn(MIN_REGION_SIZE, 1f - y)
        return FocusRegion(x, y, width, height)
    }

    // Determine which handle is at a given position
    fun getHandleAtPosition(position: Offset, region: Rect): DragHandle {
        val hitRadius = handleRadiusPx * 2  // Larger hit area for easier touch

        // Check corners first
        if ((position - Offset(region.left, region.top)).getDistance() < hitRadius) {
            return DragHandle.TOP_LEFT
        }
        if ((position - Offset(region.right, region.top)).getDistance() < hitRadius) {
            return DragHandle.TOP_RIGHT
        }
        if ((position - Offset(region.left, region.bottom)).getDistance() < hitRadius) {
            return DragHandle.BOTTOM_LEFT
        }
        if ((position - Offset(region.right, region.bottom)).getDistance() < hitRadius) {
            return DragHandle.BOTTOM_RIGHT
        }

        // Check if inside the box for moving
        if (region.contains(position)) {
            return DragHandle.CENTER
        }

        return DragHandle.NONE
    }

    // Update region based on drag
    fun updateRegion(handle: DragHandle, dragAmount: Offset, currentRect: Rect): Rect {
        val minSize = MIN_REGION_SIZE * minOf(componentSize.width, componentSize.height)

        return when (handle) {
            DragHandle.TOP_LEFT -> {
                val newLeft = (currentRect.left + dragAmount.x).coerceIn(0f, currentRect.right - minSize)
                val newTop = (currentRect.top + dragAmount.y).coerceIn(0f, currentRect.bottom - minSize)
                Rect(newLeft, newTop, currentRect.right, currentRect.bottom)
            }
            DragHandle.TOP_RIGHT -> {
                val newRight = (currentRect.right + dragAmount.x).coerceIn(currentRect.left + minSize, componentSize.width.toFloat())
                val newTop = (currentRect.top + dragAmount.y).coerceIn(0f, currentRect.bottom - minSize)
                Rect(currentRect.left, newTop, newRight, currentRect.bottom)
            }
            DragHandle.BOTTOM_LEFT -> {
                val newLeft = (currentRect.left + dragAmount.x).coerceIn(0f, currentRect.right - minSize)
                val newBottom = (currentRect.bottom + dragAmount.y).coerceIn(currentRect.top + minSize, componentSize.height.toFloat())
                Rect(newLeft, currentRect.top, currentRect.right, newBottom)
            }
            DragHandle.BOTTOM_RIGHT -> {
                val newRight = (currentRect.right + dragAmount.x).coerceIn(currentRect.left + minSize, componentSize.width.toFloat())
                val newBottom = (currentRect.bottom + dragAmount.y).coerceIn(currentRect.top + minSize, componentSize.height.toFloat())
                Rect(currentRect.left, currentRect.top, newRight, newBottom)
            }
            DragHandle.CENTER -> {
                val width = currentRect.width
                val height = currentRect.height
                val newLeft = (currentRect.left + dragAmount.x).coerceIn(0f, componentSize.width - width)
                val newTop = (currentRect.top + dragAmount.y).coerceIn(0f, componentSize.height - height)
                Rect(newLeft, newTop, newLeft + width, newTop + height)
            }
            DragHandle.NONE -> currentRect
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { componentSize = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val rect = normalizedToPixel(focusRegion)
                        activeHandle = getHandleAtPosition(offset, rect)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (activeHandle != DragHandle.NONE) {
                            val currentRect = normalizedToPixel(focusRegion)
                            val newRect = updateRegion(activeHandle, dragAmount, currentRect)
                            focusRegion = pixelToNormalized(newRect)
                            onFocusRegionChanged(focusRegion)
                        }
                    },
                    onDragEnd = {
                        activeHandle = DragHandle.NONE
                    }
                )
            }
    ) {
        // Background image
        Image(
            bitmap = imageBitmap,
            contentDescription = "Image to select focus region",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Overlay and selection box
        if (componentSize.width > 0 && componentSize.height > 0) {
            val rect = normalizedToPixel(focusRegion)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val overlayColor = Color.Black.copy(alpha = 0.5f)
                val borderColor = Color.White
                val accentColor = Color(0xFF4CAF50)  // Green accent

                // Draw dark overlay outside the selection
                // Top rectangle
                drawRect(
                    color = overlayColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, rect.top)
                )
                // Bottom rectangle
                drawRect(
                    color = overlayColor,
                    topLeft = Offset(0f, rect.bottom),
                    size = Size(size.width, size.height - rect.bottom)
                )
                // Left rectangle
                drawRect(
                    color = overlayColor,
                    topLeft = Offset(0f, rect.top),
                    size = Size(rect.left, rect.height)
                )
                // Right rectangle
                drawRect(
                    color = overlayColor,
                    topLeft = Offset(rect.right, rect.top),
                    size = Size(size.width - rect.right, rect.height)
                )

                // Draw selection border
                drawRect(
                    color = borderColor,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = 3f)
                )

                // Draw rule of thirds grid lines
                val thirdWidth = rect.width / 3
                val thirdHeight = rect.height / 3
                val gridColor = Color.White.copy(alpha = 0.4f)

                // Vertical lines
                drawLine(
                    color = gridColor,
                    start = Offset(rect.left + thirdWidth, rect.top),
                    end = Offset(rect.left + thirdWidth, rect.bottom),
                    strokeWidth = 1f
                )
                drawLine(
                    color = gridColor,
                    start = Offset(rect.left + 2 * thirdWidth, rect.top),
                    end = Offset(rect.left + 2 * thirdWidth, rect.bottom),
                    strokeWidth = 1f
                )

                // Horizontal lines
                drawLine(
                    color = gridColor,
                    start = Offset(rect.left, rect.top + thirdHeight),
                    end = Offset(rect.right, rect.top + thirdHeight),
                    strokeWidth = 1f
                )
                drawLine(
                    color = gridColor,
                    start = Offset(rect.left, rect.top + 2 * thirdHeight),
                    end = Offset(rect.right, rect.top + 2 * thirdHeight),
                    strokeWidth = 1f
                )

                // Draw corner handles
                val handleRadius = handleRadiusPx
                val corners = listOf(
                    Offset(rect.left, rect.top),
                    Offset(rect.right, rect.top),
                    Offset(rect.left, rect.bottom),
                    Offset(rect.right, rect.bottom)
                )

                corners.forEach { corner ->
                    // Outer circle (white)
                    drawCircle(
                        color = borderColor,
                        radius = handleRadius,
                        center = corner
                    )
                    // Inner circle (accent)
                    drawCircle(
                        color = accentColor,
                        radius = handleRadius - 3f,
                        center = corner
                    )
                }
            }
        }
    }
}
