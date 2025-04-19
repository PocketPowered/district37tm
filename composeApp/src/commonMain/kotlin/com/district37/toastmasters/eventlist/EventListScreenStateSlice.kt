package com.district37.toastmasters.eventlist

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.models.EventPreview
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListScreenState(
    val events: List<EventPreview> = emptyList(),
    val isCurrentTabFriday: Boolean
)

object RefreshTriggered: UiEvent

data class TabChanged(
    val isFriday: Boolean
): UiEvent

class EventListScreenStateSlice(
    private val eventRepository: EventRepository,
    private val eventPreviewTransformer: EventPreviewTransformer
) : ViewModelSlice() {

    private val _screenState: MutableStateFlow<Resource<EventListScreenState>> =
        MutableStateFlow(Resource.Loading())
    val screenState = _screenState

    override fun afterInit() {
        super.afterInit()
        fetchData(isFriday = true)
    }

    override fun handleUiEvent(event: UiEvent) {
        super.handleUiEvent(event)
        when (event) {
            RefreshTriggered -> {
                val currentTabSelection = _screenState.value.data?.isCurrentTabFriday ?: true
                _screenState.update {
                    Resource.Loading()
                }
                fetchData(isFriday = currentTabSelection)
            }
            is TabChanged -> {
                _screenState.update {
                    Resource.Loading()
                }
                fetchData(isFriday = event.isFriday)
            }
        }
    }

    private fun fetchData(isFriday: Boolean) {
        sliceScope.launch(Dispatchers.IO) {
            val events = eventRepository.getEvents(isFriday)
            _screenState.update {
                events.map { backendEventPreviews ->
                    val mappedEvents = backendEventPreviews.map(eventPreviewTransformer::transform)
                    EventListScreenState(mappedEvents, isFriday)
                }
            }
        }
    }
}