package com.district37.toastmasters.infra

/**
 * iOS implementation of ServerConfig
 * For iOS, we use compile-time configuration
 *
 * To change the server URL for iOS:
 * - Development: Use your Mac's local IP address (e.g., http://192.168.1.100:8080/graphql)
 * - Production: Use the production Heroku URL
 *
 * Debug builds will try localhost first, then fallback to production if unreachable
 * Release builds connect directly to production with no fallback
 */
actual object ServerConfig {
    // Change this based on your environment
    // For local development on iOS simulator, use your Mac's IP address
    // Find it with: ifconfig | grep "inet " | grep -v 127.0.0.1
    private const val IS_PRODUCTION = false

    private const val DEV_SERVER_URL = "https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql"  // Change to your Mac's IP if needed
    private const val PROD_SERVER_URL = "https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql"

    actual val serverUrl: String = if (IS_PRODUCTION) PROD_SERVER_URL else DEV_SERVER_URL

    actual val fallbackUrl: String? = if (IS_PRODUCTION) {
        // Production builds have no fallback
        null
    } else {
        // Development builds fallback to production if localhost is unreachable
        PROD_SERVER_URL
    }

    actual val enableFallback: Boolean = !IS_PRODUCTION

    actual val isDebugBuild: Boolean = !IS_PRODUCTION

    actual val localhostUrl: String = "http://localhost:8080/graphql"
}
