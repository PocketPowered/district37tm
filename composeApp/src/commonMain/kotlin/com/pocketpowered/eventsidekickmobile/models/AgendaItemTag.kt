package com.district37.toastmasters.models

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Enum representing the different tags that can be applied to agenda items.
 * Each tag defines its own visual styling through the [AgendaItemTagStyle].
 */
enum class AgendaItemTag {
    HIGHLIGHTED,
    SPECIAL,
    LIMITED;

    companion object {
        /**
         * Safely parse a string to an AgendaItemTag, returning null if not recognized
         */
        fun fromString(value: String?): AgendaItemTag? {
            if (value == null) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Defines the visual styling for an agenda item tag.
 * This sealed class allows for flexible, type-safe styling that can be easily extended.
 */
sealed class AgendaItemTagStyle {
    /**
     * No special styling - default appearance
     */
    data object None : AgendaItemTagStyle()

    /**
     * Gradient background effect
     * @param brush The gradient brush to apply as the background
     * @param contentColor Optional override for text/content color on this background
     */
    data class GradientBackground(
        val brush: Brush,
        val contentColor: Color? = null
    ) : AgendaItemTagStyle()

    /**
     * Gradient border effect
     * @param brush The gradient brush to apply to the border
     * @param borderWidth The width of the border in dp
     */
    data class GradientBorder(
        val brush: Brush,
        val borderWidth: Float = 2f
    ) : AgendaItemTagStyle()

    /**
     * Accent indicator (left edge highlight)
     * @param color The color for the accent indicator
     * @param width The width of the indicator in dp
     */
    data class AccentIndicator(
        val color: Color,
        val width: Float = 4f
    ) : AgendaItemTagStyle()

    /**
     * Animated shimmer effect overlay
     * @param colors The gradient colors for the shimmer animation
     */
    data class Shimmer(
        val colors: List<Color>
    ) : AgendaItemTagStyle()

    companion object {
        // Gold/amber colors for highlighted items
        private val shimmerGoldDark = Color(0xFFB8860B)
        private val shimmerGoldLight = Color(0xFFF2DF74)

        /**
         * Get the visual style for a given tag
         */
        fun forTag(tag: AgendaItemTag?): AgendaItemTagStyle {
            return when (tag) {
                AgendaItemTag.HIGHLIGHTED -> Shimmer(
                    colors = listOf(
                        shimmerGoldDark.copy(alpha = 0.2f),
                        shimmerGoldLight.copy(alpha = 0.4f),
                        shimmerGoldDark.copy(alpha = 0.2f)
                    )
                )
                AgendaItemTag.SPECIAL -> None // TODO: Implement special styling
                AgendaItemTag.LIMITED -> None // TODO: Implement limited styling
                null -> None
            }
        }
    }
}
