package com.district37.toastmasters.infra

/**
 * OAuth callback URL scheme and host for deep linking
 */
const val OAUTH_CALLBACK_SCHEME = "eventsidekick"
const val OAUTH_CALLBACK_HOST = "auth-callback"
const val OAUTH_REDIRECT_URL = "$OAUTH_CALLBACK_SCHEME://$OAUTH_CALLBACK_HOST"

/**
 * Platform-specific OAuth browser launcher.
 * Opens an OAuth URL in a secure browser and captures the callback.
 *
 * - Android: Uses Chrome Custom Tabs with deep link callback
 * - iOS: Uses ASWebAuthenticationSession
 */
expect class OAuthBrowser {
    /**
     * Launch OAuth flow in browser and return the callback URL.
     *
     * @param url The OAuth authorization URL to open
     * @return Result containing the callback URL with tokens, or failure
     */
    suspend fun launchOAuth(url: String): Result<String>
}
