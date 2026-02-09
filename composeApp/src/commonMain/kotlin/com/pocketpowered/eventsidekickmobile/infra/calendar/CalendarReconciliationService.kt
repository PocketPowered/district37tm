package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.AgendaItemSyncRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.EventSyncRepository
import com.district37.toastmasters.models.AgendaItemSyncRecord
import com.district37.toastmasters.models.AgendaItemSyncStatus
import com.district37.toastmasters.models.EventSyncRecord
import com.district37.toastmasters.models.EventSyncStatus
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource

/**
 * Result of a reconciliation run.
 */
data class ReconciliationResult(
    val deleted: Int = 0,
    val updated: Int = 0,
    val errors: Int = 0
) {
    val hasChanges: Boolean = deleted > 0 || updated > 0
    val isSuccess: Boolean = errors == 0

    companion object {
        val NoChanges = ReconciliationResult()
        val Skipped = ReconciliationResult() // User has nothing synced
    }
}

/**
 * Result of reinstall reconciliation.
 */
sealed class ReinstallReconciliationResult {
    data class Success(
        val relinkedCount: Int,
        val needsResyncCount: Int,
        val errorCount: Int
    ) : ReinstallReconciliationResult() {
        val hasIssues: Boolean = needsResyncCount > 0 || errorCount > 0
    }

    data object NoRecords : ReinstallReconciliationResult()
    data object PermissionDenied : ReinstallReconciliationResult()
    data class Error(val message: String) : ReinstallReconciliationResult()
}

/**
 * Service for reconciling local calendar sync state with server state.
 *
 * This is the "belt and suspenders" backup to the push notification system.
 * It runs on app open to catch any missed push notifications and ensure
 * the local calendar matches what the server expects.
 *
 * Reconciliation flow:
 * 1. Check if user has any synced items (bail early if none)
 * 2. Fetch sync records from server (what should be in calendar)
 * 3. Compare with local state
 * 4. Handle discrepancies:
 *    - Item deleted on server → delete from calendar
 *    - Item updated on server → update calendar event
 *    - User no longer GOING → delete from calendar
 */
