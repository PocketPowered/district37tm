package com.district37.toastmasters.features.events

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for EventList screen
 */
class EventListViewModel(
    private val eventRepository: EventRepository,
    private val initialEventCount: Int = 7
) : LoggingViewModel() {

    private val _events = MutableStateFlow<Resource<List<Event>>>(Resource.Loading)
    val events: StateFlow<Resource<List<Event>>> = _events.asStateFlow()

    private val _hasMoreEvents = MutableStateFlow(false)
    val hasMoreEvents: StateFlow<Boolean> = _hasMoreEvents.asStateFlow()

    private val _endCursor = MutableStateFlow<String?>(null)
    val endCursor: StateFlow<String?> = _endCursor.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.update { Resource.Loading }
            val result = eventRepository.getEvents(first = initialEventCount)
            _events.update { result.map { it.events } }

            // Store pagination metadata
            if (result is Resource.Success) {
                _hasMoreEvents.update { result.data.hasNextPage }
                _endCursor.update { result.data.endCursor }
            }
        }
    }

    fun refresh() {
        loadEvents()
    }
}
