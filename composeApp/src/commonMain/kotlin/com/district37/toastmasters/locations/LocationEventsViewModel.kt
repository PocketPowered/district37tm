package com.district37.toastmasters.locations

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.LocationsRepository
import com.district37.toastmasters.eventlist.EventPreviewTransformer
import com.district37.toastmasters.models.EventPreview
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationEventsViewModel(
    private val eventRepository: EventRepository,
    private val locationsRepository: LocationsRepository,
    private val eventPreviewTransformer: EventPreviewTransformer,
    private val searchLocationNodeTransformer: SearchLocationNodeTransformer,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _events = MutableStateFlow<Resource<List<EventPreview>>>(Resource.Loading)
    val events: StateFlow<Resource<List<EventPreview>>> = _events.asStateFlow()

    private val _locationImages = MutableStateFlow<List<String>>(emptyList())
    val locationImages: StateFlow<List<String>> = _locationImages.asStateFlow()

    fun initialize(locationName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.update { Resource.Loading }

            coroutineScope {
                val imagesDeferred = async { loadLocationImages(locationName) }
                val eventsDeferred = async { loadFilteredEvents(locationName) }

                _locationImages.update { imagesDeferred.await() }
                _events.update { eventsDeferred.await() }
            }
        }
    }

    private suspend fun loadLocationImages(locationName: String): List<String> {
        val result = locationsRepository.searchLocationsByName(locationName)
        if (result !is Resource.Success) return emptyList()
        return result.data
            .map { searchLocationNodeTransformer.transform(it) }
            .firstOrNull { it.locationName == locationName }
            ?.locationImages
            ?: emptyList()
    }

    private suspend fun loadFilteredEvents(locationName: String): Resource<List<EventPreview>> {
        val datesResource = eventRepository.getAvailableDates()
        if (datesResource !is Resource.Success) {
            return datesResource as? Resource.Error ?: Resource.Loading
        }
        val filtered = datesResource.data.flatMap { dateKey ->
            val result = eventRepository.getEventsByKey(dateKey)
            if (result is Resource.Success) result.data else emptyList()
        }
            .mapNotNull { eventPreviewTransformer.transform(it) }
            .filter { it.locationInfo == locationName }
            .sortedBy { it.time.startTime }
        return Resource.Success(filtered)
    }
}
