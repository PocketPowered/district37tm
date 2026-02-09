package com.district37.toastmasters.infra.calendar

/**
 * Platform-specific calendar deep-linking functionality.
 * Opens the native calendar app to view a synced event.
 */
expect class CalendarLauncher {
    /**
     * Open the native calendar app to view the specified event.
     * @param nativeEventId The platform-specific calendar event ID
     */
    fun openCalendarEvent(nativeEventId: String)
}
