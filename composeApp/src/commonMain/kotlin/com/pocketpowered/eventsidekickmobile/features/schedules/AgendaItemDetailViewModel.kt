package com.district37.toastmasters.features.schedules

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.components.calendar.CalendarSyncState
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.FriendRsvpRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.features.engagement.FriendRsvpsFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.infra.calendar.CalendarInfo
import com.district37.toastmasters.infra.calendar.CalendarResult
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.UserEngagement
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for agenda item detail screen
 * Loads agenda item details by ID
 */
class AgendaItemDetailViewModel(
    agendaItemRepository: AgendaItemRepository,
    private val engagementManager: EngagementManager,
    private val authRepository: AuthRepository,
    private val friendRsvpRepository: FriendRsvpRepository,
    private val calendarSyncManager: CalendarSyncManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val agendaItemId: Int
) : BaseDetailViewModel<AgendaItem, AgendaItemRepository>(agendaItemId, agendaItemRepository) {

    override val tag = "AgendaItemDetailViewModel"

    // Calendar sync state
    private val _isItemSynced = MutableStateFlow(false)
    val isItemSynced: StateFlow<Boolean> = _isItemSynced.asStateFlow()

    private val _isCalendarSyncing = MutableStateFlow(false)
    val isCalendarSyncing: StateFlow<Boolean> = _isCalendarSyncing.asStateFlow()

    private val _availableCalendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val availableCalendars: StateFlow<List<CalendarInfo>> = _availableCalendars.asStateFlow()

    private val _calendarSyncError = MutableStateFlow<String?>(null)
    val calendarSyncError: StateFlow<String?> = _calendarSyncError.asStateFlow()

    private val _showCalendarPicker = MutableStateFlow(false)
    val showCalendarPicker: StateFlow<Boolean> = _showCalendarPicker.asStateFlow()

    private val _needsCalendarPermission = MutableStateFlow(false)
    val needsCalendarPermission: StateFlow<Boolean> = _needsCalendarPermission.asStateFlow()

    private val _isLoadingCalendars = MutableStateFlow(false)
    val isLoadingCalendars: StateFlow<Boolean> = _isLoadingCalendars.asStateFlow()

    /**
     * Computed sync state for UI display.
     * Combines multiple state flows to determine the current visual sync state.
     */
    val calendarSyncState: StateFlow<CalendarSyncState> = combine(
        _isItemSynced,
        _isCalendarSyncing,
        _calendarSyncError
    ) { isSynced, isSyncing, error ->
        when {
            isSyncing -> CalendarSyncState.PENDING
            error != null -> CalendarSyncState.ERROR
            isSynced -> CalendarSyncState.SYNCED
            else -> CalendarSyncState.NOT_SYNCED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarSyncState.NOT_SYNCED
    )

    /**
     * Feature for authentication state and login prompts
     */
    val authFeature = AuthFeature(authRepository, viewModelScope)

    /**
     * Feature for entity engagement operations - initialized lazily when agenda item loads
     */
    private var _engagementFeature: EntityEngagementFeature? = null
    val engagementFeature: EntityEngagementFeature?
        get() = _engagementFeature

    /**
     * Feature for friend RSVPs display - initialized when agenda item loads
     */
    private var _friendRsvpsFeature: FriendRsvpsFeature? = null
    val friendRsvpsFeature: FriendRsvpsFeature?
        get() = _friendRsvpsFeature

    init {
        // Initialize engagement feature when item is loaded
        viewModelScope.launch {
            item.collect { resource ->
                if (resource is Resource.Success) {
                    val agendaItem = resource.data

                    // Initialize engagement feature only once to avoid overwriting optimistic updates
                    if (_engagementFeature == null) {
                        val newEngagementFeature = EntityEngagementFeature(
                            entityType = EntityType.AGENDAITEM,
                            entityId = agendaItem.id,
                            engagementManager = engagementManager,
                            authFeature = authFeature,
                            coroutineScope = viewModelScope
                        ).apply {
                            initialize(agendaItem.userEngagement)
                        }
                        _engagementFeature = newEngagementFeature

                        // Start watching for RSVP changes after engagement feature is initialized
                        setupEngagementWatchers(newEngagementFeature)
                    }

                    // Initialize friend RSVPs feature if authenticated
                    if (_friendRsvpsFeature == null && authFeature.isAuthenticated) {
                        _friendRsvpsFeature = FriendRsvpsFeature(
                            entityType = EntityType.AGENDAITEM,
                            entityId = agendaItem.id,
                            friendRsvpRepository = friendRsvpRepository,
                            coroutineScope = viewModelScope
                        )
                    }
                }
            }
        }

        // Check initial sync state
        _isItemSynced.value = calendarSyncManager.isAgendaItemSynced(agendaItemId)
    }

    /**
     * Set up watchers for engagement changes.
     * Called after engagement feature is initialized to ensure we have a valid feature to watch.
     */
    private fun setupEngagementWatchers(engagementFeature: EntityEngagementFeature) {
        // Refresh friend RSVPs when engagement changes
        viewModelScope.launch {
            engagementFeature.engagement.collect {
                // When user changes their RSVP, refresh friend list
                _friendRsvpsFeature?.refresh()
            }
        }

        // Watch for RSVP changes to auto-sync/unsync calendar
        viewModelScope.launch {
            var previousEngagement: UserEngagement? = null

            engagementFeature.engagement.collect { engagement ->
                // Check if status changed
                val wasGoing = previousEngagement?.status == UserEngagementStatus.GOING
                val isGoing = engagement.status == UserEngagementStatus.GOING

                when {
                    // User just RSVP'd GOING - add to calendar if auto-sync enabled
                    !wasGoing && isGoing -> {
                        handleUserRsvpGoing()
                    }
                    // User un-RSVP'd - remove from calendar
                    wasGoing && !isGoing -> {
                        handleUserUnRsvp()
                    }
                }

                previousEngagement = engagement
            }
        }
    }

    /**
     * Handle user RSVP'ing as GOING - auto-sync if enabled and calendar is set up.
     * Only syncs automatically if user has enabled the auto-sync preference.
     */
    private suspend fun handleUserRsvpGoing() {
        // Check if auto-sync is enabled
        val autoSyncEnabled = userPreferencesManager.getAutoSyncCalendar()
        if (!autoSyncEnabled) {
            Logger.d(tag, "Auto-sync disabled, skipping calendar sync")
            return
        }

        // Check if already synced
        if (_isItemSynced.value) {
            Logger.d(tag, "Item already synced, skipping")
            return
        }

        // Check if we have a preferred calendar set
        val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
        if (preferredCalendarId == null) {
            Logger.d(tag, "No preferred calendar set, skipping auto-sync")
            return
        }

        // Check permission
        if (!calendarSyncManager.hasCalendarPermission()) {
            Logger.d(tag, "No calendar permission, skipping auto-sync")
            return
        }

        Logger.d(tag, "Auto-syncing to calendar after RSVP GOING")
        syncToCalendar(preferredCalendarId)
    }

    /**
     * Handle user un-RSVP'ing - remove from calendar if synced.
     */
    private suspend fun handleUserUnRsvp() {
        if (!_isItemSynced.value) {
            Logger.d(tag, "Item not synced, nothing to remove")
            return
        }

        Logger.d(tag, "Removing from calendar after un-RSVP")
        removeFromCalendar()
    }

    // ========== Calendar Sync Methods ==========

    /**
     * Load available calendars from the device.
     */
    private suspend fun loadAvailableCalendars() {
        _isLoadingCalendars.value = true
        when (val result = calendarSyncManager.getAvailableCalendars()) {
            is CalendarResult.Success -> {
                _availableCalendars.value = result.data
            }
            is CalendarResult.Error -> {
                _calendarSyncError.value = result.message
            }
            CalendarResult.PermissionDenied -> {
                _needsCalendarPermission.value = true
            }
        }
        _isLoadingCalendars.value = false
    }

    /**
     * Called when user taps the calendar button.
     */
    fun onCalendarButtonClick() {
        viewModelScope.launch {
            _calendarSyncError.value = null
            Logger.d(tag, "onCalendarButtonClick called")

            // Check if already synced
            if (_isItemSynced.value) {
                Logger.d(tag, "Item already synced, removing from calendar")
                removeFromCalendar()
                return@launch
            }

            // Check permission first
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            if (!hasPermission) {
                Logger.d(tag, "No calendar permission, requesting...")
                _needsCalendarPermission.value = true
                return@launch
            }

            // Check if we have a preferred calendar set
            val preferredCalendarId = calendarSyncManager.getPreferredCalendarId()
            if (preferredCalendarId != null) {
                syncToCalendar(preferredCalendarId)
                return@launch
            }

            // Need to pick a calendar - show picker and load calendars
            _showCalendarPicker.value = true

            if (_availableCalendars.value.isEmpty() && !_isLoadingCalendars.value) {
                loadAvailableCalendars()
            }

            // After loading, check if we can auto-select
            val calendars = _availableCalendars.value
            if (calendars.size == 1) {
                _showCalendarPicker.value = false
                syncToCalendar(calendars.first().id)
            }
        }
    }

    /**
     * Called when user selects a calendar from the picker.
     */
    fun onCalendarSelected(calendarId: String) {
        _showCalendarPicker.value = false
        calendarSyncManager.setPreferredCalendar(calendarId)
        viewModelScope.launch {
            syncToCalendar(calendarId)
        }
    }

    /**
     * Dismiss the calendar picker.
     */
    fun dismissCalendarPicker() {
        _showCalendarPicker.value = false
    }

    /**
     * Dismiss permission request.
     */
    fun dismissPermissionRequest() {
        _needsCalendarPermission.value = false
    }

    /**
     * Clear calendar sync error.
     */
    fun clearCalendarError() {
        _calendarSyncError.value = null
    }

    /**
     * Sync the current agenda item to a calendar.
     */
    private suspend fun syncToCalendar(calendarId: String) {
        val agendaItem = (item.value as? Resource.Success)?.data ?: return
        val eventName = agendaItem.event?.name ?: "Event"

        _isCalendarSyncing.value = true
        _calendarSyncError.value = null

        when (val result = calendarSyncManager.syncAgendaItemToCalendar(agendaItem, eventName, calendarId)) {
            is CalendarResult.Success -> {
                _isItemSynced.value = true
            }
            is CalendarResult.Error -> {
                _calendarSyncError.value = result.message
            }
            CalendarResult.PermissionDenied -> {
                _needsCalendarPermission.value = true
            }
        }

        _isCalendarSyncing.value = false
    }

    /**
     * Remove agenda item from calendar.
     */
    private suspend fun removeFromCalendar() {
        _isCalendarSyncing.value = true
        _calendarSyncError.value = null

        when (val result = calendarSyncManager.removeAgendaItemFromCalendar(agendaItemId)) {
            is CalendarResult.Success -> {
                _isItemSynced.value = false
            }
            is CalendarResult.Error -> {
                _calendarSyncError.value = result.message
            }
            CalendarResult.PermissionDenied -> {
                _needsCalendarPermission.value = true
            }
        }

        _isCalendarSyncing.value = false
    }
}
