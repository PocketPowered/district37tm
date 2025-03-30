package com.district37.toastmasters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.wongislandd.nexus.theming.AppTheme

fun MainViewController() = ComposeUIViewController {
    AppTheme {
        App(
            modifier = Modifier.background(MaterialTheme.colors.primary)
                .safeDrawingPadding()
        )
    }
}