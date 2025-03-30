package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.models.Event
import com.district37.toastmasters.EventRepository
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailsScreenState(
    val event: Event
)

class EventDetailsScreenStateSlice(
    private val eventRepository: EventRepository
): ViewModelSlice() {

    private val _screenState: MutableStateFlow<Resource<EventDetailsScreenState>> =
        MutableStateFlow(Resource.Loading())
    val screenState = _screenState

    /**
     * May be refreshing more than necessary, consider
     * caching last known event or finding a better way to
     * initialize this viewmodel
     */
    fun initialize(eventId: Int) {
        sliceScope.launch(Dispatchers.IO) {
            val eventDetails = eventRepository.getEventDetails(eventId)
            _screenState.update {
                eventDetails.map { eventRes ->
                    EventDetailsScreenState(eventRes)
                }
            }
        }
    }
}