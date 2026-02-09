package com.district37.toastmasters.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.infra.calendar.CalendarInfo
import com.district37.toastmasters.infra.calendar.CalendarReconciliationService
import com.district37.toastmasters.infra.calendar.CalendarResult
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.infra.calendar.LocalAgendaItemSyncRecord
import com.district37.toastmasters.infra.calendar.LocalCalendarSyncRecord
import com.district37.toastmasters.infra.calendar.ReinstallReconciliationResult
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Theme options for the app
 */
enum class ThemeOption(val value: String, val displayName: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark")
}

/**
 * State for the Settings screen
 */
data class SettingsState(
    val selectedTheme: ThemeOption = ThemeOption.SYSTEM,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    // Calendar sync settings
    val hasCalendarPermission: Boolean = false,
    val availableCalendars: List<CalendarInfo> = emptyList(),
    val selectedCalendarId: String? = null,
    val isLoadingCalendars: Boolean = false,
    val needsCalendarPermission: Boolean = false,
    // Calendar sync status
    val syncedItemCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncResult: String? = null
)

/**
 * ViewModel for the Settings screen
 * Manages theme preferences, calendar sync settings, and sign out functionality
 */
class SettingsViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
    private val calendarSyncManager: CalendarSyncManager,
    private val calendarReconciliationService: CalendarReconciliationService,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    // Auto-sync calendar preference
    val autoSyncCalendar: StateFlow<Boolean> = userPreferencesManager.autoSyncCalendarFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Expose synced items from CalendarSyncManager
    val syncedEvents: StateFlow<Map<Int, LocalCalendarSyncRecord>> = calendarSyncManager.syncedEvents
    val syncedAgendaItems: StateFlow<Map<Int, LocalAgendaItemSyncRecord>> = calendarSyncManager.syncedAgendaItems

    init {
        // Observe auth state to keep theme in sync
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                if (authState is AuthState.Authenticated) {
                    val currentTheme = authState.user.preferences?.theme?.let { theme ->
                        ThemeOption.entries.find { it.value == theme }
                    } ?: ThemeOption.SYSTEM

                    // Only update if not currently saving (to avoid overwriting optimistic update)
                    if (!_state.value.isSaving) {
                        _state.update { it.copy(selectedTheme = currentTheme) }
                    }
                }
            }
        }

        // Load calendar settings
        loadCalendarSettings()
    }

    /**
     * Load calendar permission status and available calendars
     */
    private fun loadCalendarSettings() {
        viewModelScope.launch {
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            val selectedCalendarId = calendarSyncManager.getPreferredCalendarId()
            val syncedItemCount = calendarSyncManager.getSyncedItemCount()

            _state.update {
                it.copy(
                    hasCalendarPermission = hasPermission,
                    selectedCalendarId = selectedCalendarId,
                    syncedItemCount = syncedItemCount
                )
            }

            if (hasPermission) {
                loadAvailableCalendars()
            }
        }
    }

    /**
     * Load available calendars from the device
     */
    private suspend fun loadAvailableCalendars() {
        _state.update { it.copy(isLoadingCalendars = true) }

        when (val result = calendarSyncManager.getAvailableCalendars()) {
            is CalendarResult.Success -> {
                _state.update {
                    it.copy(
                        availableCalendars = result.data,
                        isLoadingCalendars = false
                    )
                }
            }
            is CalendarResult.Error -> {
                Logger.e(TAG, "Failed to load calendars: ${result.message}")
                _state.update {
                    it.copy(
                        isLoadingCalendars = false,
                        errorMessage = "Failed to load calendars"
                    )
                }
            }
            CalendarResult.PermissionDenied -> {
                _state.update {
                    it.copy(
                        hasCalendarPermission = false,
                        isLoadingCalendars = false,
                        needsCalendarPermission = true
                    )
                }
            }
        }
    }

    /**
     * Select a calendar for syncing events
     */
    fun onCalendarSelected(calendarId: String) {
        calendarSyncManager.setPreferredCalendar(calendarId)
        _state.update { it.copy(selectedCalendarId = calendarId) }
        Logger.d(TAG, "Selected calendar: $calendarId")
    }

    /**
     * Clear selected calendar
     */
    fun clearSelectedCalendar() {
        _state.update { it.copy(selectedCalendarId = null) }
        // Note: CalendarSyncManager doesn't have a clear method, but we can set to null in UI
    }

    /**
     * Refresh calendar list
     */
    fun refreshCalendars() {
        viewModelScope.launch {
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            _state.update { it.copy(hasCalendarPermission = hasPermission) }

            if (hasPermission) {
                loadAvailableCalendars()
            } else {
                _state.update { it.copy(needsCalendarPermission = true) }
            }
        }
    }

    /**
     * Called when calendar permission is granted.
     * Refreshes calendars and runs reinstall reconciliation if this is first launch after install.
     */
    fun onCalendarPermissionGranted() {
        viewModelScope.launch {
            _state.update { it.copy(hasCalendarPermission = true) }
            loadAvailableCalendars()

            // Check if we need to run reinstall reconciliation
            // This handles the case where app was reinstalled but permissions weren't granted yet
            if (userPreferencesManager.isFirstLaunchAfterInstall()) {
                Logger.d(TAG, "Permission granted on first launch, running reinstall reconciliation")
                runReinstallReconciliation()
            }
        }
    }

    /**
     * Run reinstall reconciliation to re-link calendar events after app reinstall.
     */
    private suspend fun runReinstallReconciliation() {
        val shouldMarkComplete = when (val result = calendarReconciliationService.reconcileOnReinstall()) {
            is ReinstallReconciliationResult.Success -> {
                Logger.d(TAG, "Reinstall reconciliation: relinked=${result.relinkedCount}, needsResync=${result.needsResyncCount}, errors=${result.errorCount}")
                if (result.hasIssues) {
                    _state.update {
                        it.copy(lastSyncResult = "Some calendar events may need to be re-synced")
                    }
                } else if (result.relinkedCount > 0) {
                    _state.update {
                        it.copy(lastSyncResult = "Re-linked ${result.relinkedCount} calendar event(s)")
                    }
                }
                // Refresh synced item count
                val newCount = calendarSyncManager.getSyncedItemCount()
                _state.update { it.copy(syncedItemCount = newCount) }
                true
            }
            is ReinstallReconciliationResult.NoRecords -> {
                Logger.d(TAG, "No sync records on server, nothing to reconcile")
                true
            }
            is ReinstallReconciliationResult.PermissionDenied -> {
                Logger.d(TAG, "Calendar permission denied during reconciliation")
                false
            }
            is ReinstallReconciliationResult.Error -> {
                Logger.e(TAG, "Reinstall reconciliation error: ${result.message}")
                false
            }
        }

        if (shouldMarkComplete) {
            userPreferencesManager.markFirstLaunchCompleted()
        }
    }

    /**
     * Refresh all calendar settings including calendars and synced items
     */
    fun refreshCalendarSettings() {
        viewModelScope.launch {
            val hasPermission = calendarSyncManager.hasCalendarPermission()
            val selectedCalendarId = calendarSyncManager.getPreferredCalendarId()
            val syncedItemCount = calendarSyncManager.getSyncedItemCount()

            _state.update {
                it.copy(
                    hasCalendarPermission = hasPermission,
                    selectedCalendarId = selectedCalendarId,
                    syncedItemCount = syncedItemCount
                )
            }

            if (hasPermission) {
                loadAvailableCalendars()
            }
        }
    }

    /**
     * Dismiss permission request
     */
    fun dismissPermissionRequest() {
        _state.update { it.copy(needsCalendarPermission = false) }
    }

    /**
     * Manually trigger calendar sync reconciliation.
     * This checks all synced items against the server and fixes any discrepancies.
     */
    fun syncNow() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, lastSyncResult = null) }

            try {
                val result = calendarReconciliationService.reconcileAll()
                val resultMessage = when {
                    result.errors > 0 -> "Sync completed with ${result.errors} error(s)"
                    result.deleted == 0 && result.updated == 0 -> "All items are synced"
                    else -> "Fixed ${result.deleted + result.updated} sync issue(s)"
                }

                // Refresh synced item count
                val newCount = calendarSyncManager.getSyncedItemCount()

                _state.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncResult = resultMessage,
                        syncedItemCount = newCount
                    )
                }
                Logger.d(TAG, "Manual sync completed: $resultMessage")
            } catch (e: Exception) {
                Logger.e(TAG, "Manual sync failed: ${e.message}")
                _state.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncResult = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Clear the last sync result message
     */
    fun clearSyncResult() {
        _state.update { it.copy(lastSyncResult = null) }
    }

    /**
     * Update the theme preference
     */
    fun onThemeChanged(theme: ThemeOption) {
        if (_state.value.selectedTheme == theme) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, selectedTheme = theme) }

            val result = userProfileRepository.updateMyPreferences(theme = theme.value)

            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    // Refresh auth state to get updated preferences
                    authRepository.refreshAuthState()
                    Logger.d(TAG, "Theme updated to: ${theme.value}")
                }
                is Resource.Error -> {
                    Logger.e(TAG, "Failed to update theme: ${result.message}")
                    // Revert the optimistic update by refreshing from auth state
                    val currentAuthState = authRepository.authState.value
                    val revertedTheme = if (currentAuthState is AuthState.Authenticated) {
                        currentAuthState.user.preferences?.theme?.let { t ->
                            ThemeOption.entries.find { it.value == t }
                        } ?: ThemeOption.SYSTEM
                    } else {
                        ThemeOption.SYSTEM
                    }
                    _state.update {
                        it.copy(
                            isSaving = false,
                            selectedTheme = revertedTheme,
                            errorMessage = result.message ?: "Failed to update theme"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Sign out the user
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    /**
     * Toggle the auto-sync calendar preference.
     * When enabled, RSVP'd agenda items are automatically synced to device calendar.
     */
    fun toggleAutoSyncCalendar() {
        viewModelScope.launch {
            val newValue = !autoSyncCalendar.value
            userPreferencesManager.setAutoSyncCalendar(newValue)
            Logger.d(TAG, "Auto-sync calendar preference set to: $newValue")
        }
    }

    /**
     * Clear the error message
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
