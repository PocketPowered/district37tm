package com.district37.toastmasters.eventlist

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.TabInfo
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListScreenState(
    val events: List<EventPreview> = emptyList(),
    val currentlySelectedTabKey: String,
    val availableTabs: List<TabInfo>
)

object RefreshTriggered : UiEvent

data class TabChanged(
    val selectedTab: TabInfo
) : UiEvent

class EventListScreenStateSlice(
    private val eventRepository: EventRepository,
    private val eventPreviewTransformer: EventPreviewTransformer,
    private val tabInfoTransformer: TabInfoTransformer
) : ViewModelSlice() {

    private val _screenState: MutableStateFlow<Resource<EventListScreenState>> =
        MutableStateFlow(Resource.Loading())
    val screenState = _screenState

    override fun afterInit() {
        super.afterInit()
        fetchInitialData()
    }

    override fun handleUiEvent(event: UiEvent) {
        super.handleUiEvent(event)
        when (event) {
            RefreshTriggered -> {
                val currentScreenStateData = _screenState.value.data
                _screenState.update {
                    Resource.Loading()
                }
                if (currentScreenStateData != null) {
                    fetchEventsByKey(
                        currentScreenStateData.currentlySelectedTabKey,
                        currentScreenStateData.availableTabs
                    )
                } else if (_screenState.value is Resource.Error) {
                    fetchInitialData()
                }
            }

            is TabChanged -> {
                val currentScreenStateData = _screenState.value.data
                _screenState.update {
                    Resource.Loading()
                }
                val currentTabsState = currentScreenStateData?.availableTabs ?: emptyList()
                val newTabState = currentTabsState.map {
                    TabInfo(
                        it.displayName,
                        it.dateKey,
                        it.dateKey == event.selectedTab.dateKey
                    )
                }
                fetchEventsByKey(event.selectedTab.dateKey, newTabState)
            }
        }
    }


    private fun fetchEventsByKey(key: String, availableTabs: List<TabInfo>) {
        sliceScope.launch(Dispatchers.IO) {
            val eventsForSelectedTab =
                eventRepository.getEventsByKey(key).map { events ->
                    events.map {
                        eventPreviewTransformer.transform(it)
                    }
                }
            if (eventsForSelectedTab is Resource.Success) {
                _screenState.update {
                    Resource.Success(
                        EventListScreenState(
                            events = eventsForSelectedTab.data,
                            currentlySelectedTabKey = key,
                            availableTabs = availableTabs
                        )
                    )
                }
            } else {
                _screenState.update { Resource.Error(ErrorType.UNKNOWN) }
            }
        }
    }

    private fun fetchInitialData() {
        sliceScope.launch(Dispatchers.IO) {
            _screenState.update { Resource.Loading() }
            val availableTabsRes =
                eventRepository.getAvailableTabs().map { tabInfoTransformer.transform(it) }
            if (availableTabsRes is Resource.Error) {
                _screenState.update { Resource.Error(availableTabsRes.errorType) }
                return@launch
            } else if (availableTabsRes is Resource.Success) {
                val availableTabs = availableTabsRes.data
                val selectedTab =
                    availableTabs.find { it.isSelected }
                        ?: throw IllegalStateException("No selected tab!")
                fetchEventsByKey(selectedTab.dateKey, availableTabs)
            }
        }
    }
}