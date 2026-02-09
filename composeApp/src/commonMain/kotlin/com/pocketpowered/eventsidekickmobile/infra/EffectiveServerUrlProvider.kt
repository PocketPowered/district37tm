package com.district37.toastmasters.infra

/**
 * Provides the effective server URL based on developer settings.
 * This is determined once at app startup and cached for the session.
 * Changing the localhost setting requires an app restart.
 */
class EffectiveServerUrlProvider(
    private val useLocalhost: Boolean
) {
    /**
     * The effective GraphQL server URL for this session.
     * Returns localhost URL if enabled in dev settings (debug builds only),
     * otherwise returns the production server URL.
     */
    val serverUrl: String = if (useLocalhost && ServerConfig.isDebugBuild) {
        ServerConfig.localhostUrl
    } else {
        ServerConfig.serverUrl
    }

    /**
     * The effective base URL (without /graphql suffix) for REST API calls.
     */
    val baseUrl: String
        get() = if (serverUrl.endsWith("/graphql")) {
            serverUrl.removeSuffix("/graphql")
        } else {
            serverUrl
        }
}
