package com.district37.toastmasters.infra

import com.district37.toastmasters.navigation.DeeplinkConfig

/**
 * Utility object for generating shareable deeplink URLs for various entities.
 * Uses web URLs that support Universal Links (iOS) and App Links (Android).
 * If the app is installed, the link opens in the app. Otherwise, it opens the web landing page.
 *
 * When a slug is available, it is preferred over the numeric ID for more readable URLs.
 */
object ShareUrlGenerator {
    private val BASE_URL = "https://${DeeplinkConfig.WEB_HOST}/"

    /**
     * Generate a shareable URL for an event
     * @param eventId The event ID
     * @param slug Optional human-readable slug (preferred over ID if available)
     * @return Web URL (e.g., "https://christopher-wong.com/event/tech-conference-2026")
     */
    fun generateEventUrl(eventId: Int, slug: String? = null): String =
        "${BASE_URL}event/${slug ?: eventId}"

    /**
     * Generate a shareable URL for a venue
     * @param venueId The venue ID
     * @param slug Optional human-readable slug (preferred over ID if available)
     * @return Web URL (e.g., "https://christopher-wong.com/venue/madison-square-garden")
     */
    fun generateVenueUrl(venueId: Int, slug: String? = null): String =
        "${BASE_URL}venue/${slug ?: venueId}"

    /**
     * Generate a shareable URL for a performer
     * @param performerId The performer ID
     * @param slug Optional human-readable slug (preferred over ID if available)
     * @return Web URL (e.g., "https://christopher-wong.com/performer/taylor-swift")
     */
    fun generatePerformerUrl(performerId: Int, slug: String? = null): String =
        "${BASE_URL}performer/${slug ?: performerId}"

    /**
     * Generate a shareable URL for an agenda item
     * @param agendaItemId The agenda item ID
     * @return Web URL (e.g., "https://christopher-wong.com/agenda-item/123")
     */
    fun generateAgendaItemUrl(agendaItemId: Int): String = "${BASE_URL}agenda-item/$agendaItemId"

    /**
     * Generate a shareable URL for a location
     * @param locationId The location ID
     * @param slug Optional human-readable slug (preferred over ID if available)
     * @return Web URL (e.g., "https://christopher-wong.com/location/new-york-city")
     */
    fun generateLocationUrl(locationId: Int, slug: String? = null): String =
        "${BASE_URL}location/${slug ?: locationId}"

    /**
     * Generate a shareable URL for a user profile
     * @param username The user's username
     * @return Web URL (e.g., "https://christopher-wong.com/profile/johndoe")
     */
    fun generateProfileUrl(username: String): String = "${BASE_URL}profile/$username"

    /**
     * Generate a shareable URL for an organization
     * @param organizationId The organization ID
     * @param slug Optional human-readable slug (preferred over ID if available)
     * @return Web URL (e.g., "https://christopher-wong.com/organization/acme-events")
     */
    fun generateOrganizationUrl(organizationId: Int, slug: String? = null): String =
        "${BASE_URL}organization/${slug ?: organizationId}"
}
