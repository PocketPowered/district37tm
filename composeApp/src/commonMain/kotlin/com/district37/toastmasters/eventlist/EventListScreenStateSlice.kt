package com.district37.toastmasters.eventlist

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.database.FavoritesRepository
import com.district37.toastmasters.models.DateTabInfo
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.findSelectedTab
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
    val availableTabs: List<DateTabInfo>
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
    private val _eventsList: MutableStateFlow<Resource<List<EventPreview>>> =
        MutableStateFlow(Resource.NotLoading)

    private val _screenState: StateFlow<Resource<EventListScreenState>> = combine(
        _availableTabs, _eventsList, favoritesRepository.getAllFavorites(), _agendaSelection
    ) { availableTabs, eventList, favoritedEventIds, agendaOption ->
        if (availableTabs is Resource.Loading || eventList is Resource.Loading) {
            return@combine Resource.Loading
        }
        if (availableTabs is Resource.Error || eventList is Resource.Error) {
            return@combine Resource.Error(ErrorType.CLIENT_ERROR)
        }
        if (availableTabs is Resource.Success && eventList is Resource.NotLoading) {
            val selectedTabKey = availableTabs.data.findSelectedTab().dateKey
            fetchEventsByDate(selectedTabKey)
            return@combine Resource.Loading
        }
        if (availableTabs is Resource.Success && eventList is Resource.Success) {
            val favoritedTaggedEvents =
                eventList.data.map { eventPreview ->
                    eventPreview.copy(
                        isFavorited = favoritedEventIds.contains(
                            eventPreview.id.toLong()
                        )
                    )
                }
            val filteredByFavorites = favoritedTaggedEvents.filter {
                it.isFavorited
            }
            return@combine Resource.Success(
                EventListScreenState(
                    events = if (agendaOption == AgendaOption.FULL_AGENDA) favoritedTaggedEvents else filteredByFavorites,
                    availableTabs = availableTabs.data,
                    agendaOption = agendaOption
                )
            )
        }
        Resource.Error(ErrorType.CLIENT_ERROR)
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
                        val currentlySelectedKey =
                            currentValue.availableTabs.findSelectedTab().dateKey
                        fetchEventsByDate(currentlySelectedKey)
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
                fetchEventsByDate(event.selectedDateTab.dateKey)
            }

            is AgendaChanged -> {
                _agendaSelection.update { event.selectedAgenda }
            }
        }
    }


    private fun fetchEventsByDate(key: Long) {
        sliceScope.launch(Dispatchers.IO) {
            _eventsList.update {
                Resource.Loading
            }
            eventRepository.getEventsByKey(key).map { events ->
                events.mapNotNull {
                    eventPreviewTransformer.transform(it)
                }
            }.handle(
                onSuccess = { availableEvents ->
                    _eventsList.update {
                        Resource.Success(availableEvents)
                    }
                },
                onError = { error, _ ->
                    _eventsList.update { Resource.Error(error) }
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
                    },
                    onError = { error, _ ->
                        _availableTabs.update { Resource.Error(error) }
                    }
                )
        }
    }
}