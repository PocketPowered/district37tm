package com.district37.toastmasters.util

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Converts a ByteArray to an ImageBitmap for display in Compose.
 * This replaces the Peekaboo library's toImageBitmap() extension function.
 *
 * @return ImageBitmap representation of the byte array, or null if conversion fails
 */
fun ByteArray.toImageBitmap(): ImageBitmap? {
    return try {
        this.decodeToImageBitmap()
    } catch (e: Exception) {
        null
    }
}
