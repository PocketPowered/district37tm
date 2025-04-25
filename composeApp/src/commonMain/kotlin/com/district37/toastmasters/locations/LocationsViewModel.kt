package com.district37.toastmasters.locations

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.LocationsRepository
import com.district37.toastmasters.models.Location
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val repository: LocationsRepository,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _locations = MutableStateFlow<Resource<List<Location>>>(Resource.Loading)
    val locations: StateFlow<Resource<List<Location>>> = _locations.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            _locations.update {
                repository.getAllLocations()
            }
        }
    }

    fun onRefresh() {
        loadLocations()
    }
} 