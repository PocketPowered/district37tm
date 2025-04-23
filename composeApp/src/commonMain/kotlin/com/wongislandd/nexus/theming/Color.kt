package com.wongislandd.nexus.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Background and text colors
//val backgroundColor
//    @Composable
//    get() = if (isSystemInDarkTheme()) backgroundLight else backgroundDark
//
//val textColor
//    @Composable
//    get() = if (isSystemInDarkTheme()) onPrimaryDark else onPrimaryLight
//
//val surfaceColor
//    @Composable
//    get() = if (isSystemInDarkTheme()) surfaceDark else surfaceLight

val MaterialThemeColors
    @Composable
    get() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = primaryDark,
            onPrimary = onPrimaryDark,
            secondary = secondaryDark,
            onSecondary = onSecondaryDark,
            error = errorDark,
            onError = onErrorDark,
            background = backgroundDark,
            onBackground = onBackgroundDark,
            surface = surfaceDark,
            onSurface = onSurfaceDark,
        )
    } else {
        lightColors(
            primary = primaryLight,
            onPrimary = onPrimaryLight,
            secondary = secondaryLight,
            onSecondary = onSecondaryLight,
            error = errorLight,
            onError = onErrorLight,
            background = backgroundLight,
            onBackground = onBackgroundLight,
            surface = surfaceLight,
            onSurface = onSurfaceLight,
        )
    }

private val primaryLight = Color(0xFF004165)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFF88929D)
private val onPrimaryContainerLight = Color(0xFFFFFFFF)
private val secondaryLight = Color(0xFF772432)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFFFDADB)
private val onSecondaryContainerLight = Color(0xFF5C3F41)
private val tertiaryLight = Color(0xFF775930)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFFDDB5)
private val onTertiaryContainerLight = Color(0xFF5D411B)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFF8F7)
private val onBackgroundLight = Color(0xFF22191A)
private val surfaceLight = Color(0xFFFFF8F7)
private val onSurfaceLight = Color(0xFF22191A)
private val surfaceVariantLight = Color(0xFFF4DDDE)
private val onSurfaceVariantLight = Color(0xFF524344)
private val outlineLight = Color(0xFF857374)
private val outlineVariantLight = Color(0xFFD7C1C2)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF382E2E)
private val inverseOnSurfaceLight = Color(0xFFFFEDED)
private val inversePrimaryLight = Color(0xFFFFB2B9)
private val surfaceDimLight = Color(0xFFE7D6D6)
private val surfaceBrightLight = Color(0xFFFFF8F7)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFFF0F0)
private val surfaceContainerLight = Color(0xFFFCEAEA)
private val surfaceContainerHighLight = Color(0xFFF6E4E4)
private val surfaceContainerHighestLight = Color(0xFFF0DEDF)

private val primaryDark = Color(0xFFFFB2B9)
private val onPrimaryDark = Color(0xFF561D25)
private val primaryContainerDark = Color(0xFF72333B)
private val onPrimaryContainerDark = Color(0xFFFFDADB)
private val secondaryDark = Color(0xFFE5BDBF)
private val onSecondaryDark = Color(0xFF44292C)
private val secondaryContainerDark = Color(0xFF5C3F41)
private val onSecondaryContainerDark = Color(0xFFFFDADB)
private val tertiaryDark = Color(0xFFE8C08E)
private val onTertiaryDark = Color(0xFF442B06)
private val tertiaryContainerDark = Color(0xFF5D411B)
private val onTertiaryContainerDark = Color(0xFFFFDDB5)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF1A1112)
private val onBackgroundDark = Color(0xFFF0DEDF)
private val surfaceDark = Color(0xFF1E1E1E)
private val onSurfaceDark = Color(0xFFF0DEDF)
private val surfaceVariantDark = Color(0xFF524344)
private val onSurfaceVariantDark = Color(0xFFD7C1C2)
private val outlineDark = Color(0xFF9F8C8D)
private val outlineVariantDark = Color(0xFF524344)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF0DEDF)
private val inverseOnSurfaceDark = Color(0xFF382E2E)
private val inversePrimaryDark = Color(0xFF8F4951)
private val surfaceDimDark = Color(0xFF1A1112)
private val surfaceBrightDark = Color(0xFF413737)
private val surfaceContainerLowestDark = Color(0xFF140C0D)
private val surfaceContainerLowDark = Color(0xFF22191A)
private val surfaceContainerDark = Color(0xFF271D1E)
private val surfaceContainerHighDark = Color(0xFF312828)
private val surfaceContainerHighestDark = Color(0xFF3D3233)