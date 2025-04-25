package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.LocationsRepository
import com.district37.toastmasters.models.EventDetails
import com.district37.toastmasters.models.Location
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

data class EventDetailsScreenState(
    val event: EventDetails,
    val location: Location? = null
)

class EventDetailsScreenStateSlice(
    private val eventRepository: EventRepository,
    private val locationsRepository: LocationsRepository,
    private val eventDetailsTransformer: EventDetailsTransformer
) : ViewModelSlice() {

    private val _eventDetails: MutableStateFlow<Resource<EventDetails>> =
        MutableStateFlow(Resource.Loading)
    private val _location: MutableStateFlow<Resource<Location?>> =
        MutableStateFlow(Resource.NotLoading)

    private val _screenState: StateFlow<Resource<EventDetailsScreenState>> = combine(
        _eventDetails,
        _location
    ) { eventDetails, location ->
        when (eventDetails) {
            is Resource.Loading -> Resource.Loading
            is Resource.Error -> Resource.Error(eventDetails.error)
            is Resource.Success -> {
                // Location is optional, so we can still have a success state even if location search failed
                val locationResult = when (location) {
                    is Resource.Success -> location.data
                    else -> null // Treat any other state as no location found
                }
                Resource.Success(
                    EventDetailsScreenState(
                        event = eventDetails.data,
                        location = locationResult
                    )
                )
            }
            else -> Resource.Error(ErrorType.CLIENT_ERROR)
        }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, Resource.Loading)

    val screenState = _screenState

    fun initialize(eventId: Int) {
        fetchEventDetails(eventId)
    }

    private fun fetchEventDetails(eventId: Int) {
        sliceScope.launch(Dispatchers.IO) {
            _eventDetails.update { Resource.Loading }
            _location.update { Resource.NotLoading }

            eventRepository.getEventDetails(eventId)
                .map { backendEventDetails ->
                    eventDetailsTransformer.transform(backendEventDetails)
                }
                .handle(
                    onSuccess = { eventDetails ->
                        _eventDetails.update { Resource.Success(eventDetails) }
                        // If we have location info, try to find a matching location
                        fetchLocation(eventDetails.locationInfo)
                    },
                    onError = { error, _ ->
                        _eventDetails.update { Resource.Error(error) }
                    }
                )
        }
    }

    private fun fetchLocation(locationInfo: String) {
        sliceScope.launch(Dispatchers.IO) {
            locationsRepository.searchLocationsByName(locationInfo)
                .handle(
                    onSuccess = { locations ->
                        _location.update { Resource.Success(locations.firstOrNull()) }
                    },
                    onError = { _, _ ->
                        // Location search failed, but that's okay - just set to null
                        _location.update { Resource.Success(null) }
                    }
                )
        }
    }
}