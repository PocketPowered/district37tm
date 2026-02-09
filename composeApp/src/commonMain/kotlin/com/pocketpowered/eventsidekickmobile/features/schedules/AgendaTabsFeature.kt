package com.district37.toastmasters.features.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.models.AgendaDate
import com.district37.toastmasters.models.AgendaItemConnection
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Reusable feature for managing agenda date tabs with lazy-loaded agenda items.
 * Can be used by any ViewModel that needs to display agenda items organized by date.
 *
 * This feature handles:
 * - Storing the list of agenda dates (provided by the parent, e.g., from event details)
 * - Lazy loading agenda items when a date tab is selected
 * - Pagination of agenda items within each date
 * - Persisting the "My Schedule" filter preference
 *
 * @param agendaItemRepository Repository for fetching agenda items
 * @param coroutineScope Scope for launching coroutines (typically viewModelScope)
 * @param eventId The event ID to fetch agenda items for
 * @param userPreferencesManager Manager for persisting user preferences (optional)
 */
class AgendaTabsFeature(
    private val agendaItemRepository: AgendaItemRepository,
    private val coroutineScope: CoroutineScope,
    private val eventId: Int,
    private val userPreferencesManager: UserPreferencesManager? = null
) {
    /**
     * List of agenda dates to display as tabs
     */
    private val _agendaDates = MutableStateFlow<List<AgendaDate>>(emptyList())
    val agendaDates: StateFlow<List<AgendaDate>> = _agendaDates.asStateFlow()

    /**
     * Map of date to agenda items connection
     * Contains pagination info (hasNextPage, endCursor, totalCount)
     */
    private val _agendaItems = MutableStateFlow<Map<LocalDate, Resource<AgendaItemConnection>>>(emptyMap())
    val agendaItems: StateFlow<Map<LocalDate, Resource<AgendaItemConnection>>> = _agendaItems.asStateFlow()

    /**
     * Set of dates currently loading more items
     */
    private val _isLoadingMoreItems = MutableStateFlow<Set<LocalDate>>(emptySet())
    val isLoadingMoreItems: StateFlow<Set<LocalDate>> = _isLoadingMoreItems.asStateFlow()

    /**
     * Currently selected tab index - managed here to persist across recompositions
     */
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    /**
     * Whether to show only items the user has RSVP'd "GOING" to (My Schedule filter)
     */
    private val _myScheduleOnly = MutableStateFlow(false)
    val myScheduleOnly: StateFlow<Boolean> = _myScheduleOnly.asStateFlow()

    init {
        // Load persisted myScheduleOnly preference on initialization
        userPreferencesManager?.let { prefs ->
            coroutineScope.launch(Dispatchers.IO) {
                val persistedValue = prefs.getMyScheduleOnly()
                _myScheduleOnly.value = persistedValue
            }
        }
    }

    /**
     * Initialize the feature with agenda dates from the event
     *
     * @param agendaDates List of agenda dates from the event
     */
    fun initialize(agendaDates: List<AgendaDate>) {
        clearCache()
        _agendaDates.update { agendaDates }
        // Reset tab selection and load first date's items + preload adjacent
        _selectedTabIndex.update { 0 }
        if (agendaDates.isNotEmpty()) {
            loadAgendaItemsForDate(agendaDates.first().date)
            preloadAdjacentDates(0)
        }
    }

    /**
     * Get the currently selected date, if any
     */
    fun getSelectedDate(): LocalDate? {
        val index = _selectedTabIndex.value
        val datesList = _agendaDates.value
        return if (index in datesList.indices) datesList[index].date else null
    }

    /**
     * Select a tab by index and load its agenda items
     * This is the primary way to change tabs - handles both state update and data loading
     *
     * @param index The tab index to select
     */
    fun selectTab(index: Int) {
        if (index < 0 || index >= _agendaDates.value.size) return
        _selectedTabIndex.update { index }
        val date = _agendaDates.value[index].date
        loadAgendaItemsForDate(date)
        preloadAdjacentDates(index)
    }

    /**
     * Preload agenda items for adjacent dates (for smoother swiping)
     * Called when a tab is selected to ensure adjacent pages have data ready
     *
     * @param currentIndex The currently selected tab index
     */
    private fun preloadAdjacentDates(currentIndex: Int) {
        val dates = _agendaDates.value

        // Preload previous date
        if (currentIndex > 0) {
            loadAgendaItemsForDate(dates[currentIndex - 1].date)
        }

        // Preload next date
        if (currentIndex < dates.size - 1) {
            loadAgendaItemsForDate(dates[currentIndex + 1].date)
        }
    }

    /**
     * Load agenda items for a specific date
     * Called when a user selects a date tab
     *
     * @param date The date to load items for
     */
    fun loadAgendaItemsForDate(date: LocalDate) {
        coroutineScope.launch(Dispatchers.IO) {
            // Only load if not already loaded or loading
            if (!_agendaItems.value.containsKey(date)) {
                // Update the map to show loading for this date
                _agendaItems.update { it + (date to Resource.Loading) }

                val result = agendaItemRepository.getAgendaItemsByEventAndDate(
                    eventId = eventId,
                    date = date,
                    myScheduleOnly = _myScheduleOnly.value
                )

                // Update the map with the connection result
                _agendaItems.update { it + (date to result) }
            }
        }
    }

    /**
     * Load more agenda items for a specific date (pagination)
     * Called when user scrolls to the bottom of the agenda items list
     *
     * @param date The date to load more items for
     */
    fun loadMoreAgendaItemsForDate(date: LocalDate) {
        // Get current state for this date
        val currentState = _agendaItems.value[date]
        if (currentState !is Resource.Success) return

        val connection = currentState.data
        if (!connection.hasNextPage) return

        // Don't load if already loading more for this date
        if (_isLoadingMoreItems.value.contains(date)) return

        coroutineScope.launch(Dispatchers.IO) {
            _isLoadingMoreItems.update { it + date }

            val result = agendaItemRepository.getAgendaItemsByEventAndDate(
                eventId = eventId,
                date = date,
                cursor = connection.endCursor,
                myScheduleOnly = _myScheduleOnly.value
            )

            if (result is Resource.Success) {
                // Append new items to existing items
                val newConnection = AgendaItemConnection(
                    agendaItems = connection.agendaItems + result.data.agendaItems,
                    hasNextPage = result.data.hasNextPage,
                    endCursor = result.data.endCursor,
                    totalCount = result.data.totalCount
                )
                _agendaItems.update { it + (date to Resource.Success(newConnection)) }
            }

            _isLoadingMoreItems.update { it - date }
        }
    }

    /**
     * Clear all cached agenda items (useful for refresh)
     */
    fun clearCache() {
        _agendaItems.update { emptyMap() }
    }

    /**
     * Refresh items for the currently selected date
     */
    fun refreshCurrentDate() {
        getSelectedDate()?.let { date ->
            _agendaItems.update { it - date }
            loadAgendaItemsForDate(date)
        }
    }

    /**
     * Toggle the "My Schedule" filter on/off.
     * When toggled, clears the cache and reloads the current date with the new filter.
     * The preference is persisted locally.
     */
    fun toggleMySchedule() {
        val newValue = !_myScheduleOnly.value
        _myScheduleOnly.update { newValue }

        // Persist the preference
        userPreferencesManager?.let { prefs ->
            coroutineScope.launch(Dispatchers.IO) {
                prefs.setMyScheduleOnly(newValue)
            }
        }

        // Clear cache so items are reloaded with new filter
        clearCache()
        // Reload current date
        getSelectedDate()?.let { date ->
            loadAgendaItemsForDate(date)
        }
    }

    /**
     * Set the "My Schedule" filter explicitly.
     * The preference is persisted locally.
     * @param enabled Whether to enable the filter
     */
    fun setMyScheduleOnly(enabled: Boolean) {
        if (_myScheduleOnly.value != enabled) {
            _myScheduleOnly.update { enabled }

            // Persist the preference
            userPreferencesManager?.let { prefs ->
                coroutineScope.launch(Dispatchers.IO) {
                    prefs.setMyScheduleOnly(enabled)
                }
            }

            // Clear cache so items are reloaded with new filter
            clearCache()
            // Reload current date
            getSelectedDate()?.let { date ->
                loadAgendaItemsForDate(date)
            }
        }
    }
}

