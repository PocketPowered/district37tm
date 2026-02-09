package com.district37.toastmasters.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Android implementation of HapticFeedback.
 * Uses the Android View's performHapticFeedback API.
 */
actual class HapticFeedback actual constructor(view: Any?) {
    private val androidView: View? = view as? View

    /**
     * Perform a long press haptic feedback using Android's LONG_PRESS constant.
     */
    actual fun performLongPress() {
        androidView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * Remember a HapticFeedback instance for Android.
 * Uses the current composition's LocalView to access the Android view.
 */
@Composable
actual fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HapticFeedback(view) }
}
