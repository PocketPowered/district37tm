package com.district37.toastmasters.infra

import com.district37.toastmasters.BuildConfig

/**
 * Android implementation of ServerConfig
 * Uses BuildConfig to get the appropriate server URL based on build variant
 *
 * Debug builds will try localhost first, then fallback to production if unreachable
 * Release builds connect directly to production with no fallback
 */
actual object ServerConfig {
    private const val PRODUCTION_URL = "https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql"

    actual val serverUrl: String = BuildConfig.SERVER_URL

    actual val fallbackUrl: String? = if (BuildConfig.DEBUG) {
        // Debug builds fallback to production if localhost is unreachable
        PRODUCTION_URL
    } else {
        // Release builds have no fallback (already on production)
        null
    }

    actual val enableFallback: Boolean = BuildConfig.DEBUG

    actual val isDebugBuild: Boolean = BuildConfig.DEBUG

    actual val localhostUrl: String = "http://10.0.2.2:8080/graphql"
}
