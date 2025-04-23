package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.models.EventDetails
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailsScreenState(
    val event: EventDetails
)

class EventDetailsScreenStateSlice(
    private val eventRepository: EventRepository,
    private val eventDetailsTransformer: EventDetailsTransformer
) : ViewModelSlice() {

    private val _screenState: MutableStateFlow<Resource<EventDetailsScreenState>> =
        MutableStateFlow(Resource.Loading)
    val screenState = _screenState

    fun initialize(eventId: Int) {
        sliceScope.launch(Dispatchers.IO) {
            val eventDetails = eventRepository.getEventDetails(eventId)
            _screenState.update {
                eventDetails.map { backendEventDetails ->
                    eventDetailsTransformer.transform(backendEventDetails)?.let {
                        EventDetailsScreenState(it)
                    }
                }
            }
        }
    }
}