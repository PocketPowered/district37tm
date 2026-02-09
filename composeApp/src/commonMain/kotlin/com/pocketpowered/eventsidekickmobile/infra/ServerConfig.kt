package com.district37.toastmasters.infra

/**
 * Platform-specific server configuration
 * Provides the appropriate server URL for each platform and build variant
 */
expect object ServerConfig {
    /**
     * Get the primary GraphQL server URL for the current platform and build configuration
     */
    val serverUrl: String

    /**
     * Get the fallback server URL (typically production) to use if primary connection fails
     * Returns null if no fallback is available
     */
    val fallbackUrl: String?

    /**
     * Whether to enable automatic fallback to production server on connection failure
     * Typically enabled for debug builds, disabled for release
     */
    val enableFallback: Boolean

    /**
     * Whether this is a debug build
     * Used to enable developer features like the developer page
     */
    val isDebugBuild: Boolean

    /**
     * The localhost server URL for development testing
     * Platform-specific: Android emulator uses 10.0.2.2, iOS uses localhost
     */
    val localhostUrl: String
}
