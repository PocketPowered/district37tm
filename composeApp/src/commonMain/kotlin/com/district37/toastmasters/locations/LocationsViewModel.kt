package com.district37.toastmasters.locations

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.LocationsRepository
import com.district37.toastmasters.models.Location
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationWithCount(val location: Location, val eventCount: Int)

class LocationsViewModel(
    private val repository: LocationsRepository,
    private val eventRepository: EventRepository,
    private val allLocationNodeTransformer: AllLocationNodeTransformer,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _locations = MutableStateFlow<Resource<List<LocationWithCount>>>(Resource.Loading)
    val locations: StateFlow<Resource<List<LocationWithCount>>> = _locations.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val locationsResult = repository.getAllLocations()
            if (locationsResult !is Resource.Success) {
                _locations.update { locationsResult as? Resource.Error ?: Resource.Loading }
                return@launch
            }
            val locations = locationsResult.data.mapNotNull { allLocationNodeTransformer.transform(it) }
            val countsByLocation = buildEventCountMap()
            _locations.update {
                Resource.Success(locations.map { location ->
                    LocationWithCount(location, countsByLocation[location.locationName] ?: 0)
                })
            }
        }
    }

    private suspend fun buildEventCountMap(): Map<String, Int> {
        val datesResult = eventRepository.getAvailableDates()
        if (datesResult !is Resource.Success) return emptyMap()
        return datesResult.data.flatMap { dateKey ->
            val result = eventRepository.getEventsByKey(dateKey)
            if (result is Resource.Success) result.data else emptyList()
        }
            .mapNotNull { it.location_info }
            .groupingBy { it }
            .eachCount()
    }

    fun onRefresh() {
        loadLocations()
    }
} 
