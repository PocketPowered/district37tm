package com.district37.toastmasters.infra.calendar

import kotlinx.datetime.Instant

/**
 * Result of attempting to match a server sync record to an existing calendar event.
 */
sealed class CalendarEventMatchResult {
    /** Event found by its original calendarEventId */
    data class FoundById(val calendarEventId: String) : CalendarEventMatchResult()

    /** Event found by matching title and time range */
    data class FoundByContent(val calendarEventId: String) : CalendarEventMatchResult()

    /** Event not found - needs to be re-synced */
    data object NotFound : CalendarEventMatchResult()

    /** Error during matching (e.g., permission denied) */
    data class Error(val message: String) : CalendarEventMatchResult()
}

/**
 * Data needed to match a calendar event.
 */
data class CalendarEventMatchCriteria(
    val originalCalendarEventId: String,
    val title: String,
    val startTime: Instant,
    val endTime: Instant?
)

/**
 * Platform-specific service for matching server sync records to existing calendar events.
 * Used during reinstall reconciliation to find and re-link existing calendar events.
 */
expect class CalendarEventMatcher {
    /**
     * Check if a calendar event exists by its ID.
     * @param calendarEventId The device calendar's event ID
     * @return true if the event exists
     */
    suspend fun eventExistsById(calendarEventId: String): Boolean

    /**
     * Find a calendar event by title and time range.
     * Uses a tolerance window to account for minor time differences.
     *
     * @param title The event title to search for (partial match)
     * @param startTime The expected start time
     * @param endTime The expected end time (optional)
     * @param toleranceMinutes How many minutes of variance to allow (default 5)
     * @return The calendarEventId if found, null otherwise
     */
    suspend fun findEventByContent(
        title: String,
        startTime: Instant,
        endTime: Instant?,
        toleranceMinutes: Int = 5
    ): String?

    /**
     * Attempt to match a sync record to an existing calendar event.
     * First tries by ID (fast), then falls back to content matching.
     *
     * @param criteria The matching criteria including original ID and content
     * @return Match result indicating how/if the event was found
     */
    suspend fun matchEvent(criteria: CalendarEventMatchCriteria): CalendarEventMatchResult
}
