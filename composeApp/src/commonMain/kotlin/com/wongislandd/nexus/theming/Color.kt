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

//val ThemeColors
//    @Composable
//    get() = if (isSystemInDarkTheme()) {
//        darkColors(
//            primary = LoyalBlue,
//            onPrimary = PureWhite,
//            secondary = TrueMaroon,
//            onSecondary = PureWhite,
//            background = backgroundColor,
//            surface = surfaceColor,
//            onSurface = textColor,
//            onBackground = textColor,
//            error = TMRed,
//            onError = PureWhite
//        )
//    } else {
//        lightColors(
//            primary = LoyalBlue,
//            onPrimary = PureWhite,
//            secondary = TrueMaroon,
//            onSecondary = PureWhite,
//            background = backgroundColor,
//            surface = surfaceColor,
//            onSurface = textColor,
//            onBackground = textColor,
//            error = TMRed,
//            onError = PureWhite
//        )
//    }

val MaterialThemeColors
    @Composable
    get() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = primaryDark,
            onPrimary = onPrimaryDark,
//            primaryContainer = primaryContainerDark,
//            onPrimaryContainer = onPrimaryContainerDark,
            secondary = secondaryDark,
            onSecondary = onSecondaryDark,
//            secondaryContainer = secondaryContainerDark,
//            onSecondaryContainer = onSecondaryContainerDark,
//            tertiary = tertiaryDark,
//            onTertiary = onTertiaryDark,
//            tertiaryContainer = tertiaryContainerDark,
//            onTertiaryContainer = onTertiaryContainerDark,
            error = errorDark,
            onError = onErrorDark,
//            errorContainer = errorContainerDark,
//            onErrorContainer = onErrorContainerDark,
            background = backgroundDark,
            onBackground = onBackgroundDark,
            surface = surfaceDark,
            onSurface = onSurfaceDark,
//            surfaceVariant = surfaceVariantDark,
//            onSurfaceVariant = onSurfaceVariantDark,
//            outline = outlineDark,
//            outlineVariant = outlineVariantDark,
//            scrim = scrimDark,
//            inverseSurface = inverseSurfaceDark,
//            inverseOnSurface = inverseOnSurfaceDark,
//            inversePrimary = inversePrimaryDark,
//            surfaceDim = surfaceDimDark,
//            surfaceBright = surfaceBrightDark,
//            surfaceContainerLowest = surfaceContainerLowestDark,
//            surfaceContainerLow = surfaceContainerLowDark,
//            surfaceContainer = surfaceContainerDark,
//            surfaceContainerHigh = surfaceContainerHighDark,
//            surfaceContainerHighest = surfaceContainerHighestDark,
        )
    } else {
        lightColors(
            primary = primaryLight,
            onPrimary = onPrimaryLight,
//            primaryContainer = primaryContainerLight,
//            onPrimaryContainer = onPrimaryContainerLight,
            secondary = secondaryLight,
            onSecondary = onSecondaryLight,
//            secondaryContainer = secondaryContainerLight,
//            onSecondaryContainer = onSecondaryContainerLight,
//            tertiary = tertiaryLight,
//            onTertiary = onTertiaryLight,
//            tertiaryContainer = tertiaryContainerLight,
//            onTertiaryContainer = onTertiaryContainerLight,
            error = errorLight,
            onError = onErrorLight,
//            errorContainer = errorContainerLight,
//            onErrorContainer = onErrorContainerLight,
            background = backgroundLight,
            onBackground = onBackgroundLight,
            surface = surfaceLight,
            onSurface = onSurfaceLight,
//            surfaceVariant = surfaceVariantLight,
//            onSurfaceVariant = onSurfaceVariantLight,
//            outline = outlineLight,
//            outlineVariant = outlineVariantLight,
//            scrim = scrimLight,
//            inverseSurface = inverseSurfaceLight,
//            inverseOnSurface = inverseOnSurfaceLight,
//            inversePrimary = inversePrimaryLight,
//            surfaceDim = surfaceDimLight,
//            surfaceBright = surfaceBrightLight,
//            surfaceContainerLowest = surfaceContainerLowestLight,
//            surfaceContainerLow = surfaceContainerLowLight,
//            surfaceContainer = surfaceContainerLight,
//            surfaceContainerHigh = surfaceContainerHighLight,
//            surfaceContainerHighest = surfaceContainerHighestLight,
        )
    }

val primaryLight = Color(0xFF8F4951)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDADB)
val onPrimaryContainerLight = Color(0xFF72333B)
val secondaryLight = Color(0xFF765659)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFDADB)
val onSecondaryContainerLight = Color(0xFF5C3F41)
val tertiaryLight = Color(0xFF775930)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFDDB5)
val onTertiaryContainerLight = Color(0xFF5D411B)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFFF8F7)
val onBackgroundLight = Color(0xFF22191A)
val surfaceLight = Color(0xFFFFF8F7)
val onSurfaceLight = Color(0xFF22191A)
val surfaceVariantLight = Color(0xFFF4DDDE)
val onSurfaceVariantLight = Color(0xFF524344)
val outlineLight = Color(0xFF857374)
val outlineVariantLight = Color(0xFFD7C1C2)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF382E2E)
val inverseOnSurfaceLight = Color(0xFFFFEDED)
val inversePrimaryLight = Color(0xFFFFB2B9)
val surfaceDimLight = Color(0xFFE7D6D6)
val surfaceBrightLight = Color(0xFFFFF8F7)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF0F0)
val surfaceContainerLight = Color(0xFFFCEAEA)
val surfaceContainerHighLight = Color(0xFFF6E4E4)
val surfaceContainerHighestLight = Color(0xFFF0DEDF)

val primaryDark = Color(0xFFFFB2B9)
val onPrimaryDark = Color(0xFF561D25)
val primaryContainerDark = Color(0xFF72333B)
val onPrimaryContainerDark = Color(0xFFFFDADB)
val secondaryDark = Color(0xFFE5BDBF)
val onSecondaryDark = Color(0xFF44292C)
val secondaryContainerDark = Color(0xFF5C3F41)
val onSecondaryContainerDark = Color(0xFFFFDADB)
val tertiaryDark = Color(0xFFE8C08E)
val onTertiaryDark = Color(0xFF442B06)
val tertiaryContainerDark = Color(0xFF5D411B)
val onTertiaryContainerDark = Color(0xFFFFDDB5)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF1A1112)
val onBackgroundDark = Color(0xFFF0DEDF)
val surfaceDark = Color(0xFF1A1112)
val onSurfaceDark = Color(0xFFF0DEDF)
val surfaceVariantDark = Color(0xFF524344)
val onSurfaceVariantDark = Color(0xFFD7C1C2)
val outlineDark = Color(0xFF9F8C8D)
val outlineVariantDark = Color(0xFF524344)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF0DEDF)
val inverseOnSurfaceDark = Color(0xFF382E2E)
val inversePrimaryDark = Color(0xFF8F4951)
val surfaceDimDark = Color(0xFF1A1112)
val surfaceBrightDark = Color(0xFF413737)
val surfaceContainerLowestDark = Color(0xFF140C0D)
val surfaceContainerLowDark = Color(0xFF22191A)
val surfaceContainerDark = Color(0xFF271D1E)
val surfaceContainerHighDark = Color(0xFF312828)
val surfaceContainerHighestDark = Color(0xFF3D3233)