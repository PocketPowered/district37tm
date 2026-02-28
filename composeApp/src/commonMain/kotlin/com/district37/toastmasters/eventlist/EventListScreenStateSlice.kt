package com.district37.toastmasters.eventlist

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.database.FavoritesRepository
import com.district37.toastmasters.models.DateTabInfo
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.findSelectedTabOrNull
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.util.Resource.Loading.handle
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListScreenState(
    val events: List<EventPreview> = emptyList(),
    val agendaOption: AgendaOption = AgendaOption.FULL_AGENDA,
    val availableTabs: List<DateTabInfo>,
    val isScheduleLoading: Boolean = false
)

object RefreshTriggered : UiEvent

data class AgendaChanged(
    val selectedAgenda: AgendaOption
) : UiEvent

data class DateChanged(
    val selectedDateTab: DateTabInfo
) : UiEvent

enum class AgendaOption {
    FULL_AGENDA,
    FAVORITES_AGENDA
}

class EventListScreenStateSlice(
    private val eventRepository: EventRepository,
    private val eventPreviewTransformer: EventPreviewTransformer,
    private val favoritesRepository: FavoritesRepository,
    private val dateTransformer: DateTransformer,
) : ViewModelSlice() {

    private val _agendaSelection: MutableStateFlow<AgendaOption> =
        MutableStateFlow(AgendaOption.FULL_AGENDA)
    private val _availableTabs: MutableStateFlow<Resource<List<DateTabInfo>>> =
        MutableStateFlow(Resource.Loading)
    private val _eventsByDateKey: MutableStateFlow<Map<Long, List<EventPreview>>> =
        MutableStateFlow(emptyMap())
    private val _loadingDateKeys: MutableStateFlow<Set<Long>> =
        MutableStateFlow(emptySet())

    private val _screenState: StateFlow<Resource<EventListScreenState>> = combine(
        _availableTabs,
        _eventsByDateKey,
        _loadingDateKeys,
        favoritesRepository.getAllFavorites(),
        _agendaSelection
    ) { availableTabs, eventsByDateKey, loadingDateKeys, favoritedEventIds, agendaOption ->
        when (availableTabs) {
            is Resource.Loading -> Resource.Loading
            is Resource.Error -> Resource.Error(ErrorType.CLIENT_ERROR)
            is Resource.Success -> {
                val selectedTab = availableTabs.data.findSelectedTabOrNull()
                if (selectedTab == null) {
                    Resource.Success(
                        EventListScreenState(
                            events = emptyList(),
                            availableTabs = availableTabs.data,
                            agendaOption = agendaOption,
                            isScheduleLoading = false
                        )
                    )
                } else {
                    val selectedEvents = eventsByDateKey[selectedTab.dateKey].orEmpty()
                    val withFavoriteState = selectedEvents.map { eventPreview ->
                        eventPreview.copy(
                            isFavorited = favoritedEventIds.contains(eventPreview.id.toLong())
                        )
                    }
                    val visibleEvents = if (agendaOption == AgendaOption.FULL_AGENDA) {
                        withFavoriteState
                    } else {
                        withFavoriteState.filter { it.isFavorited }
                    }

                    Resource.Success(
                        EventListScreenState(
                            events = visibleEvents,
                            availableTabs = availableTabs.data,
                            agendaOption = agendaOption,
                            isScheduleLoading = loadingDateKeys.contains(selectedTab.dateKey)
                        )
                    )
                }
            }
            else -> Resource.Error(ErrorType.CLIENT_ERROR)
        }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, Resource.Loading)


    val screenState = _screenState

    override fun afterInit() {
        super.afterInit()
        fetchAvailableTabs()
    }

    override fun handleUiEvent(event: UiEvent) {
        super.handleUiEvent(event)
        when (event) {
            RefreshTriggered -> {
                _screenState.value.handle(
                    onSuccess = { currentValue ->
                        val selectedTab = currentValue.availableTabs.findSelectedTabOrNull()
                        if (selectedTab != null) {
                            preloadSelectedAndAdjacentDates(
                                availableTabs = currentValue.availableTabs,
                                forceReload = true
                            )
                        } else {
                            fetchAvailableTabs()
                        }
                    },
                    onError = { _, _ ->
                        fetchAvailableTabs()
                    }
                )
            }

            is DateChanged -> {
                _availableTabs.update { availableTabsRes ->
                    availableTabsRes.map { availableTabs ->
                        availableTabs.map { tab ->
                            tab.copy(isSelected = tab.dateKey == event.selectedDateTab.dateKey)
                        }
                    }
                }
                val tabs = (_availableTabs.value as? Resource.Success)?.data.orEmpty()
                preloadSelectedAndAdjacentDates(
                    availableTabs = tabs,
                    forceReload = false
                )
            }

            is AgendaChanged -> {
                _agendaSelection.update { event.selectedAgenda }
            }
        }
    }


    private fun fetchEventsByDate(key: Long, forceReload: Boolean) {
        if (!forceReload && _eventsByDateKey.value.containsKey(key)) {
            return
        }
        if (_loadingDateKeys.value.contains(key)) {
            return
        }

        _loadingDateKeys.update { currentlyLoading -> currentlyLoading + key }
        sliceScope.launch(Dispatchers.IO) {
            eventRepository.getEventsByKey(key).map { events ->
                events.mapNotNull {
                    eventPreviewTransformer.transform(it)
                }
            }.handle(
                onSuccess = { availableEvents ->
                    _eventsByDateKey.update { current ->
                        current + (key to availableEvents)
                    }
                    _loadingDateKeys.update { currentlyLoading ->
                        currentlyLoading - key
                    }
                },
                onError = { _, _ ->
                    _loadingDateKeys.update { currentlyLoading ->
                        currentlyLoading - key
                    }
                }
            )
        }
    }

    private fun fetchAvailableTabs() {
        sliceScope.launch(Dispatchers.IO) {
            _availableTabs.update { Resource.Loading }
            eventRepository.getAvailableDates().map { dateTransformer.transform(it) }
                .handle(
                    onSuccess = { availableTabs ->
                        _availableTabs.update {
                            Resource.Success(availableTabs)
                        }
                        preloadSelectedAndAdjacentDates(
                            availableTabs = availableTabs,
                            forceReload = false
                        )
                    },
                    onError = { error, _ ->
                        _availableTabs.update { Resource.Error(error) }
                    }
                )
        }
    }

    private fun preloadSelectedAndAdjacentDates(
        availableTabs: List<DateTabInfo>,
        forceReload: Boolean
    ) {
        if (availableTabs.isEmpty()) {
            return
        }

        val selectedIndex = availableTabs.indexOfFirst { it.isSelected }.let { idx ->
            if (idx >= 0) idx else 0
        }

        val dateKeysToLoad = buildList {
            add(availableTabs[selectedIndex].dateKey)
            if (selectedIndex > 0) {
                add(availableTabs[selectedIndex - 1].dateKey)
            }
            if (selectedIndex < availableTabs.lastIndex) {
                add(availableTabs[selectedIndex + 1].dateKey)
            }
        }.distinct()

        dateKeysToLoad.forEach { dateKey ->
            fetchEventsByDate(dateKey, forceReload = forceReload)
        }
    }
}
