package com.district37.toastmasters.navigation

import com.district37.toastmasters.util.Logger

/**
 * Parser for deeplink URLs that maps URL strings to DeeplinkDestination objects.
 * Supports both app scheme (eventsidekick://) and future https:// Universal Links.
 */
object DeeplinkParser {
    private const val TAG = "DeeplinkParser"
    private const val OAUTH_HOST = "auth-callback"

    /**
     * Parse a URL string into a DeeplinkDestination
     * @param url The URL to parse (e.g., "eventsidekick://event/123" or "https://eventsidekick.app/event/123")
     * @return A DeeplinkDestination representing the parsed URL
     */
    fun parse(url: String): DeeplinkDestination {
        try {
            Logger.d(TAG, "Parsing deeplink: $url")

            // Extract path from URL
            val parts = parseUrl(url)
            val scheme = parts.scheme
            val host = parts.host
            val pathSegments = parts.pathSegments

            // Check for OAuth callback (existing pattern)
            if (scheme == DeeplinkConfig.APP_SCHEME && host == OAUTH_HOST) {
                Logger.d(TAG, "Recognized OAuth callback")
                return DeeplinkDestination.OAuthCallback(url)
            }

            // Check for my-requests deep link (opens profile with Requests tab)
            // Supports both app scheme (eventsidekick://my-requests) and web URL (https://christopher-wong.com/my-requests)
            val isAppSchemeMyRequests = scheme == DeeplinkConfig.APP_SCHEME && host == "my-requests" && pathSegments.isEmpty()
            val isWebUrlMyRequests = scheme == "https" && host == DeeplinkConfig.WEB_HOST && pathSegments.size == 1 && pathSegments[0] == "my-requests"
            if (isAppSchemeMyRequests || isWebUrlMyRequests) {
                Logger.d(TAG, "Recognized my-requests deeplink")
                return DeeplinkDestination.MyRequests
            }

            // Parse entity deeplinks
            // Format 1: eventsidekick://event/123 (entity type as host)
            // Format 2: https://christopher-wong.com/event/123 (entity type in path)
            val (entityType, identifier) = when {
                // Format 1: entity type is the host (e.g., eventsidekick://event/123)
                pathSegments.size == 1 && scheme == DeeplinkConfig.APP_SCHEME -> {
                    host to pathSegments[0]
                }
                // Format 2: entity type is in path (e.g., https://christopher-wong.com/event/123)
                pathSegments.size >= 2 && (scheme == "https" && host == DeeplinkConfig.WEB_HOST) -> {
                    pathSegments[0] to pathSegments[1]
                }
                else -> null to null
            }

            if (entityType != null && identifier != null) {
                val destination = when (entityType) {
                    // These entity types support both numeric IDs and slugs
                    "event" -> DeeplinkDestination.Event(identifier)
                    "venue" -> DeeplinkDestination.Venue(identifier)
                    "performer" -> DeeplinkDestination.Performer(identifier)
                    "location" -> DeeplinkDestination.Location(identifier)
                    "organization" -> DeeplinkDestination.Organization(identifier)
                    // Agenda items only support numeric IDs
                    "agenda-item" -> identifier.toIntOrNull()?.let {
                        DeeplinkDestination.AgendaItem(it)
                    }
                    // Profile uses username strings
                    "profile" -> DeeplinkDestination.Profile(identifier)
                    else -> null
                }

                if (destination != null) {
                    Logger.d(TAG, "Successfully parsed: $entityType -> $destination")
                    return destination
                }
            }

            Logger.i(TAG, "Unrecognized deeplink format: $url")
            return DeeplinkDestination.Unknown
        } catch (e: Exception) {
            Logger.e(TAG, "Error parsing deeplink: ${e.message}")
            return DeeplinkDestination.Unknown
        }
    }

    /**
     * Simple URL parser that extracts scheme, host, and path segments
     */
    private fun parseUrl(url: String): UrlParts {
        // Extract scheme
        val schemeEnd = url.indexOf("://")
        if (schemeEnd == -1) {
            return UrlParts("", "", emptyList())
        }

        val scheme = url.substring(0, schemeEnd)
        val remainder = url.substring(schemeEnd + 3)

        // Extract host and path
        val pathStart = remainder.indexOf('/')
        val (host, path) = if (pathStart == -1) {
            remainder to ""
        } else {
            remainder.substring(0, pathStart) to remainder.substring(pathStart)
        }

        // Extract path segments
        val pathSegments = path.split('/')
            .filter { it.isNotEmpty() }

        return UrlParts(scheme, host, pathSegments)
    }

    private data class UrlParts(
        val scheme: String,
        val host: String,
        val pathSegments: List<String>
    )
}
