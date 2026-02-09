package com.district37.toastmasters.models

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Result from the image selection wizard containing all necessary metadata
 * for creating or updating an image.
 */
data class ImageSelectionResult(
    /**
     * Raw image bytes from gallery selection.
     */
    val imageBytes: ByteArray,

    /**
     * Cached ImageBitmap for preview display.
     * Derived from imageBytes.
     */
    val imageBitmap: ImageBitmap?,

    /**
     * Optional focus region defining the important area of the image.
     * Used for smart cropping when displaying the image.
     * Coordinates are normalized (0-1 range).
     */
    val focusRegion: FocusRegion?,

    /**
     * Optional caption/description for the image.
     */
    val caption: String?
) {
    /**
     * Returns true if this result contains an image.
     */
    val hasImage: Boolean
        get() = true  // imageBytes is now required, so there's always an image

    /**
     * Returns true if this is a device-selected image (always true now).
     */
    val isDeviceImage: Boolean
        get() = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageSelectionResult

        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (focusRegion != other.focusRegion) return false
        if (caption != other.caption) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageBytes.contentHashCode()
        result = 31 * result + (focusRegion?.hashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        return result
    }
}
