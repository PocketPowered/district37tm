package com.district37.toastmasters.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Helper composable that screens use to configure the root TopAppBar.
 * Call this at the top of your screen composable to set up the TopAppBar.
 *
 * Uses LaunchedEffect to update the config only when it changes, avoiding
 * unnecessary state updates while ensuring proper TopAppBar state during
 * navigation transitions.
 *
 * @param config The TopAppBar configuration for this screen
 * @param onBackClick Callback for back button press (required for screens with back button)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureTopAppBar(
    config: TopAppBarConfig,
    onBackClick: (() -> Unit)? = null
) {
    val controller = LocalTopAppBarController.current

    // Remember scroll behavior to avoid recreation on every composition
    val scrollBehavior = rememberTopAppBarScrollBehaviorIfNeeded(config.enableScrollCollapse)

    // Use LaunchedEffect with config as key - only runs when config/callback changes
    // This avoids unnecessary state updates while still handling navigation properly
    LaunchedEffect(config, onBackClick) {
        controller.setConfig(config, onBackClick, scrollBehavior)
    }
}

/**
 * Remembers a TopAppBarScrollBehavior only when needed.
 * Returns null if scroll behavior is not required.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberTopAppBarScrollBehaviorIfNeeded(
    needed: Boolean
): TopAppBarScrollBehavior? {
    return if (needed) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(
            state = rememberTopAppBarState()
        )
    } else {
        null
    }
}

/**
 * Returns a Modifier with nestedScroll connection if scroll behavior is active.
 * Use this on your scrollable content (LazyColumn, etc.) to enable scroll-collapsing.
 *
 * Usage:
 * ```
 * val scrollModifier = rememberTopAppBarScrollModifier()
 * LazyColumn(modifier = Modifier.then(scrollModifier)) { ... }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTopAppBarScrollModifier(): Modifier {
    val controller = LocalTopAppBarController.current
    val scrollBehavior by controller.scrollBehavior

    return scrollBehavior?.let {
        Modifier.nestedScroll(it.nestedScrollConnection)
    } ?: Modifier
}
