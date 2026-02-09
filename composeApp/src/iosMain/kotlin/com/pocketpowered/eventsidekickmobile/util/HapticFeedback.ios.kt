package com.district37.toastmasters.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

/**
 * iOS implementation of HapticFeedback.
 * Uses UIImpactFeedbackGenerator with medium impact style.
 */
actual class HapticFeedback actual constructor(view: Any?) {
    // iOS doesn't need the view parameter, ignore it
    private val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)

    init {
        // Prepare the generator for faster response
        generator.prepare()
    }

    /**
     * Perform a long press haptic feedback using iOS impact feedback.
     */
    actual fun performLongPress() {
        generator.impactOccurred()
        // Prepare again for next use
        generator.prepare()
    }
}

/**
 * Remember a HapticFeedback instance for iOS.
 */
@Composable
actual fun rememberHapticFeedback(): HapticFeedback {
    return remember { HapticFeedback(null) }
}