class CalendarReconciliationService(
    private val calendarSyncManager: CalendarSyncManager,
    private val agendaItemSyncRepository: AgendaItemSyncRepository,
    private val eventSyncRepository: EventSyncRepository,
    private val agendaItemRepository: AgendaItemRepository,
    private val eventRepository: EventRepository,
    private val calendarEventMatcher: CalendarEventMatcher
) {
    private val TAG = "CalendarReconciliation"

    /**
     * Run full reconciliation for all synced items.
     * Call this on app launch.
     *
     * @return Result indicating what was fixed
     */
    suspend fun reconcileAll(): ReconciliationResult {
        // Quick check - bail early if nothing is synced
        val syncCount = calendarSyncManager.getSyncedItemCount()
        if (syncCount == 0) {
            Logger.d(TAG, "No synced items, skipping reconciliation")
            return ReconciliationResult.Skipped
        }

        Logger.d(TAG, "Starting full reconciliation for $syncCount items")

        var totalDeleted = 0
        var totalUpdated = 0
        var totalErrors = 0

        // Reconcile agenda items
        val localAgendaItemIds = calendarSyncManager.getAllSyncedAgendaItemIds()
        if (localAgendaItemIds.isNotEmpty()) {
            when (val serverResult = agendaItemSyncRepository.getMySyncedAgendaItems()) {
                is Resource.Success -> {
                    val agendaResult = reconcileWithServerRecords(localAgendaItemIds, serverResult.data)
                    totalDeleted += agendaResult.deleted
                    totalUpdated += agendaResult.updated
                    totalErrors += agendaResult.errors
                }
                is Resource.Error -> {
                    Logger.e(TAG, "Failed to fetch server agenda item sync records: ${serverResult.message}")
                    totalErrors++
                }
                else -> {
                    Logger.e(TAG, "Unexpected result type from server for agenda items")
                    totalErrors++
                }
            }
        }

        // Reconcile events
        val eventResult = reconcileEventSyncs()
        totalDeleted += eventResult.deleted
        totalUpdated += eventResult.updated
        totalErrors += eventResult.errors

        Logger.d(TAG, "Full reconciliation complete: deleted=$totalDeleted, updated=$totalUpdated, errors=$totalErrors")
        return ReconciliationResult(deleted = totalDeleted, updated = totalUpdated, errors = totalErrors)
    }

    /**
     * Run reconciliation for a specific event's agenda items.
     * Call this when viewing event details.
     *
     * @param eventId The event to reconcile
     * @return Result indicating what was fixed
     */
    suspend fun reconcileForEvent(eventId: Int): ReconciliationResult {
        val localSyncs = calendarSyncManager.getSyncedAgendaItemsForEvent(eventId)
        if (localSyncs.isEmpty()) {
            Logger.d(TAG, "No synced items for event $eventId, skipping reconciliation")
            return ReconciliationResult.Skipped
        }

        Logger.d(TAG, "Starting reconciliation for event $eventId (${localSyncs.size} items)")

        // Fetch server sync records for this event
        return when (val serverResult = agendaItemSyncRepository.getSyncedAgendaItemsForEvent(eventId)) {
            is Resource.Success -> {
                val localSyncedIds = localSyncs.map { it.agendaItemId }
                reconcileWithServerRecords(localSyncedIds, serverResult.data)
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch server sync records for event $eventId: ${serverResult.message}")
                ReconciliationResult(errors = 1)
            }
            else -> {
                Logger.e(TAG, "Unexpected result type from server")
                ReconciliationResult(errors = 1)
            }
        }
    }

    /**
     * Core reconciliation logic - compare local state with server records
     * and fix any discrepancies.
     */
    private suspend fun reconcileWithServerRecords(
        localSyncedIds: List<Int>,
        serverRecords: List<AgendaItemSyncRecord>
    ): ReconciliationResult {
        var deleted = 0
        var updated = 0
        var errors = 0

        // Create a map of server records for quick lookup
        val serverRecordsMap = serverRecords.associateBy { it.agendaItemId }

        // Check each locally synced item against server state
        for (agendaItemId in localSyncedIds) {
            val serverRecord = serverRecordsMap[agendaItemId]

            when {
                // Item not in server records - was deleted or user un-RSVP'd
                serverRecord == null -> {
                    Logger.d(TAG, "Agenda item $agendaItemId not found on server, removing from calendar")
                    when (calendarSyncManager.removeAgendaItemFromCalendar(agendaItemId)) {
                        is CalendarResult.Success -> deleted++
                        is CalendarResult.Error -> errors++
                        CalendarResult.PermissionDenied -> errors++
                    }
                }

                // Server marked it as deleted
                serverRecord.status == AgendaItemSyncStatus.DELETED -> {
                    Logger.d(TAG, "Agenda item $agendaItemId marked as deleted on server, removing from calendar")
                    when (calendarSyncManager.removeAgendaItemFromCalendar(agendaItemId)) {
                        is CalendarResult.Success -> deleted++
                        is CalendarResult.Error -> errors++
                        CalendarResult.PermissionDenied -> errors++
                    }
                }

                // Server marked it as needing update
                serverRecord.status == AgendaItemSyncStatus.NEEDS_UPDATE -> {
                    Logger.d(TAG, "Agenda item $agendaItemId needs update, fetching details...")

                    // Fetch the full agenda item from the server
                    when (val agendaItemResult = agendaItemRepository.getAgendaItem(agendaItemId)) {
                        is Resource.Success -> {
                            val agendaItem = agendaItemResult.data
                            val eventName = agendaItem.event?.name ?: "Event"

                            // Update the calendar event with fresh data
                            when (calendarSyncManager.updateAgendaItemInCalendar(
                                agendaItem = agendaItem,
                                eventName = eventName,
                                calendarEventId = serverRecord.calendarEventId
                            )) {
                                is CalendarResult.Success -> {
                                    Logger.d(TAG, "Successfully updated agenda item $agendaItemId in calendar")
                                    updated++
                                }
                                is CalendarResult.Error -> {
                                    Logger.e(TAG, "Failed to update agenda item $agendaItemId in calendar")
                                    errors++
                                }
                                CalendarResult.PermissionDenied -> {
                                    Logger.e(TAG, "Permission denied when updating agenda item $agendaItemId")
                                    errors++
                                }
                            }
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to fetch agenda item $agendaItemId: ${agendaItemResult.message}")
                            errors++
                        }
                        else -> {
                            Logger.e(TAG, "Unexpected result when fetching agenda item $agendaItemId")
                            errors++
                        }
                    }
                }

                // Item exists on server with SYNCED status - verify it's still valid
                // For now, we trust that SYNCED items are in sync. Future enhancement:
                // compare lastServerUpdatedAt with a locally stored timestamp to detect
                // missed push notification updates.
                else -> {
                    // Item is SYNCED - no action needed
                    Logger.d(TAG, "Agenda item $agendaItemId is in sync")
                }
            }
        }

        Logger.d(TAG, "Reconciliation complete: deleted=$deleted, updated=$updated, errors=$errors")
        return ReconciliationResult(deleted, updated, errors)
    }

    /**
     * Check if any synced items need reconciliation.
     * This is a quick check that can be used to show UI indicators.
     *
     * @return True if reconciliation is needed
     */
    suspend fun needsReconciliation(): Boolean {
        val syncCount = calendarSyncManager.getSyncedItemCount()
        if (syncCount == 0) return false

        // Fetch server records and compare
        return when (val serverResult = agendaItemSyncRepository.getMySyncedAgendaItems()) {
            is Resource.Success -> {
                val localSyncedIds = calendarSyncManager.getAllSyncedAgendaItemIds()
                val serverRecordsMap = serverResult.data.associateBy { it.agendaItemId }

                // Check for any discrepancies
                localSyncedIds.any { agendaItemId ->
                    val serverRecord = serverRecordsMap[agendaItemId]
                    serverRecord == null ||
                            serverRecord.status == AgendaItemSyncStatus.DELETED ||
                            serverRecord.status == AgendaItemSyncStatus.NEEDS_UPDATE
                }
            }
            else -> false
        }
    }

    /**
     * Reconcile sync state after app reinstall.
     *
     * This method:
     * 1. Fetches sync records from the server (what the server thinks is synced)
     * 2. For each record, checks if the calendar event still exists on the device
     *    - First by stored calendarEventId (fast)
     *    - Then by title + time matching (fallback)
     * 3. Re-links found events to the local cache
     * 4. Reports items that couldn't be found (may need resync)
     *
     * @return Result with counts of relinked, needs_resync, and errors
     */
    suspend fun reconcileOnReinstall(): ReinstallReconciliationResult {
        Logger.d(TAG, "Starting reinstall reconciliation...")

        // Check calendar permission first
        if (!calendarSyncManager.hasCalendarPermission()) {
            Logger.d(TAG, "No calendar permission, skipping reinstall reconciliation")
            return ReinstallReconciliationResult.PermissionDenied
        }

        var totalRelinked = 0
        var totalNeedsResync = 0
        var totalErrors = 0

        // Reconcile agenda item syncs
        val agendaResult = reconcileAgendaItemsOnReinstall()
        when (agendaResult) {
            is ReinstallReconciliationResult.Success -> {
                totalRelinked += agendaResult.relinkedCount
                totalNeedsResync += agendaResult.needsResyncCount
                totalErrors += agendaResult.errorCount
            }
            is ReinstallReconciliationResult.Error -> {
                Logger.e(TAG, "Agenda item reinstall reconciliation failed: ${agendaResult.message}")
                totalErrors++
            }
            ReinstallReconciliationResult.NoRecords -> {
                Logger.d(TAG, "No agenda item sync records")
            }
            ReinstallReconciliationResult.PermissionDenied -> {
                // Already checked above
            }
        }

        // Reconcile event syncs
        val eventResult = reconcileEventsOnReinstall()
        when (eventResult) {
            is ReinstallReconciliationResult.Success -> {
                totalRelinked += eventResult.relinkedCount
                totalNeedsResync += eventResult.needsResyncCount
                totalErrors += eventResult.errorCount
            }
            is ReinstallReconciliationResult.Error -> {
                Logger.e(TAG, "Event reinstall reconciliation failed: ${eventResult.message}")
                totalErrors++
            }
            ReinstallReconciliationResult.NoRecords -> {
                Logger.d(TAG, "No event sync records")
            }
            ReinstallReconciliationResult.PermissionDenied -> {
                // Already checked above
            }
        }

        if (totalRelinked == 0 && totalNeedsResync == 0 && totalErrors == 0) {
            return ReinstallReconciliationResult.NoRecords
        }

        Logger.d(TAG, "Reinstall reconciliation complete: relinked=$totalRelinked, needsResync=$totalNeedsResync, errors=$totalErrors")
        return ReinstallReconciliationResult.Success(
            relinkedCount = totalRelinked,
            needsResyncCount = totalNeedsResync,
            errorCount = totalErrors
        )
    }

    /**
     * Reconcile agenda item syncs on reinstall.
     */
    private suspend fun reconcileAgendaItemsOnReinstall(): ReinstallReconciliationResult {
        // Fetch server records
        val serverRecords = when (val result = agendaItemSyncRepository.getMySyncedAgendaItems()) {
            is Resource.Success -> result.data.filter { it.status == AgendaItemSyncStatus.SYNCED }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch agenda item sync records: ${result.message}")
                return ReinstallReconciliationResult.Error(result.message ?: "Unknown error")
            }
            else -> {
                Logger.e(TAG, "Unexpected result type")
                return ReinstallReconciliationResult.Error("Unexpected result type")
            }
        }

        if (serverRecords.isEmpty()) {
            return ReinstallReconciliationResult.NoRecords
        }

        Logger.d(TAG, "Found ${serverRecords.size} agenda item sync records on server, checking calendar...")

        var relinkedCount = 0
        var needsResyncCount = 0
        var errorCount = 0

        for (record in serverRecords) {
            // Skip if we don't have the necessary info for matching
            val startTime = record.syncedAt
            if (startTime == null) {
                Logger.d(TAG, "Sync record ${record.agendaItemId} has no syncedAt, skipping")
                continue
            }

            val criteria = CalendarEventMatchCriteria(
                originalCalendarEventId = record.calendarEventId,
                title = "", // We don't have the title stored on the server record
                startTime = startTime,
                endTime = null
            )

            when (val matchResult = calendarEventMatcher.matchEvent(criteria)) {
                is CalendarEventMatchResult.FoundById -> {
                    Logger.d(TAG, "Found calendar event by ID for agenda item ${record.agendaItemId}")
                    calendarSyncManager.relinkAgendaItem(
                        agendaItemId = record.agendaItemId,
                        eventId = record.eventId,
                        calendarEventId = record.calendarEventId,
                        calendarId = record.calendarId ?: ""
                    )
                    relinkedCount++
                }
                is CalendarEventMatchResult.FoundByContent -> {
                    Logger.d(TAG, "Found calendar event by content for agenda item ${record.agendaItemId}")
                    calendarSyncManager.relinkAgendaItem(
                        agendaItemId = record.agendaItemId,
                        eventId = record.eventId,
                        calendarEventId = matchResult.calendarEventId,
                        calendarId = record.calendarId ?: ""
                    )
                    relinkedCount++
                }
                CalendarEventMatchResult.NotFound -> {
                    Logger.d(TAG, "Calendar event not found for agenda item ${record.agendaItemId}")
                    needsResyncCount++
                }
                is CalendarEventMatchResult.Error -> {
                    Logger.e(TAG, "Error matching calendar event: ${matchResult.message}")
                    errorCount++
                }
            }
        }

        return ReinstallReconciliationResult.Success(relinkedCount, needsResyncCount, errorCount)
    }

    /**
     * Reconcile event syncs on reinstall.
     */
    private suspend fun reconcileEventsOnReinstall(): ReinstallReconciliationResult {
        // Fetch server records
        val serverRecords = when (val result = eventSyncRepository.getMySyncedEvents()) {
            is Resource.Success -> result.data.filter { it.status == EventSyncStatus.SYNCED }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch event sync records: ${result.message}")
                return ReinstallReconciliationResult.Error(result.message ?: "Unknown error")
            }
            else -> {
                Logger.e(TAG, "Unexpected result type")
                return ReinstallReconciliationResult.Error("Unexpected result type")
            }
        }

        if (serverRecords.isEmpty()) {
            return ReinstallReconciliationResult.NoRecords
        }

        Logger.d(TAG, "Found ${serverRecords.size} event sync records on server, checking calendar...")

        var relinkedCount = 0
        var needsResyncCount = 0
        var errorCount = 0

        for (record in serverRecords) {
            // Skip if we don't have the necessary info for matching
            val startTime = record.syncedAt
            if (startTime == null) {
                Logger.d(TAG, "Sync record for event ${record.eventId} has no syncedAt, skipping")
                continue
            }

            val criteria = CalendarEventMatchCriteria(
                originalCalendarEventId = record.calendarEventId,
                title = "", // We don't have the title stored on the server record
                startTime = startTime,
                endTime = null
            )

            when (val matchResult = calendarEventMatcher.matchEvent(criteria)) {
                is CalendarEventMatchResult.FoundById -> {
                    Logger.d(TAG, "Found calendar event by ID for event ${record.eventId}")
                    calendarSyncManager.relinkEvent(
                        eventId = record.eventId,
                        calendarEventId = record.calendarEventId,
                        calendarId = record.calendarId ?: ""
                    )
                    relinkedCount++
                }
                is CalendarEventMatchResult.FoundByContent -> {
                    Logger.d(TAG, "Found calendar event by content for event ${record.eventId}")
                    calendarSyncManager.relinkEvent(
                        eventId = record.eventId,
                        calendarEventId = matchResult.calendarEventId,
                        calendarId = record.calendarId ?: ""
                    )
                    relinkedCount++
                }
                CalendarEventMatchResult.NotFound -> {
                    Logger.d(TAG, "Calendar event not found for event ${record.eventId}")
                    needsResyncCount++
                }
                is CalendarEventMatchResult.Error -> {
                    Logger.e(TAG, "Error matching calendar event: ${matchResult.message}")
                    errorCount++
                }
            }
        }

        return ReinstallReconciliationResult.Success(relinkedCount, needsResyncCount, errorCount)
    }

    /**
     * Reconcile event syncs (for events, not agenda items).
     * Compare local state with server records and fix discrepancies.
     */
    suspend fun reconcileEventSyncs(): ReconciliationResult {
        val localSyncedIds = calendarSyncManager.getAllSyncedEventIds()
        if (localSyncedIds.isEmpty()) {
            Logger.d(TAG, "No events synced locally, skipping event reconciliation")
            return ReconciliationResult.Skipped
        }

        Logger.d(TAG, "Starting event sync reconciliation for ${localSyncedIds.size} events")

        return when (val serverResult = eventSyncRepository.getMySyncedEvents()) {
            is Resource.Success -> {
                reconcileEventSyncsWithServerRecords(localSyncedIds, serverResult.data)
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch server event sync records: ${serverResult.message}")
                ReconciliationResult(errors = 1)
            }
            else -> {
                Logger.e(TAG, "Unexpected result type from server")
                ReconciliationResult(errors = 1)
            }
        }
    }

    /**
     * Core reconciliation logic for event syncs.
     */
    private suspend fun reconcileEventSyncsWithServerRecords(
        localSyncedIds: List<Int>,
        serverRecords: List<EventSyncRecord>
    ): ReconciliationResult {
        var deleted = 0
        var updated = 0
        var errors = 0

        val serverRecordsMap = serverRecords.associateBy { it.eventId }

        for (eventId in localSyncedIds) {
            val serverRecord = serverRecordsMap[eventId]

            when {
                // Event not in server records - was deleted or user un-RSVP'd
                serverRecord == null -> {
                    Logger.d(TAG, "Event $eventId not found on server, removing from calendar")
                    when (calendarSyncManager.removeEventFromCalendar(eventId)) {
                        is CalendarResult.Success -> deleted++
                        is CalendarResult.Error -> errors++
                        CalendarResult.PermissionDenied -> errors++
                    }
                }

                // Server marked it as deleted
                serverRecord.status == EventSyncStatus.DELETED -> {
                    Logger.d(TAG, "Event $eventId marked as deleted on server, removing from calendar")
                    when (calendarSyncManager.removeEventFromCalendar(eventId)) {
                        is CalendarResult.Success -> deleted++
                        is CalendarResult.Error -> errors++
                        CalendarResult.PermissionDenied -> errors++
                    }
                }

                // Server marked it as needing update
                serverRecord.status == EventSyncStatus.NEEDS_UPDATE -> {
                    Logger.d(TAG, "Event $eventId needs update, fetching details...")

                    // Fetch the full event from the server
                    when (val eventResult = eventRepository.getEvent(eventId)) {
                        is Resource.Success -> {
                            val event = eventResult.data

                            // Update the calendar event with fresh data
                            when (calendarSyncManager.updateEventInCalendar(
                                event = event,
                                venue = null, // Venue will be fetched by CalendarSyncManager if needed
                                calendarEventId = serverRecord.calendarEventId
                            )) {
                                is CalendarResult.Success -> {
                                    Logger.d(TAG, "Successfully updated event $eventId in calendar")
                                    updated++
                                }
                                is CalendarResult.Error -> {
                                    Logger.e(TAG, "Failed to update event $eventId in calendar")
                                    errors++
                                }
                                CalendarResult.PermissionDenied -> {
                                    Logger.e(TAG, "Permission denied when updating event $eventId")
                                    errors++
                                }
                            }
                        }
                        is Resource.Error -> {
                            Logger.e(TAG, "Failed to fetch event $eventId: ${eventResult.message}")
                            errors++
                        }
                        else -> {
                            Logger.e(TAG, "Unexpected result when fetching event $eventId")
                            errors++
                        }
                    }
                }

                // Event is SYNCED - no action needed
                else -> {
                    Logger.d(TAG, "Event $eventId is in sync")
                }
            }
        }

        Logger.d(TAG, "Event sync reconciliation complete: deleted=$deleted, updated=$updated, errors=$errors")
        return ReconciliationResult(deleted = deleted, updated = updated, errors = errors)
    }
}
