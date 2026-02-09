package com.district37.toastmasters.navigation

import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages deeplink handling state for the application.
 * Receives deeplink URLs from platform-specific code and stores them
 * until the navigation system is ready to process them.
 */
class DeeplinkHandler {
    private val TAG = "DeeplinkHandler"

    private val _pendingDeeplink = MutableStateFlow<DeeplinkDestination?>(null)
    val pendingDeeplink: StateFlow<DeeplinkDestination?> = _pendingDeeplink.asStateFlow()

    /**
     * Handle an incoming deeplink URL
     * @param url The deeplink URL to process
     */
    fun handleDeeplink(url: String) {
        Logger.d(TAG, "Handling deeplink: $url")

        val destination = DeeplinkParser.parse(url)

        when (destination) {
            is DeeplinkDestination.OAuthCallback -> {
                // Delegate to existing OAuth callback handler
                Logger.d(TAG, "Delegating to OAuth callback handler")
                // Extract URI from the callback destination
                // The OAuthCallbackHandler expects a Uri, but we'll handle this in platform code
            }
            is DeeplinkDestination.Unknown -> {
                Logger.i(TAG, "Ignoring unknown deeplink: $url")
            }
            else -> {
                Logger.d(TAG, "Setting pending deeplink: $destination")
                _pendingDeeplink.value = destination
            }
        }
    }

    /**
     * Clear the currently pending deeplink after it has been processed
     */
    fun clearPendingDeeplink() {
        Logger.d(TAG, "Clearing pending deeplink")
        _pendingDeeplink.value = null
    }

    /**
     * Set a destination directly (for in-app navigation across tabs).
     * This allows components in other tabs to navigate to screens in the Explore tab.
     * @param destination The destination to navigate to
     */
    fun setDestination(destination: DeeplinkDestination) {
        Logger.d(TAG, "Setting destination: $destination")
        _pendingDeeplink.value = destination
    }
}
