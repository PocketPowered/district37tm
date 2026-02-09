package com.district37.toastmasters.features.events

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.FriendRsvpRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.infra.calendar.CalendarInfo
import com.district37.toastmasters.infra.calendar.CalendarResult
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.features.engagement.FriendRsvpsFeature
import com.district37.toastmasters.features.schedules.AgendaTabsFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.DeleteOperationFeature
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseDetailViewModel
import com.district37.toastmasters.viewmodel.LazyFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for EventDetail screen
 *
 * Loads event details with nested venue and agenda dates in a single query.
 * Permissions are automatically extracted by BaseDetailViewModel.
 */
class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val agendaItemRepository: AgendaItemRepository,
    private val engagementManager: EngagementManager,
    private val authRepository: AuthRepository,
    private val friendRsvpRepository: FriendRsvpRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val calendarSyncManager: CalendarSyncManager,
    private val eventId: Int
) : BaseDetailViewModel<Event, EventRepository>(eventId, eventRepository) {

    override val tag = "EventDetailViewModel"

    // Delete functionality using DeleteOperationFeature
    private val deleteHandler = DeleteOperationFeature(
        tag = tag,
        scope = viewModelScope
    )
    val isDeleting = deleteHandler.isDeleting
    val deleteSuccess = deleteHandler.deleteSuccess

    // Pin event state (issue #23)
    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    // Consolidated calendar sync state (replaces multiple scattered StateFlows)
    private val _calendarState = MutableStateFlow(CalendarSyncState.Initial)
    val calendarState: StateFlow<CalendarSyncState> = _calendarState.asStateFlow()

    // Consolidated bulk sync state
    private val _bulkSyncState = MutableStateFlow(BulkSyncState.Initial)
    val bulkSyncState: StateFlow<BulkSyncState> = _bulkSyncState.asStateFlow()

    // Convenience accessors for backwards compatibility with existing UI code
    val isEventSynced: StateFlow<Boolean> = calendarState.map { it.isEventSynced }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val availableCalendars: StateFlow<List<CalendarInfo>> = calendarState.map { it.availableCalendars }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val calendarSyncError: StateFlow<String?> = calendarState.map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val isCalendarSyncing: StateFlow<Boolean> = calendarState.map { it.isSyncing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val showCalendarPicker: StateFlow<Boolean> = calendarState.map { it.showCalendarPicker }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val needsCalendarPermission: StateFlow<Boolean> = calendarState.map { it.needsPermission }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isLoadingCalendars: StateFlow<Boolean> = calendarState.map { it.isLoadingCalendars }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isBulkSyncing: StateFlow<Boolean> = bulkSyncState.map { it.isSyncing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val bulkSyncResult: StateFlow<String?> = bulkSyncState.map { it.resultMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Feature for managing agenda date tabs
     */
    val agendaTabsFeature = AgendaTabsFeature(
        agendaItemRepository = agendaItemRepository,
        coroutineScope = viewModelScope,
        eventId = eventId,
        userPreferencesManager = userPreferencesManager
    )

    // Derived state (issue #18)
    val agendaEmptyMessage: StateFlow<String> = agendaTabsFeature.myScheduleOnly.map { myScheduleOnly ->
        if (myScheduleOnly) {
            "No items in your schedule for this date"
        } else {
            "No agenda items for this date"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No agenda items for this date")

    /**
     * Feature for authentication state and login prompts
     */
    val authFeature = AuthFeature(authRepository, viewModelScope)

    /**
     * Feature for entity engagement operations - initialized lazily when event loads
     */
    private val engagementFeatureLazy = LazyFeature(
        scope = viewModelScope,
        trigger = item.map { it is Resource.Success }
    ) {
        val event = (item.value as? Resource.Success)?.data
        EntityEngagementFeature(
            entityType = EntityType.EVENT,
            entityId = event?.id ?: eventId,
            engagementManager = engagementManager,
            authFeature = authFeature,
            coroutineScope = viewModelScope
        ).apply {
            event?.userEngagement?.let { initialize(it) }
        }
    }

    val engagementFeature: EntityEngagementFeature?
        get() = engagementFeatureLazy.instance

    /**
     * Feature for friend RSVPs display - initialized when event loads and user is authenticated
     */
    private val friendRsvpsFeatureLazy = LazyFeature(
        scope = viewModelScope,
        trigger = item.map { it is Resource.Success && authFeature.isAuthenticated }
    ) {
        FriendRsvpsFeature(
            entityType = EntityType.EVENT,
            entityId = eventId,
            friendRsvpRepository = friendRsvpRepository,
            coroutineScope = viewModelScope
        )
    }

    val friendRsvpsFeature: FriendRsvpsFeature?
        get() = friendRsvpsFeatureLazy.instance

    init {
        // Initialize agenda tabs feature with dates from event when loaded
        viewModelScope.launch {
            item.collect { resource ->
                if (resource is Resource.Success) {
                    agendaTabsFeature.initialize(resource.data.agendaDates)
                    // Load pinned state (issue #23)
                    // TODO: Load from event when isPinned field is added to Event model
                    // _isPinned.value = resource.data.isPinned ?: false
                }
            }
        }

        // Refresh friend RSVPs when engagement changes and auto-sync to calendar
        viewModelScope.launch {
            engagementFeatureLazy.isInitialized.collect { initialized ->
                if (initialized) {
                    var previousStatus: com.district37.toastmasters.graphql.type.UserEngagementStatus? = null
                    engagementFeature?.engagement?.collect { engagement ->
                        // When user changes their RSVP, refresh friend list
                        friendRsvpsFeature?.refresh()

                        // Auto-sync to calendar when RSVP changes to GOING
                        val currentStatus = engagement.status
                        if (currentStatus == com.district37.toastmasters.graphql.type.UserEngagementStatus.GOING
                            && previousStatus != com.district37.toastmasters.graphql.type.UserEngagementStatus.GOING
                        ) {
                            // User just RSVP'd "Going" - auto-sync to calendar if preferred calendar is set
                            val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
                            if (preferredCalendarId != null && !_calendarState.value.isEventSynced) {
                                syncToCalendar(preferredCalendarId)
                            }
                        } else if (currentStatus != com.district37.toastmasters.graphql.type.UserEngagementStatus.GOING
                            && previousStatus == com.district37.toastmasters.graphql.type.UserEngagementStatus.GOING
                        ) {
                            // User changed from "Going" to something else - remove from calendar
                            if (_calendarState.value.isEventSynced) {
                                removeFromCalendar()
                            }
                        }
                        previousStatus = currentStatus
                    }
                }
            }
        }
    }

    /**
     * Delete the event
     */
    fun deleteEvent() {
        deleteHandler.performDelete {
            when (val result = eventRepository.deleteEvent(eventId)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.errorType, result.message)
                else -> result as Resource<Unit>
            }
        }
    }

    /**
     * Toggle pin state for the event (issue #23)
     */
    fun togglePin() {
        val currentPinned = _isPinned.value
        _isPinned.value = !currentPinned

        viewModelScope.launch {
            // TODO: Call repository method to persist pin state
            // For now, this is just local state - will need to integrate with backend/preferences
        }
    }

    /**
     * Check if we have calendar permission and load calendars if so
     */
    fun checkCalendarPermission() {
        viewModelScope.launch {
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            _calendarState.update { it.copy(needsPermission = !hasPermission) }

            if (hasPermission) {
                loadAvailableCalendars()
            }
        }
    }

    /**
     * Load available calendars from the device
     */
    private suspend fun loadAvailableCalendars() {
        _calendarState.update { it.copy(isLoadingCalendars = true) }
        when (val result = calendarSyncManager.getAvailableCalendars()) {
            is CalendarResult.Success -> {
                _calendarState.update { it.copy(availableCalendars = result.data) }
            }
            is CalendarResult.Error -> {
                _calendarState.update { it.copy(error = result.message) }
            }
            CalendarResult.PermissionDenied -> {
                _calendarState.update { it.copy(needsPermission = true) }
            }
        }
        _calendarState.update { it.copy(isLoadingCalendars = false) }
    }

    /**
     * Called when user taps the calendar button
     */
    fun onCalendarButtonClick() {
        viewModelScope.launch {
            _calendarState.update { it.copy(error = null) }
            Logger.d(tag, "onCalendarButtonClick called")

            // Check if already synced
            if (_calendarState.value.isEventSynced) {
                Logger.d(tag, "Event already synced, removing from calendar")
                // Remove from calendar
                removeFromCalendar()
                return@launch
            }

            // Check permission first
            Logger.d(tag, "Checking calendar permission...")
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            Logger.d(tag, "hasCalendarPermission returned: $hasPermission")
            if (!hasPermission) {
                Logger.d(tag, "Setting needsCalendarPermission = true")
                _calendarState.update { it.copy(needsPermission = true) }
                return@launch
            }

            // Check if we have a preferred calendar set
            val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
            if (preferredCalendarId != null) {
                // Sync directly to preferred calendar
                syncToCalendar(preferredCalendarId)
                return@launch
            }

            // Need to pick a calendar - show picker and load calendars
            _calendarState.update { it.copy(showCalendarPicker = true) }

            // Load calendars if not loaded
            val currentState = _calendarState.value
            if (currentState.availableCalendars.isEmpty() && !currentState.isLoadingCalendars) {
                loadAvailableCalendars()
            }

            // After loading, check if we can auto-select
            val calendars = _calendarState.value.availableCalendars
            if (calendars.size == 1) {
                // Only one calendar, use it directly
                _calendarState.update { it.copy(showCalendarPicker = false) }
                syncToCalendar(calendars.first().id)
            }
            // Otherwise keep picker open for user to select
        }
    }

    /**
     * Called when user selects a calendar from the picker
     */
    fun onCalendarSelected(calendarId: String) {
        _calendarState.update { it.copy(showCalendarPicker = false) }
        calendarSyncManager.setPreferredCalendar(calendarId)
        viewModelScope.launch {
            syncToCalendar(calendarId)
        }
    }

    /**
     * Dismiss the calendar picker
     */
    fun dismissCalendarPicker() {
        _calendarState.update { it.copy(showCalendarPicker = false) }
    }

    /**
     * Dismiss permission request dialog
     */
    fun dismissPermissionRequest() {
        _calendarState.update { it.copy(needsPermission = false) }
    }

    /**
     * Clear calendar sync error
     */
    fun clearCalendarError() {
        _calendarState.update { it.copy(error = null) }
    }

    /**
     * Sync the current event to a calendar
     */
    private suspend fun syncToCalendar(calendarId: String) {
        val event = itemData ?: return

        _calendarState.update { it.copy(isSyncing = true, error = null) }

        when (val result = calendarSyncManager.syncEventToCalendar(event, event.venue, calendarId)) {
            is CalendarResult.Success -> {
                _calendarState.update { it.copy(isEventSynced = true) }
            }
            is CalendarResult.Error -> {
                _calendarState.update { it.copy(error = result.message) }
            }
            CalendarResult.PermissionDenied -> {
                _calendarState.update { it.copy(needsPermission = true) }
            }
        }

        _calendarState.update { it.copy(isSyncing = false) }
    }

    /**
     * Remove event from calendar
     */
    private suspend fun removeFromCalendar() {
        _calendarState.update { it.copy(isSyncing = true, error = null) }

        when (val result = calendarSyncManager.removeEventFromCalendar(eventId)) {
            is CalendarResult.Success -> {
                _calendarState.update { it.copy(isEventSynced = false) }
            }
            is CalendarResult.Error -> {
                _calendarState.update { it.copy(error = result.message) }
            }
            CalendarResult.PermissionDenied -> {
                _calendarState.update { it.copy(needsPermission = true) }
            }
        }

        _calendarState.update { it.copy(isSyncing = false) }
    }

    // ========== Bulk Sync "My Schedule" Methods ==========

    /**
     * Clear the bulk sync result message.
     */
    fun clearBulkSyncResult() {
        _bulkSyncState.update { it.copy(resultMessage = null) }
    }

    /**
     * Sync all "My Schedule" agenda items to the calendar.
     * This fetches all agenda items the user has RSVP'd "Going" to and syncs them.
     */
    fun syncMyScheduleToCalendar() {
        viewModelScope.launch {
            _calendarState.update { it.copy(error = null) }
            _bulkSyncState.update { it.copy(resultMessage = null) }

            // Check permission first
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            if (!hasPermission) {
                _calendarState.update { it.copy(needsPermission = true) }
                return@launch
            }

            // Check if we have a preferred calendar set
            val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
            if (preferredCalendarId == null) {
                // Need to show calendar picker first
                _calendarState.update { it.copy(showCalendarPicker = true) }
                val currentState = _calendarState.value
                if (currentState.availableCalendars.isEmpty() && !currentState.isLoadingCalendars) {
                    loadAvailableCalendars()
                }
                return@launch
            }

            performBulkSync(preferredCalendarId)
        }
    }

    /**
     * Called after selecting a calendar for bulk sync.
     */
    fun onCalendarSelectedForBulkSync(calendarId: String) {
        _calendarState.update { it.copy(showCalendarPicker = false) }
        calendarSyncManager.setPreferredCalendar(calendarId)
        viewModelScope.launch {
            performBulkSync(calendarId)
        }
    }

    /**
     * Perform the actual bulk sync of My Schedule items.
     */
    private suspend fun performBulkSync(calendarId: String) {
        val event = itemData ?: return
        val agendaDates = event.agendaDates

        if (agendaDates.isEmpty()) {
            _bulkSyncState.update { it.copy(resultMessage = "No agenda dates found for this event") }
            return
        }

        _bulkSyncState.update { it.copy(isSyncing = true) }
        Logger.d(tag, "Starting bulk sync for ${agendaDates.size} dates")

        val allAgendaItems = mutableListOf<com.district37.toastmasters.models.AgendaItem>()

        // Fetch all "My Schedule" items for each date
        for (agendaDate in agendaDates) {
            // Fetch items with myScheduleOnly = true
            when (val result = agendaItemRepository.getAgendaItemsByEventAndDate(
                eventId = eventId,
                date = agendaDate.date,
                first = 100, // Get up to 100 items per date
                myScheduleOnly = true
            )) {
                is Resource.Success -> {
                    allAgendaItems.addAll(result.data.agendaItems)
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to fetch agenda items for ${agendaDate.date}: ${result.message}")
                }
                else -> {}
            }
        }

        if (allAgendaItems.isEmpty()) {
            _bulkSyncState.update {
                it.copy(
                    isSyncing = false,
                    resultMessage = "No items in your schedule to sync. RSVP 'Going' to agenda items first."
                )
            }
            return
        }

        Logger.d(tag, "Found ${allAgendaItems.size} My Schedule items to sync")

        // Sync all items
        when (val result = calendarSyncManager.syncAgendaItemsToCalendar(
            agendaItems = allAgendaItems,
            eventName = event.name,
            calendarId = calendarId
        )) {
            is CalendarResult.Success -> {
                val count = result.data
                _bulkSyncState.update { it.copy(resultMessage = "Synced $count agenda items to your calendar") }
            }
            is CalendarResult.Error -> {
                _calendarState.update { it.copy(error = result.message) }
            }
            CalendarResult.PermissionDenied -> {
                _calendarState.update { it.copy(needsPermission = true) }
            }
        }

        _bulkSyncState.update { it.copy(isSyncing = false) }
    }
}
