package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.data.repository.AgendaItemSyncRepository
import com.district37.toastmasters.data.repository.EventSyncRepository
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemSyncStatus
import com.district37.toastmasters.models.EventSyncStatus
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.PlatformType
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.currentPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * Represents a local sync record tracking which events have been synced to the calendar.
 * This will later be persisted to the server.
 */
data class LocalCalendarSyncRecord(
    val eventId: Int,
    val calendarEventId: String,
    val calendarId: String,
    val title: String,
    val startTime: Instant?
)

/**
 * Represents a local sync record for agenda items.
 */
data class LocalAgendaItemSyncRecord(
    val agendaItemId: Int,
    val eventId: Int,
    val calendarEventId: String,
    val calendarId: String,
    val title: String,
    val startTime: Instant?
)

/**
 * Manages calendar synchronization for events and agenda items.
 * Orchestrates between the platform CalendarService and server-side sync tracking.
 */
class CalendarSyncManager(
    private val calendarService: CalendarService,
    private val agendaItemSyncRepository: AgendaItemSyncRepository,
    private val eventSyncRepository: EventSyncRepository
) {
    private val TAG = "CalendarSyncManager"

    /**
     * Convert PlatformType to Platform for server API calls.
     */
    private fun getCurrentPlatform(): Platform {
        return when (currentPlatform) {
            PlatformType.ANDROID -> Platform.ANDROID
            PlatformType.IOS -> Platform.IOS
        }
    }

    // In-memory cache of synced events (will be replaced with server sync)
    private val _syncedEvents = MutableStateFlow<Map<Int, LocalCalendarSyncRecord>>(emptyMap())
    val syncedEvents: StateFlow<Map<Int, LocalCalendarSyncRecord>> = _syncedEvents.asStateFlow()

    // In-memory cache of synced agenda items
    private val _syncedAgendaItems = MutableStateFlow<Map<Int, LocalAgendaItemSyncRecord>>(emptyMap())
    val syncedAgendaItems: StateFlow<Map<Int, LocalAgendaItemSyncRecord>> = _syncedAgendaItems.asStateFlow()

    // Selected calendar for syncing (stored in preferences)
    private var selectedCalendarId: String? = null

    /**
     * Check if we have calendar permissions.
     */
    suspend fun hasCalendarPermission(): Boolean {
        return calendarService.hasCalendarPermission()
    }

    /**
     * Get available calendars from the device.
     */
    suspend fun getAvailableCalendars(): CalendarResult<List<CalendarInfo>> {
        return calendarService.getAvailableCalendars()
    }

    /**
     * Set the preferred calendar for syncing events.
     */
    fun setPreferredCalendar(calendarId: String) {
        selectedCalendarId = calendarId
        Logger.d(TAG, "Set preferred calendar: $calendarId")
    }

    /**
     * Get the currently selected calendar ID.
     */
    fun getPreferredCalendarId(): String? = selectedCalendarId

    /**
     * Check if an event is currently synced to the calendar.
     */
    fun isEventSynced(eventId: Int): Boolean {
        return _syncedEvents.value.containsKey(eventId)
    }

    /**
     * Get the local sync record for an event if it exists.
     */
    fun getSyncRecord(eventId: Int): LocalCalendarSyncRecord? {
        return _syncedEvents.value[eventId]
    }

    /**
     * Sync an event to the device calendar.
     *
     * @param event The event to sync
     * @param venue Optional venue information for location
     * @param calendarId Optional specific calendar ID (uses preferred calendar if not specified)
     * @return Result containing the calendar event ID or error
     */
    suspend fun syncEventToCalendar(
        event: Event,
        venue: Venue?,
        calendarId: String? = null
    ): CalendarResult<String> {
        val targetCalendarId = calendarId ?: selectedCalendarId
        if (targetCalendarId == null) {
            Logger.e(TAG, "No calendar selected for syncing")
            return CalendarResult.Error("No calendar selected. Please choose a calendar in settings.")
        }

        // Check if already synced
        val existingRecord = _syncedEvents.value[event.id]
        if (existingRecord != null) {
            Logger.d(TAG, "Event ${event.id} already synced, updating...")
            return updateEventInCalendar(event, venue, existingRecord.calendarEventId)
        }

        // Create calendar event data
        val startTime = event.startDate?.instant
        if (startTime == null) {
            return CalendarResult.Error("Event has no start date")
        }

        val calendarEventData = CalendarEventData(
            eventId = event.id,
            title = event.name,
            description = event.description,
            startTime = startTime,
            endTime = event.endDate?.instant,
            timezone = event.startDate.timezone,
            location = venue?.let { buildLocationString(it) },
            latitude = venue?.latitude,
            longitude = venue?.longitude
        )

        // Add to calendar
        return when (val result = calendarService.addEvent(targetCalendarId, calendarEventData)) {
            is CalendarResult.Success -> {
                val calendarEventId = result.data
                // Cache the sync record locally
                _syncedEvents.value = _syncedEvents.value + (event.id to LocalCalendarSyncRecord(
                    eventId = event.id,
                    calendarEventId = calendarEventId,
                    calendarId = targetCalendarId,
                    title = event.name,
                    startTime = startTime
                ))
                Logger.d(TAG, "Synced event ${event.id} to calendar: $calendarEventId")

                // Record sync on server (non-blocking - don't fail local sync if server fails)
                try {
                    val serverResult = eventSyncRepository.recordEventSync(
                        eventId = event.id,
                        platform = getCurrentPlatform(),
                        calendarEventId = calendarEventId,
                        calendarId = targetCalendarId
                    )
                    when (serverResult) {
                        is Resource.Success -> {
                            Logger.d(TAG, "Recorded event sync to server: ${event.id}")
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to record event sync to server (non-fatal): ${serverResult.message}")
                        }
                        else -> {
                            Logger.d(TAG, "Unexpected result when recording event sync to server")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Exception recording event sync to server (non-fatal): ${e.message}")
                }

                CalendarResult.Success(calendarEventId)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to sync event ${event.id}: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when syncing event ${event.id}")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Update an existing calendar event.
     * Internal for access from CalendarReconciliationService during reconciliation.
     */
    internal suspend fun updateEventInCalendar(
        event: Event,
        venue: Venue?,
        calendarEventId: String
    ): CalendarResult<String> {
        val startTime = event.startDate?.instant
        if (startTime == null) {
            return CalendarResult.Error("Event has no start date")
        }

        val calendarEventData = CalendarEventData(
            eventId = event.id,
            title = event.name,
            description = event.description,
            startTime = startTime,
            endTime = event.endDate?.instant,
            timezone = event.startDate.timezone,
            location = venue?.let { buildLocationString(it) },
            latitude = venue?.latitude,
            longitude = venue?.longitude
        )

        return when (val result = calendarService.updateEvent(calendarEventId, calendarEventData)) {
            is CalendarResult.Success -> {
                CalendarResult.Success(calendarEventId)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to update calendar event: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when updating")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Remove an event from the device calendar.
     *
     * @param eventId The event ID to remove
     * @return Result indicating success or error
     */
    suspend fun removeEventFromCalendar(eventId: Int): CalendarResult<Unit> {
        val syncRecord = _syncedEvents.value[eventId]
        if (syncRecord == null) {
            Logger.d(TAG, "Event $eventId not synced, nothing to remove")
            return CalendarResult.Success(Unit)
        }

        return when (val result = calendarService.deleteEvent(syncRecord.calendarEventId)) {
            is CalendarResult.Success -> {
                // Remove from local cache
                _syncedEvents.value = _syncedEvents.value - eventId
                Logger.d(TAG, "Removed event $eventId from calendar")

                // Remove sync record from server (non-blocking)
                try {
                    val serverResult = eventSyncRepository.removeEventSync(eventId)
                    when (serverResult) {
                        is Resource.Success -> {
                            Logger.d(TAG, "Removed event sync from server: $eventId")
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to remove event sync from server (non-fatal): ${serverResult.message}")
                        }
                        else -> {
                            Logger.d(TAG, "Unexpected result when removing event sync from server")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Exception removing event sync from server (non-fatal): ${e.message}")
                }

                CalendarResult.Success(Unit)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to remove event $eventId from calendar: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when removing event $eventId")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Handle event update - update the corresponding calendar event if synced.
     */
    suspend fun handleEventUpdate(event: Event, venue: Venue?): CalendarResult<Unit> {
        val syncRecord = _syncedEvents.value[event.id] ?: return CalendarResult.Success(Unit)

        return when (val result = updateEventInCalendar(event, venue, syncRecord.calendarEventId)) {
            is CalendarResult.Success -> CalendarResult.Success(Unit)
            is CalendarResult.Error -> result
            CalendarResult.PermissionDenied -> CalendarResult.PermissionDenied
        }
    }

    /**
     * Build a location string from venue information.
     */
    private fun buildLocationString(venue: Venue): String {
        val parts = mutableListOf<String>()
        venue.name?.let { parts.add(it) }
        venue.address?.let { parts.add(it) }
        venue.city?.let { city ->
            val cityPart = buildString {
                append(city)
                venue.state?.let { state -> append(", $state") }
            }
            parts.add(cityPart)
        }
        return parts.joinToString(", ")
    }

    /**
     * Load synced agenda items from the server.
     * Called on app launch to restore sync state after reinstall or when local cache is empty.
     */
    suspend fun loadSyncedAgendaItems() {
        Logger.d(TAG, "Loading synced agenda items from server...")

        when (val result = agendaItemSyncRepository.getMySyncedAgendaItems()) {
            is Resource.Success -> {
                val serverRecords = result.data
                Logger.d(TAG, "Loaded ${serverRecords.size} agenda item sync records from server")

                // Filter for SYNCED status only and convert to local format
                val localRecords = serverRecords
                    .filter { it.status == AgendaItemSyncStatus.SYNCED }
                    .associate { record ->
                        record.agendaItemId to LocalAgendaItemSyncRecord(
                            agendaItemId = record.agendaItemId,
                            eventId = record.eventId,
                            calendarEventId = record.calendarEventId,
                            calendarId = record.calendarId ?: "",
                            title = "", // Title will be loaded when displaying
                            startTime = null // Start time not available from server sync records
                        )
                    }

                _syncedAgendaItems.value = localRecords
                Logger.d(TAG, "Populated local agenda item cache with ${localRecords.size} active syncs")
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to load synced agenda items from server: ${result.message}")
            }
            else -> {
                Logger.d(TAG, "Unexpected result when loading synced agenda items")
            }
        }
    }

    /**
     * Load synced event calendar records from the server.
     * Called on app launch to restore event sync state after reinstall or when local cache is empty.
     */
    suspend fun loadSyncedEventCalendarRecords() {
        Logger.d(TAG, "Loading synced event calendar records from server...")

        when (val result = eventSyncRepository.getMySyncedEvents()) {
            is Resource.Success -> {
                val serverRecords = result.data
                Logger.d(TAG, "Loaded ${serverRecords.size} event sync records from server")

                // Filter for SYNCED status only and convert to local format
                val localRecords = serverRecords
                    .filter { it.status == EventSyncStatus.SYNCED }
                    .associate { record ->
                        record.eventId to LocalCalendarSyncRecord(
                            eventId = record.eventId,
                            calendarEventId = record.calendarEventId,
                            calendarId = record.calendarId ?: "",
                            title = "", // Title will be loaded when displaying
                            startTime = record.syncedAt
                        )
                    }

                _syncedEvents.value = localRecords
                Logger.d(TAG, "Populated local event cache with ${localRecords.size} active syncs")
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to load synced events from server: ${result.message}")
            }
            else -> {
                Logger.d(TAG, "Unexpected result when loading synced events")
            }
        }
    }

    /**
     * Load all synced records from the server.
     * Convenience method that loads both agenda items and events.
     */
    suspend fun loadSyncedEvents() {
        loadSyncedAgendaItems()
        loadSyncedEventCalendarRecords()
    }

    // ========== Agenda Item Sync Methods ==========

    /**
     * Check if an agenda item is currently synced to the calendar.
     */
    fun isAgendaItemSynced(agendaItemId: Int): Boolean {
        return _syncedAgendaItems.value.containsKey(agendaItemId)
    }

    /**
     * Get the sync record for an agenda item if it exists.
     */
    fun getAgendaItemSyncRecord(agendaItemId: Int): LocalAgendaItemSyncRecord? {
        return _syncedAgendaItems.value[agendaItemId]
    }

    /**
     * Get all synced agenda items for an event.
     */
    fun getSyncedAgendaItemsForEvent(eventId: Int): List<LocalAgendaItemSyncRecord> {
        return _syncedAgendaItems.value.values.filter { it.eventId == eventId }
    }

    /**
     * Sync an agenda item to the device calendar.
     *
     * @param agendaItem The agenda item to sync
     * @param eventName The parent event name (for context in calendar)
     * @param calendarId Optional specific calendar ID (uses preferred calendar if not specified)
     * @return Result containing the calendar event ID or error
     */
    suspend fun syncAgendaItemToCalendar(
        agendaItem: AgendaItem,
        eventName: String,
        calendarId: String? = null
    ): CalendarResult<String> {
        val targetCalendarId = calendarId ?: selectedCalendarId
        if (targetCalendarId == null) {
            Logger.e(TAG, "No calendar selected for syncing")
            return CalendarResult.Error("No calendar selected. Please choose a calendar in settings.")
        }

        // Check if already synced
        val existingRecord = _syncedAgendaItems.value[agendaItem.id]
        if (existingRecord != null) {
            Logger.d(TAG, "Agenda item ${agendaItem.id} already synced, updating...")
            return updateAgendaItemInCalendar(agendaItem, eventName, existingRecord.calendarEventId)
        }

        // Create calendar event data
        val startTime = agendaItem.startTime?.instant
        if (startTime == null) {
            return CalendarResult.Error("Agenda item has no start time")
        }

        val calendarEventData = CalendarEventData(
            eventId = agendaItem.eventId,
            agendaItemId = agendaItem.id,
            title = "${agendaItem.title} - $eventName",
            description = agendaItem.description,
            startTime = startTime,
            endTime = agendaItem.endTime?.instant,
            timezone = agendaItem.startTime.timezone,
            location = agendaItem.location?.name,
            latitude = null, // Location doesn't have coordinates
            longitude = null
        )

        // Add to calendar
        return when (val result = calendarService.addEvent(targetCalendarId, calendarEventData)) {
            is CalendarResult.Success -> {
                val calendarEventId = result.data
                // Cache the sync record locally
                _syncedAgendaItems.value = _syncedAgendaItems.value + (agendaItem.id to LocalAgendaItemSyncRecord(
                    agendaItemId = agendaItem.id,
                    eventId = agendaItem.eventId,
                    calendarEventId = calendarEventId,
                    calendarId = targetCalendarId,
                    title = agendaItem.title,
                    startTime = startTime
                ))
                Logger.d(TAG, "Synced agenda item ${agendaItem.id} to calendar: $calendarEventId")

                // Record sync on server (non-blocking - don't fail local sync if server fails)
                try {
                    val serverResult = agendaItemSyncRepository.recordAgendaItemSync(
                        agendaItemId = agendaItem.id,
                        eventId = agendaItem.eventId,
                        platform = getCurrentPlatform(),
                        calendarEventId = calendarEventId,
                        calendarId = targetCalendarId
                    )
                    when (serverResult) {
                        is Resource.Success -> {
                            Logger.d(TAG, "Recorded agenda item sync to server: ${agendaItem.id}")
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to record sync to server (non-fatal): ${serverResult.message}")
                        }
                        else -> {
                            Logger.d(TAG, "Unexpected result when recording sync to server")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Exception recording sync to server (non-fatal): ${e.message}")
                }

                CalendarResult.Success(calendarEventId)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to sync agenda item ${agendaItem.id}: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when syncing agenda item ${agendaItem.id}")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Update an existing calendar event for an agenda item.
     * Used by reconciliation when server marks an item as NEEDS_UPDATE.
     *
     * @param agendaItem The updated agenda item with new details
     * @param eventName The parent event name (for calendar event title)
     * @param calendarEventId The existing calendar event ID to update
     * @return Result containing the calendar event ID or error
     */
    suspend fun updateAgendaItemInCalendar(
        agendaItem: AgendaItem,
        eventName: String,
        calendarEventId: String
    ): CalendarResult<String> {
        val startTime = agendaItem.startTime?.instant
        if (startTime == null) {
            return CalendarResult.Error("Agenda item has no start time")
        }

        val calendarEventData = CalendarEventData(
            eventId = agendaItem.eventId,
            agendaItemId = agendaItem.id,
            title = "${agendaItem.title} - $eventName",
            description = agendaItem.description,
            startTime = startTime,
            endTime = agendaItem.endTime?.instant,
            timezone = agendaItem.startTime.timezone,
            location = agendaItem.location?.name,
            latitude = null,
            longitude = null
        )

        return when (val result = calendarService.updateEvent(calendarEventId, calendarEventData)) {
            is CalendarResult.Success -> {
                Logger.d(TAG, "Updated agenda item calendar event: $calendarEventId")
                CalendarResult.Success(calendarEventId)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to update agenda item calendar event: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when updating agenda item")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Remove an agenda item from the device calendar.
     *
     * @param agendaItemId The agenda item ID to remove
     * @return Result indicating success or error
     */
    suspend fun removeAgendaItemFromCalendar(agendaItemId: Int): CalendarResult<Unit> {
        val syncRecord = _syncedAgendaItems.value[agendaItemId]
        if (syncRecord == null) {
            Logger.d(TAG, "Agenda item $agendaItemId not synced, nothing to remove")
            return CalendarResult.Success(Unit)
        }

        return when (val result = calendarService.deleteEvent(syncRecord.calendarEventId)) {
            is CalendarResult.Success -> {
                // Remove from local cache
                _syncedAgendaItems.value = _syncedAgendaItems.value - agendaItemId
                Logger.d(TAG, "Removed agenda item $agendaItemId from calendar")

                // Remove sync record from server (non-blocking)
                try {
                    val serverResult = agendaItemSyncRepository.removeAgendaItemSync(agendaItemId)
                    when (serverResult) {
                        is Resource.Success -> {
                            Logger.d(TAG, "Removed agenda item sync from server: $agendaItemId")
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to remove sync from server (non-fatal): ${serverResult.message}")
                        }
                        else -> {
                            Logger.d(TAG, "Unexpected result when removing sync from server")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Exception removing sync from server (non-fatal): ${e.message}")
                }

                CalendarResult.Success(Unit)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to remove agenda item $agendaItemId from calendar: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when removing agenda item $agendaItemId")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Sync multiple agenda items to the calendar (bulk operation).
     * Used for "Sync My Schedule" feature.
     *
     * @param agendaItems List of agenda items to sync
     * @param eventName The parent event name
     * @param calendarId Optional specific calendar ID
     * @return Result with count of successfully synced items or first error
     */
    suspend fun syncAgendaItemsToCalendar(
        agendaItems: List<AgendaItem>,
        eventName: String,
        calendarId: String? = null
    ): CalendarResult<Int> {
        val targetCalendarId = calendarId ?: selectedCalendarId
        if (targetCalendarId == null) {
            return CalendarResult.Error("No calendar selected. Please choose a calendar in settings.")
        }

        var successCount = 0
        var lastError: CalendarResult.Error? = null

        for (item in agendaItems) {
            // Skip items without start time
            if (item.startTime == null) {
                Logger.d(TAG, "Skipping agenda item ${item.id} - no start time")
                continue
            }

            when (val result = syncAgendaItemToCalendar(item, eventName, targetCalendarId)) {
                is CalendarResult.Success -> successCount++
                is CalendarResult.Error -> {
                    Logger.e(TAG, "Failed to sync agenda item ${item.id}: ${result.message}")
                    lastError = result
                }
                CalendarResult.PermissionDenied -> {
                    return CalendarResult.PermissionDenied
                }
            }
        }

        return if (successCount > 0 || lastError == null) {
            Logger.d(TAG, "Bulk sync complete: $successCount/${agendaItems.size} items synced")
            CalendarResult.Success(successCount)
        } else {
            lastError
        }
    }

    /**
     * Remove all synced agenda items for an event.
     */
    suspend fun removeAllAgendaItemsFromCalendar(eventId: Int): CalendarResult<Int> {
        val itemsToRemove = getSyncedAgendaItemsForEvent(eventId)
        var successCount = 0

        for (record in itemsToRemove) {
            when (val result = removeAgendaItemFromCalendar(record.agendaItemId)) {
                is CalendarResult.Success -> successCount++
                is CalendarResult.Error -> {
                    Logger.e(TAG, "Failed to remove agenda item ${record.agendaItemId}: ${result.message}")
                }
                CalendarResult.PermissionDenied -> {
                    return CalendarResult.PermissionDenied
                }
            }
        }

        Logger.d(TAG, "Removed $successCount/${itemsToRemove.size} agenda items for event $eventId")
        return CalendarResult.Success(successCount)
    }

    // ========== Push Notification Handlers ==========
    // These methods are called when calendar sync push notifications are received

    /**
     * Delete a calendar event by its calendar event ID.
     * Used when receiving CALENDAR_SYNC_DELETE push notifications.
     *
     * @param calendarEventId The device calendar's event ID
     * @return Result indicating success or error
     */
    suspend fun deleteByCalendarEventId(calendarEventId: String): CalendarResult<Unit> {
        Logger.d(TAG, "Deleting calendar event: $calendarEventId")

        // Find and remove from local cache
        val agendaItemEntry = _syncedAgendaItems.value.entries.find { it.value.calendarEventId == calendarEventId }
        if (agendaItemEntry != null) {
            _syncedAgendaItems.value = _syncedAgendaItems.value - agendaItemEntry.key
        }

        val eventEntry = _syncedEvents.value.entries.find { it.value.calendarEventId == calendarEventId }
        if (eventEntry != null) {
            _syncedEvents.value = _syncedEvents.value - eventEntry.key
        }

        // Delete from device calendar
        return when (val result = calendarService.deleteEvent(calendarEventId)) {
            is CalendarResult.Success -> {
                Logger.d(TAG, "Successfully deleted calendar event: $calendarEventId")
                CalendarResult.Success(Unit)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to delete calendar event: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Permission denied when deleting calendar event")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Delete multiple calendar events by their IDs.
     * Used when receiving CALENDAR_SYNC_BULK_DELETE push notifications.
     *
     * @param calendarEventIds List of calendar event IDs to delete
     * @return Result with count of successfully deleted items
     */
    suspend fun deleteMultipleByCalendarEventIds(calendarEventIds: List<String>): CalendarResult<Int> {
        Logger.d(TAG, "Bulk deleting ${calendarEventIds.size} calendar events")
        var successCount = 0

        for (calendarEventId in calendarEventIds) {
            when (deleteByCalendarEventId(calendarEventId)) {
                is CalendarResult.Success -> successCount++
                is CalendarResult.Error -> { /* Continue with next */ }
                CalendarResult.PermissionDenied -> {
                    return CalendarResult.PermissionDenied
                }
            }
        }

        Logger.d(TAG, "Bulk delete complete: $successCount/${calendarEventIds.size}")
        return CalendarResult.Success(successCount)
    }

    /**
     * Update a calendar event using data from a push notification payload.
     * Used when receiving CALENDAR_SYNC_UPDATE push notifications.
     *
     * @param calendarEventId The device calendar's event ID
     * @param title The agenda item title
     * @param startTime ISO 8601 timestamp string for start time
     * @param endTime ISO 8601 timestamp string for end time (optional)
     * @param timezone The timezone string (e.g., "America/New_York")
     * @param locationName The location name (optional)
     * @return Result indicating success or error
     */
    suspend fun updateCalendarEventFromPayload(
        calendarEventId: String,
        title: String,
        startTime: String?,
        endTime: String?,
        timezone: String?,
        locationName: String?
    ): CalendarResult<Unit> {
        Logger.d(TAG, "Updating calendar event from payload: $calendarEventId")

        // Parse start time - required for calendar event
        if (startTime == null) {
            Logger.e(TAG, "Cannot update calendar event - no start time provided")
            return CalendarResult.Error("No start time provided in update payload")
        }

        val startInstant = try {
            Instant.parse(startTime)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse start time: $startTime")
            return CalendarResult.Error("Invalid start time format: $startTime")
        }

        val endInstant = endTime?.let {
            try {
                Instant.parse(it)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to parse end time: $endTime")
                null
            }
        }

        // Build calendar event data
        // Note: We don't have eventId/agendaItemId in the payload update path,
        // but we don't need them for updating the calendar event
        val calendarEventData = CalendarEventData(
            eventId = 0, // Not used for updates
            agendaItemId = null,
            title = title,
            description = null, // Not included in payload - preserves existing
            startTime = startInstant,
            endTime = endInstant,
            timezone = timezone ?: "UTC",
            location = locationName,
            latitude = null,
            longitude = null
        )

        // Update the calendar event
        return when (val result = calendarService.updateEvent(calendarEventId, calendarEventData)) {
            is CalendarResult.Success -> {
                Logger.d(TAG, "Successfully updated calendar event from payload: $calendarEventId")
                CalendarResult.Success(Unit)
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to update calendar event from payload: ${result.message}")
                result
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Permission denied when updating calendar event from payload")
                CalendarResult.PermissionDenied
            }
        }
    }

    /**
     * Get count of synced agenda items (for reconciliation checks).
     */
    fun getSyncedItemCount(): Int {
        return _syncedAgendaItems.value.size + _syncedEvents.value.size
    }

    /**
     * Get all synced agenda item IDs (for reconciliation).
     */
    fun getAllSyncedAgendaItemIds(): List<Int> {
        return _syncedAgendaItems.value.keys.toList()
    }

    /**
     * Re-link an agenda item to a calendar event.
     * Used during reinstall reconciliation when we find existing calendar events
     * that match our server sync records.
     *
     * @param agendaItemId The agenda item ID
     * @param eventId The parent event ID
     * @param calendarEventId The device calendar event ID
     * @param calendarId The device calendar ID
     */
    fun relinkAgendaItem(
        agendaItemId: Int,
        eventId: Int,
        calendarEventId: String,
        calendarId: String
    ) {
        Logger.d(TAG, "Re-linking agenda item $agendaItemId to calendar event $calendarEventId")
        _syncedAgendaItems.value = _syncedAgendaItems.value + (agendaItemId to LocalAgendaItemSyncRecord(
            agendaItemId = agendaItemId,
            eventId = eventId,
            calendarEventId = calendarEventId,
            calendarId = calendarId,
            title = "", // Title will be loaded when displaying
            startTime = null // Start time not available during relink
        ))
    }

    /**
     * Get all synced event IDs (for reconciliation).
     */
    fun getAllSyncedEventIds(): List<Int> {
        return _syncedEvents.value.keys.toList()
    }

    /**
     * Re-link an event to a calendar event.
     * Used during reinstall reconciliation when we find existing calendar events
     * that match our server sync records.
     *
     * @param eventId The event ID
     * @param calendarEventId The device calendar event ID
     * @param calendarId The device calendar ID
     */
    fun relinkEvent(
        eventId: Int,
        calendarEventId: String,
        calendarId: String
    ) {
        Logger.d(TAG, "Re-linking event $eventId to calendar event $calendarEventId")
        _syncedEvents.value = _syncedEvents.value + (eventId to LocalCalendarSyncRecord(
            eventId = eventId,
            calendarEventId = calendarEventId,
            calendarId = calendarId,
            title = "", // Title will be loaded when displaying
            startTime = null // Start time not available during relink
        ))
    }
}
