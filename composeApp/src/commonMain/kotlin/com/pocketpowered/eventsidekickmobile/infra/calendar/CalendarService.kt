package com.district37.toastmasters.infra.calendar

import kotlinx.datetime.Instant

/**
 * Information about a calendar available on the device.
 */
data class CalendarInfo(
    val id: String,
    val name: String,
    val isPrimary: Boolean,
    val accountName: String?,
    val color: Int?
)

/**
 * Data needed to create a calendar event.
 * Can represent either a top-level event or an agenda item within an event.
 */
data class CalendarEventData(
    val eventId: Int,
    val agendaItemId: Int? = null, // If set, this is an agenda item sync; otherwise event sync
    val title: String,
    val description: String?,
    val startTime: Instant,
    val endTime: Instant?,
    val timezone: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * Result of a calendar operation.
 */
sealed class CalendarResult<out T> {
    data class Success<T>(val data: T) : CalendarResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : CalendarResult<Nothing>()
    data object PermissionDenied : CalendarResult<Nothing>()
}

/**
 * Platform-specific calendar functionality.
 * Provides access to device calendars for syncing events.
 */
expect class CalendarService {
    /**
     * Get available calendars on the device.
     * @return List of calendars or error
     */
    suspend fun getAvailableCalendars(): CalendarResult<List<CalendarInfo>>

    /**
     * Check if the app has calendar permissions.
     * @return true if permissions are granted
     */
    suspend fun hasCalendarPermission(): Boolean

    /**
     * Add an event to a specific calendar.
     * @param calendarId The ID of the calendar to add to
     * @param event The event data to add
     * @return The calendar event ID if successful, or error
     */
    suspend fun addEvent(calendarId: String, event: CalendarEventData): CalendarResult<String>

    /**
     * Update an existing calendar event.
     * @param calendarEventId The ID of the calendar event to update
     * @param event The updated event data
     * @return Success or error
     */
    suspend fun updateEvent(calendarEventId: String, event: CalendarEventData): CalendarResult<Unit>

    /**
     * Delete a calendar event.
     * @param calendarEventId The ID of the calendar event to delete
     * @return Success or error
     */
    suspend fun deleteEvent(calendarEventId: String): CalendarResult<Unit>

    /**
     * Get the start date of a calendar event.
     * Used by CalendarLauncher to navigate to the correct date.
     * @param calendarEventId The ID of the calendar event
     * @return The start date as Instant, or null if not found
     */
    suspend fun getEventStartDate(calendarEventId: String): Instant?
}
