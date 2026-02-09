package com.district37.toastmasters.navigation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contains measured inset information from the floating top bar system.
 * This allows content (especially hero images) to intelligently pad themselves
 * to avoid being obscured by the floating top bar and fade gradient.
 */
data class TopAppBarInsets(
    /**
     * Height of the system status bar (varies by platform/device)
     */
    val statusBarHeight: Dp = 0.dp,

    /**
     * Height of the top bar content (pills with back button, title, actions)
     * This is measured dynamically based on the actual rendered content
     */
    val topBarContentHeight: Dp = 0.dp,

    /**
     * Vertical padding applied around the top bar content
     * (8dp from FloatingTopBar implementation)
     */
    val topBarVerticalPadding: Dp = 8.dp,

    /**
     * Height of the fade gradient (140dp in MainScaffold)
     * Content should ideally be visible below this point
     */
    val fadeGradientHeight: Dp = 140.dp
) {
    /**
     * Total "safe" height where content starts to be fully visible
     * below the floating top bar system (status bar + content + padding)
     */
    val safeTopInset: Dp
        get() = statusBarHeight + topBarContentHeight + (topBarVerticalPadding * 2)

    /**
     * Recommended padding for hero images that should be visible below
     * the blur gradient (aligns with the 140dp gradient height)
     */
    val recommendedHeroPadding: Dp
        get() = fadeGradientHeight

    /**
     * Recommended top padding for scrollable content (LazyColumn, etc.)
     * Ensures content starts below the fade gradient's heavy opacity region.
     * Includes statusBarHeight to account for platform differences (iOS has taller status bars).
     * Use this instead of hardcoded padding values.
     */
    val recommendedContentPadding: Dp
        get() = statusBarHeight + (fadeGradientHeight - 60.dp)
}
