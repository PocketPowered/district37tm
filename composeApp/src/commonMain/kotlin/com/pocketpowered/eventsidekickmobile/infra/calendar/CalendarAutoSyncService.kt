package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.UserEngagement
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized service that handles automatic calendar sync when users RSVP to events or agenda items.
 *
 * This service listens to all engagement updates from EngagementManager and:
 * - Auto-syncs events/agenda items to calendar when user RSVPs "GOING" (if auto-sync is enabled)
 * - Removes events/agenda items from calendar when user un-RSVPs (changes from GOING to something else)
 *
 * This ensures calendar sync works regardless of where the RSVP happens (detail screen,
 * schedule tabs, etc.)
 */
class CalendarAutoSyncService(
    private val engagementManager: EngagementManager,
    private val calendarSyncManager: CalendarSyncManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val agendaItemRepository: AgendaItemRepository,
    private val eventRepository: EventRepository
) {
    private val TAG = "CalendarAutoSyncService"

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track previous engagement states to detect transitions (for agenda items)
    private val previousAgendaItemEngagements = MutableStateFlow<Map<Int, UserEngagement>>(emptyMap())

    // Track previous engagement states for events
    private val previousEventEngagements = MutableStateFlow<Map<Int, UserEngagement>>(emptyMap())

    // Guard to prevent multiple starts
    private var isStarted = false

    /**
     * Start listening to engagement updates.
     * Should be called once at app initialization.
     * Safe to call multiple times - will only start once.
     */
    fun start() {
        if (isStarted) {
            Logger.d(TAG, "CalendarAutoSyncService already started, skipping")
            return
        }
        isStarted = true
        Logger.d(TAG, "Starting CalendarAutoSyncService")

        serviceScope.launch {
            engagementManager.engagementUpdates.collect { engagementEvent ->
                val entityId = engagementEvent.key.entityId
                val newEngagement = engagementEvent.engagement

                when (engagementEvent.key.entityType) {
                    EntityType.AGENDAITEM -> {
                        val previousEngagement = previousAgendaItemEngagements.value[entityId]
                        previousAgendaItemEngagements.value = previousAgendaItemEngagements.value + (entityId to newEngagement)

                        val wasGoing = previousEngagement?.status == UserEngagementStatus.GOING
                        val isGoing = newEngagement.status == UserEngagementStatus.GOING

                        when {
                            !wasGoing && isGoing -> handleAgendaItemRsvpGoing(entityId)
                            wasGoing && !isGoing -> handleAgendaItemUnRsvp(entityId)
                        }
                    }
                    EntityType.EVENT -> {
                        val previousEngagement = previousEventEngagements.value[entityId]
                        previousEventEngagements.value = previousEventEngagements.value + (entityId to newEngagement)

                        val wasGoing = previousEngagement?.status == UserEngagementStatus.GOING
                        val isGoing = newEngagement.status == UserEngagementStatus.GOING

                        when {
                            !wasGoing && isGoing -> handleEventRsvpGoing(entityId)
                            wasGoing && !isGoing -> handleEventUnRsvp(entityId)
                        }
                    }
                    else -> {
                        // Other entity types don't have calendar sync
                    }
                }
            }
        }
    }

    // ============================================================
    // Agenda Item Handlers
    // ============================================================

    /**
     * Handle user RSVP'ing as GOING to an agenda item - auto-sync if enabled and calendar is set up.
     */
    private suspend fun handleAgendaItemRsvpGoing(agendaItemId: Int) {
        // Check if auto-sync is enabled
        val autoSyncEnabled = userPreferencesManager.getAutoSyncCalendar()
        if (!autoSyncEnabled) {
            Logger.d(TAG, "Auto-sync disabled, skipping calendar sync for agenda item $agendaItemId")
            return
        }

        // Check if already synced
        if (calendarSyncManager.isAgendaItemSynced(agendaItemId)) {
            Logger.d(TAG, "Agenda item $agendaItemId already synced, skipping")
            return
        }

        // Check if we have a preferred calendar set
        val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
        if (preferredCalendarId == null) {
            Logger.d(TAG, "No preferred calendar set, skipping auto-sync for agenda item $agendaItemId")
            return
        }

        // Check permission
        if (!calendarSyncManager.hasCalendarPermission()) {
            Logger.d(TAG, "No calendar permission, skipping auto-sync for agenda item $agendaItemId")
            return
        }

        // Fetch the agenda item details to sync
        Logger.d(TAG, "Fetching agenda item $agendaItemId for auto-sync")
        when (val result = agendaItemRepository.getAgendaItem(agendaItemId)) {
            is Resource.Success<AgendaItem> -> {
                val agendaItem = result.data
                val eventName = agendaItem.event?.name ?: "Event"

                Logger.d(TAG, "Auto-syncing agenda item $agendaItemId to calendar after RSVP GOING")
                when (val syncResult = calendarSyncManager.syncAgendaItemToCalendar(agendaItem, eventName, preferredCalendarId)) {
                    is CalendarResult.Success -> {
                        Logger.d(TAG, "Successfully auto-synced agenda item $agendaItemId to calendar")
                    }
                    is CalendarResult.Error -> {
                        Logger.e(TAG, "Failed to auto-sync agenda item $agendaItemId: ${syncResult.message}")
                    }
                    CalendarResult.PermissionDenied -> {
                        Logger.e(TAG, "Calendar permission denied during auto-sync for agenda item $agendaItemId")
                    }
                }
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch agenda item $agendaItemId for auto-sync: ${result.message}")
            }
            else -> {}
        }
    }

    /**
     * Handle user un-RSVP'ing from an agenda item - remove from calendar if synced.
     */
    private suspend fun handleAgendaItemUnRsvp(agendaItemId: Int) {
        if (!calendarSyncManager.isAgendaItemSynced(agendaItemId)) {
            Logger.d(TAG, "Agenda item $agendaItemId not synced, nothing to remove")
            return
        }

        Logger.d(TAG, "Removing agenda item $agendaItemId from calendar after un-RSVP")
        when (val result = calendarSyncManager.removeAgendaItemFromCalendar(agendaItemId)) {
            is CalendarResult.Success -> {
                Logger.d(TAG, "Successfully removed agenda item $agendaItemId from calendar")
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to remove agenda item $agendaItemId from calendar: ${result.message}")
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when removing agenda item $agendaItemId")
            }
        }
    }

    // ============================================================
    // Event Handlers
    // ============================================================

    /**
     * Handle user RSVP'ing as GOING to an event - auto-sync if enabled and calendar is set up.
     */
    private suspend fun handleEventRsvpGoing(eventId: Int) {
        // Check if auto-sync is enabled
        val autoSyncEnabled = userPreferencesManager.getAutoSyncCalendar()
        if (!autoSyncEnabled) {
            Logger.d(TAG, "Auto-sync disabled, skipping calendar sync for event $eventId")
            return
        }

        // Check if already synced
        if (calendarSyncManager.isEventSynced(eventId)) {
            Logger.d(TAG, "Event $eventId already synced, skipping")
            return
        }

        // Check if we have a preferred calendar set
        val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
        if (preferredCalendarId == null) {
            Logger.d(TAG, "No preferred calendar set, skipping auto-sync for event $eventId")
            return
        }

        // Check permission
        if (!calendarSyncManager.hasCalendarPermission()) {
            Logger.d(TAG, "No calendar permission, skipping auto-sync for event $eventId")
            return
        }

        // Fetch the event details to sync
        Logger.d(TAG, "Fetching event $eventId for auto-sync")
        when (val result = eventRepository.getEvent(eventId)) {
            is Resource.Success<Event> -> {
                val event = result.data

                Logger.d(TAG, "Auto-syncing event $eventId to calendar after RSVP GOING")
                when (val syncResult = calendarSyncManager.syncEventToCalendar(event, event.venue, preferredCalendarId)) {
                    is CalendarResult.Success -> {
                        Logger.d(TAG, "Successfully auto-synced event $eventId to calendar")
                    }
                    is CalendarResult.Error -> {
                        Logger.e(TAG, "Failed to auto-sync event $eventId: ${syncResult.message}")
                    }
                    CalendarResult.PermissionDenied -> {
                        Logger.e(TAG, "Calendar permission denied during auto-sync for event $eventId")
                    }
                }
            }
            is Resource.Error -> {
                Logger.e(TAG, "Failed to fetch event $eventId for auto-sync: ${result.message}")
            }
            else -> {}
        }
    }

    /**
     * Handle user un-RSVP'ing from an event - remove from calendar if synced.
     */
    private suspend fun handleEventUnRsvp(eventId: Int) {
        if (!calendarSyncManager.isEventSynced(eventId)) {
            Logger.d(TAG, "Event $eventId not synced, nothing to remove")
            return
        }

        Logger.d(TAG, "Removing event $eventId from calendar after un-RSVP")
        when (val result = calendarSyncManager.removeEventFromCalendar(eventId)) {
            is CalendarResult.Success -> {
                Logger.d(TAG, "Successfully removed event $eventId from calendar")
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to remove event $eventId from calendar: ${result.message}")
            }
            CalendarResult.PermissionDenied -> {
                Logger.e(TAG, "Calendar permission denied when removing event $eventId")
            }
        }
    }
}
