package com.district37.toastmasters.util

import androidx.compose.runtime.Composable

/**
 * Cross-platform haptic feedback provider.
 * Provides tactile feedback for user interactions.
 *
 * @param view Platform-specific view object (Android View on Android, ignored on iOS)
 */
expect class HapticFeedback(view: Any?) {
    /**
     * Perform a long press haptic feedback.
     * This should be called when the user performs a long press gesture.
     */
    fun performLongPress()
}

/**
 * Remember a HapticFeedback instance for the current composition.
 * The implementation is platform-specific.
 */
@Composable
expect fun rememberHapticFeedback(): HapticFeedback
