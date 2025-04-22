package com.wongislandd.nexus.theming

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialThemeColors,
        typography = PoppinsFont,
        content = content
    )
}