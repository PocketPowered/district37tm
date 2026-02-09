package com.district37.toastmasters.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Controller that manages TopAppBar state for the single root Scaffold.
 * Screens configure their TopAppBar by calling setConfig, and the root Scaffold
 * reads from this controller to render the appropriate TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
class TopAppBarController {
    private val _config = mutableStateOf<TopAppBarConfig>(AppBarConfigs.rootScreen())
    val config: State<TopAppBarConfig> = _config

    private val _onBackClick = mutableStateOf<(() -> Unit)?>(null)
    val onBackClick: State<(() -> Unit)?> = _onBackClick

    private val _scrollBehavior = mutableStateOf<TopAppBarScrollBehavior?>(null)
    val scrollBehavior: State<TopAppBarScrollBehavior?> = _scrollBehavior

    private val _insets = mutableStateOf(TopAppBarInsets())
    val insets: State<TopAppBarInsets> = _insets

    /**
     * Set the TopAppBar configuration for the current screen.
     * Only updates state if values have actually changed to avoid unnecessary recompositions.
     *
     * @param config The TopAppBar configuration type
     * @param onBackClick Callback for back button press (required if showBackButton is true)
     * @param scrollBehavior Optional scroll behavior for scroll-collapsing effect
     * @throws IllegalArgumentException if showBackButton is true but onBackClick is null
     */
    fun setConfig(
        config: TopAppBarConfig,
        onBackClick: (() -> Unit)? = null,
        scrollBehavior: TopAppBarScrollBehavior? = null
    ) {
        // Validation: ensure back button has a callback
        require(!config.showBackButton || onBackClick != null) {
            "onBackClick must be provided when showBackButton is true"
        }

        // Only update if config has changed (structural equality for data classes/objects)
        if (_config.value != config) {
            _config.value = config
        }

        // Always update callbacks and behavior (lambda comparison is unreliable)
        _onBackClick.value = onBackClick
        _scrollBehavior.value = scrollBehavior
    }

    /**
     * Update the measured insets (called by FloatingTopBar after measurement).
     * Only updates state if values have actually changed.
     */
    fun updateInsets(
        statusBarHeight: Dp = 0.dp,
        topBarContentHeight: Dp = 0.dp
    ) {
        val newInsets = _insets.value.copy(
            statusBarHeight = statusBarHeight,
            topBarContentHeight = topBarContentHeight
        )
        // Only update if insets have changed
        if (_insets.value != newInsets) {
            _insets.value = newInsets
        }
    }

    /**
     * Reset to default configuration (used when screen disposes)
     */
    fun reset() {
        _config.value = AppBarConfigs.rootScreen()
        _onBackClick.value = null
        _scrollBehavior.value = null
    }
}

/**
 * CompositionLocal for accessing the TopAppBarController from any screen
 */
val LocalTopAppBarController = staticCompositionLocalOf<TopAppBarController> {
    error("No TopAppBarController provided. Make sure MainScaffold provides it.")
}

/**
 * CompositionLocal for accessing top bar inset measurements
 * This allows components to adjust their layout to avoid being
 * obscured by the floating top bar
 */
val LocalTopAppBarInsets = staticCompositionLocalOf { TopAppBarInsets() }
