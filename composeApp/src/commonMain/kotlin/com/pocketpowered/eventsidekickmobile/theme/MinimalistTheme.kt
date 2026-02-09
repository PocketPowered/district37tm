package com.district37.toastmasters.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Minimalist Light Color Scheme
 *
 * Ultra-minimal color usage with heavy reliance on entity images for visual interest.
 * Pure white backgrounds with near-black text and subtle gray accents.
 */
val MinimalistLightColors = lightColorScheme(
    // Primary colors - subtle, not attention-grabbing
    primary = Color(0xFF1A1A1A),           // Near black for key elements
    onPrimary = Color(0xFFFFFFFF),         // Pure white
    primaryContainer = Color(0xFFF5F5F5),  // Very light gray for subtle containers
    onPrimaryContainer = Color(0xFF1A1A1A),

    // Secondary - barely there
    secondary = Color(0xFF4A4A4A),         // Medium gray
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFEFEF),
    onSecondaryContainer = Color(0xFF2A2A2A),

    // Tertiary - for accent moments only
    tertiary = Color(0xFF666666),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE8E8E8),
    onTertiaryContainer = Color(0xFF333333),

    // Error - minimal but clear
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    // Background - pure white for maximum content focus
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),

    // Surface - barely distinguishable from background
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),

    // Outline - extremely subtle
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5),

    // Inverse for dark overlays on images
    inverseSurface = Color(0xFF2A2A2A),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFE0E0E0),

    // Scrim - for overlays
    scrim = Color(0xFF000000),
)

/**
 * Minimalist Dark Color Scheme
 *
 * True black backgrounds for OLED optimization with light gray text.
 * Minimal color usage with subtle mid-tones.
 */
val MinimalistDarkColors = darkColorScheme(
    // Primary colors - light enough to read, dark enough to be minimal
    primary = Color(0xFFE8E8E8),           // Light gray for key elements
    onPrimary = Color(0xFF1A1A1A),         // Dark for contrast
    primaryContainer = Color(0xFF2A2A2A),  // Dark gray for containers
    onPrimaryContainer = Color(0xFFE8E8E8),

    // Secondary - subtle mid-tones
    secondary = Color(0xFFB8B8B8),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFD0D0D0),

    // Tertiary - for rare accent moments
    tertiary = Color(0xFF999999),
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF3A3A3A),
    onTertiaryContainer = Color(0xFFC0C0C0),

    // Error - visible but not jarring
    error = Color(0xFFEF5350),
    onError = Color(0xFF1A1A1A),
    errorContainer = Color(0xFF4A2626),
    onErrorContainer = Color(0xFFFFCDD2),

    // Background - true black for OLED optimization
    background = Color(0xFF000000),
    onBackground = Color(0xFFE8E8E8),

    // Surface - barely lighter than background
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFB0B0B0),

    // Outline - very subtle in dark mode
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF1A1A1A),

    // Inverse for light overlays on dark images
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF2A2A2A),

    // Scrim - for overlays
    scrim = Color(0xFF000000),
)
