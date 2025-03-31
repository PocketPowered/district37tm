package com.wongislandd.nexus.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary and secondary colors for Toastmasters
val primaryColor = Color(0xFF003B5C) // Toastmasters Blue
val secondaryColor = Color(0xFFF5A800) // Toastmasters Gold

// Error color (keep as is, since Toastmasters doesn't specify error colors)
val errorColor = Color(0xFFE57373) // Soft Red for errors

// Background and text colors
val backgroundColor
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF121212) else Color(0xFF003B5C) // Dark gray for dark theme, white for light

val textColor
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFE0E0E0) else Color(0xFF212121) // Light gray for dark theme, dark gray for light

// Surface colors for light and dark themes
val lightSurfaceColor = Color(0xFFFFFFFF) // White for light theme
val darkSurfaceColor = Color(0xFF2F4F2F) // Dark gray for dark theme

val surfaceColor
    @Composable
    get() = if (isSystemInDarkTheme()) darkSurfaceColor else lightSurfaceColor

val ThemeColors
    @Composable
    get() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            background = backgroundColor,
            surface = surfaceColor,
            onSurface = textColor,
            onBackground = textColor,
            error = errorColor,
            onError = Color.White
        )
    } else {
        lightColors(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            background = backgroundColor,
            surface = surfaceColor,
            onSurface = textColor,
            onBackground = textColor,
            error = errorColor,
            onError = Color.White
        )
    }

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = ThemeColors,
        content = content
    )
}