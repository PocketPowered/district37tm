package com.district37.toastmasters.models

import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import kotlinx.datetime.Instant

/**
 * Focus region for image cropping.
 * All values are normalized (0-1) coordinates.
 * The region defines a bounding box where the important content is located.
 */
data class FocusRegion(
    val x: Float,       // Left edge position (0 = left, 1 = right)
    val y: Float,       // Top edge position (0 = top, 1 = bottom)
    val width: Float,   // Width of region (0-1)
    val height: Float   // Height of region (0-1)
) {
    /**
     * Calculate the center point of the focus region.
     */
    val centerX: Float get() = x + (width / 2f)
    val centerY: Float get() = y + (height / 2f)

    /**
     * Convert to Compose BiasAlignment for use with ContentScale.Crop.
     * Bias values: -1 = start/top, 0 = center, 1 = end/bottom
     */
    fun toBiasAlignment(): Alignment {
        // Convert from 0-1 range to -1 to 1 bias
        val horizontalBias = (centerX * 2f) - 1f
        val verticalBias = (centerY * 2f) - 1f
        return BiasAlignment(horizontalBias, verticalBias)
    }
}

/**
 * Domain model for Image
 */
data class Image(
    val id: Int,
    val url: String,
    val altText: String?,
    val caption: String?,
    val focusRegion: FocusRegion? = null,
    val archivedAt: Instant? = null
) {
    val isArchived: Boolean
        get() = archivedAt != null

    /**
     * Get alignment for image cropping based on focus region.
     * Falls back to center alignment if no focus region is set.
     */
    fun getCropAlignment(): Alignment {
        return focusRegion?.toBiasAlignment() ?: Alignment.Center
    }
}
