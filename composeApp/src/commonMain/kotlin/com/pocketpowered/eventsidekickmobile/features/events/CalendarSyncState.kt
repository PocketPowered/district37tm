package com.district37.toastmasters.features.events

import com.district37.toastmasters.infra.calendar.CalendarInfo

/**
 * Consolidated state for calendar sync functionality.
 *
 * This groups all calendar-related state into a single data class,
 * making state management cleaner and reducing the number of StateFlows
 * in ViewModels that handle calendar sync.
 *
 * Usage:
 * ```kotlin
 * private val _calendarState = MutableStateFlow(CalendarSyncState())
 * val calendarState: StateFlow<CalendarSyncState> = _calendarState.asStateFlow()
 *
 * // Update state:
 * _calendarState.update { it.copy(isEventSynced = true) }
 * ```
 */
data class CalendarSyncState(
    /** Whether the event is currently synced to the device calendar */
    val isEventSynced: Boolean = false,

    /** Whether a calendar sync operation is in progress */
    val isSyncing: Boolean = false,

    /** List of available calendars on the device */
    val availableCalendars: List<CalendarInfo> = emptyList(),

    /** Whether calendars are currently being loaded */
    val isLoadingCalendars: Boolean = false,

    /** Error message from the last calendar operation, if any */
    val error: String? = null,

    /** Whether the calendar picker dialog should be shown */
    val showCalendarPicker: Boolean = false,

    /** Whether we need to request calendar permission from the user */
    val needsPermission: Boolean = false
) {
    /** Whether any calendar operation is in progress */
    val isOperationInProgress: Boolean
        get() = isSyncing || isLoadingCalendars

    /** Whether there's an error to display */
    val hasError: Boolean
        get() = error != null

    companion object {
        /** Initial state with all defaults */
        val Initial = CalendarSyncState()
    }
}

/**
 * Consolidated state for bulk calendar sync operations (e.g., "Sync My Schedule").
 */
data class BulkSyncState(
    /** Whether a bulk sync operation is in progress */
    val isSyncing: Boolean = false,

    /** Result message from the last bulk sync operation */
    val resultMessage: String? = null
) {
    companion object {
        val Initial = BulkSyncState()
    }
}
