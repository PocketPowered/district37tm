package com.wongislandd.nexus.theming

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = ThemeColors,
        typography = PoppinsFont,
        content = content
    )
}