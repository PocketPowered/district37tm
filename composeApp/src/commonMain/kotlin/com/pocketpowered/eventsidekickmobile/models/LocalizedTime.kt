package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * A time value with timezone context for client-side localization.
 *
 * Contains a UTC instant and the IANA timezone identifier (e.g., "America/New_York")
 * so the client can convert to local time for display.
 */
data class LocalizedTime(
    val instant: Instant,
    val timezone: String
)