/**
 * Immutable snapshot of all AgendaTabsFeature state.
 * Use [rememberAgendaTabsState] to collect this from a feature instance.
 */
data class AgendaTabsState(
    val agendaDates: List<AgendaDate>,
    val agendaItemsMap: Map<LocalDate, Resource<AgendaItemConnection>>,
    val isLoadingMoreItems: Set<LocalDate>,
    val selectedTabIndex: Int,
    val myScheduleOnly: Boolean
) {
    /**
     * Count of active schedule filters.
     * Scalable: add new filter checks here as they are added.
     */
    val activeFilterCount: Int
        get() = listOf(
            myScheduleOnly
            // Add future filters here, e.g.: someOtherFilter, anotherFilter
        ).count { it }

    companion object {
        val Empty = AgendaTabsState(
            agendaDates = emptyList(),
            agendaItemsMap = emptyMap(),
            isLoadingMoreItems = emptySet(),
            selectedTabIndex = 0,
            myScheduleOnly = false
        )
    }
}

/**
 * Collects all state from an AgendaTabsFeature into a single AgendaTabsState.
 * This reduces boilerplate in composables that need to observe multiple state flows.
 *
 * Usage:
 * ```
 * val agendaState = rememberAgendaTabsState(viewModel.agendaTabsFeature)
 * AgendaTabs(
 *     state = agendaState,
 *     feature = viewModel.agendaTabsFeature,
 *     ...
 * )
 * ```
 */
@Composable
fun rememberAgendaTabsState(feature: AgendaTabsFeature): AgendaTabsState {
    val agendaDates by feature.agendaDates.collectAsState()
    val agendaItemsMap by feature.agendaItems.collectAsState()
    val isLoadingMoreItems by feature.isLoadingMoreItems.collectAsState()
    val selectedTabIndex by feature.selectedTabIndex.collectAsState()
    val myScheduleOnly by feature.myScheduleOnly.collectAsState()

    return AgendaTabsState(
        agendaDates = agendaDates,
        agendaItemsMap = agendaItemsMap,
        isLoadingMoreItems = isLoadingMoreItems,
        selectedTabIndex = selectedTabIndex,
        myScheduleOnly = myScheduleOnly
    )
}
