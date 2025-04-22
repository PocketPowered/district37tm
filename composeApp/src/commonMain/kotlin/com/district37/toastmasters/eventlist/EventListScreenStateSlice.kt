package com.district37.toastmasters.eventlist

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.TabInfo
import com.district37.toastmasters.models.findSelectedTab
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.util.Resource.Loading.handle
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListScreenState(
    val events: List<EventPreview> = emptyList(),
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
        MutableStateFlow(Resource.Loading)
    val screenState = _screenState

    override fun afterInit() {
        super.afterInit()
        fetchInitialData()
    }

    override fun handleUiEvent(event: UiEvent) {
        super.handleUiEvent(event)
        when (event) {
            RefreshTriggered -> {
                _screenState.value.handle(
                    onSuccess = { currentValue ->
                        val currentlySelectedKey =
                            currentValue.availableTabs.findSelectedTab().dateKey
                        fetchEventsByKey(currentlySelectedKey)
                    },
                    onError = { _, _ ->
                        fetchInitialData()
                    }
                )
            }

            is TabChanged -> {
                _screenState.update { currentValue ->
                    currentValue.map {
                        it.copy(
                            availableTabs = it.availableTabs.map { tab ->
                                tab.copy(isSelected = tab.dateKey == event.selectedTab.dateKey)
                            }
                        )
                    }
                }
                fetchEventsByKey(event.selectedTab.dateKey)
            }
        }
    }


    private fun fetchEventsByKey(key: String) {
        sliceScope.launch(Dispatchers.IO) {
            val previousState = _screenState.value
            _screenState.update { Resource.Loading }
            eventRepository.getEventsByKey(key).map { events ->
                events.map {
                    eventPreviewTransformer.transform(it)
                }
            }.handle(
                onSuccess = { availableEvents ->
                    previousState.handle(
                        onSuccess = { currentValue ->
                            _screenState.update {
                                Resource.Success(currentValue.copy(events = availableEvents))
                            }
                        },
                        onError = { error, _ ->
                            _screenState.update { Resource.Error(error) }
                        }
                    )
                },
                onError = { error, _ ->
                    _screenState.update { Resource.Error(error) }
                }
            )
        }
    }

    private fun fetchInitialData() {
        sliceScope.launch(Dispatchers.IO) {
            _screenState.update { Resource.Loading }
            eventRepository.getAvailableTabs().map { tabInfoTransformer.transform(it) }
                .handle(
                    onSuccess = { availableTabs ->
                        _screenState.update {
                            Resource.Success(
                                EventListScreenState(
                                    availableTabs = availableTabs
                                )
                            )
                        }
                        val currentlySelectedTab = availableTabs.findSelectedTab()
                        fetchEventsByKey(currentlySelectedTab.dateKey)
                    },
                    onError = { error, _ ->
                        _screenState.update { Resource.Error(error) }
                    }
                )
        }
    }
}