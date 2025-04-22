package com.wongislandd.nexus.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define new color variables for the updated theme
val LoyalBlue = Color(0xFF004165)
val TrueMaroon = Color(0xFF772432)
val CoolGray = Color(0xFFA9B2B1)
val HappyYellow = Color(0xFFF2DF74)
val TMRed = Color(0xFFCD202C)
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)

// Background and text colors
val backgroundColor
    @Composable
    get() = if (isSystemInDarkTheme()) PureBlack else PureWhite

val textColor
    @Composable
    get() = if (isSystemInDarkTheme()) PureWhite else PureBlack

// Surface colors for light and dark themes
val lightSurfaceColor = PureWhite
val darkSurfaceColor = Color(0xFF1E1E1E)

val surfaceColor
    @Composable
    get() = if (isSystemInDarkTheme()) darkSurfaceColor else lightSurfaceColor

val ThemeColors
    @Composable
    get() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = LoyalBlue,
            onPrimary = PureWhite,
            secondary = TrueMaroon,
            onSecondary = PureWhite,
            background = backgroundColor,
            surface = surfaceColor,
            onSurface = textColor,
            onBackground = textColor,
            error = TMRed,
            onError = PureWhite
        )
    } else {
        lightColors(
            primary = LoyalBlue,
            onPrimary = PureWhite,
            secondary = TrueMaroon,
            onSecondary = PureWhite,
            background = backgroundColor,
            surface = surfaceColor,
            onSurface = textColor,
            onBackground = textColor,
            error = TMRed,
            onError = PureWhite
        )
    }