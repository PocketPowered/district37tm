package com.district37.toastmasters.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contains measured inset information from the floating bottom nav system.
 * This allows content to intelligently pad themselves to avoid being
 * obscured by the floating bottom navigation bar.
 */
data class BottomNavInsets(
    /**
     * Height of the bottom navigation bar content (pills with icons)
     * Based on FloatingBottomNav implementation: 8dp internal padding * 2 + touch target
     */
    val bottomNavContentHeight: Dp = 56.dp,

    /**
     * Vertical padding applied around the bottom nav (16dp from FloatingBottomNav)
     */
    val bottomNavVerticalPadding: Dp = 16.dp,

    /**
     * Extra margin for safety (ensures content doesn't get too close)
     */
    val safetyMargin: Dp = 8.dp
) {
    /**
     * Total height of the floating bottom nav including padding.
     * Use this when you need the exact space taken by the bottom nav.
     */
    val totalBottomNavHeight: Dp
        get() = bottomNavContentHeight + (bottomNavVerticalPadding * 2)

    /**
     * Recommended bottom padding for scrollable content (LazyColumn, ScrollableColumn, etc.)
     * Ensures content can scroll above the floating bottom nav.
     * Use this instead of hardcoded padding values.
     */
    val recommendedContentPadding: Dp
        get() = totalBottomNavHeight + safetyMargin
}

/**
 * CompositionLocal for providing BottomNavInsets to the composition tree.
 * Screens can use this to add appropriate bottom padding to avoid content
 * being obscured by the floating bottom navigation bar.
 */
val LocalBottomNavInsets = staticCompositionLocalOf { BottomNavInsets() }
