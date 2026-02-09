package com.district37.toastmasters.navigation

/**
 * Configuration for deeplink URL handling.
 * Centralizes URL scheme and host constants for both parsing incoming deeplinks
 * and generating shareable URLs.
 */
object DeeplinkConfig {
    /**
     * The custom app URL scheme (e.g., eventsidekick://event/123)
     */
    const val APP_SCHEME = "eventsidekick"

    /**
     * The web host for Universal Links (iOS) and App Links (Android).
     * URLs like https://christopher-wong.com/event/123 will open in the app if installed.
     * This is configurable as the domain may change.
     */
    const val WEB_HOST = "christopher-wong.com"
}
