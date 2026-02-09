package com.district37.toastmasters.features.schedules.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing agenda items within an event
 */
class ManageAgendaItemsViewModel(
    private val eventRepository: EventRepository,
    private val agendaItemRepository: AgendaItemRepository,
    private val eventId: Int
) : ViewModel() {

    private val tag = "ManageAgendaItemsViewModel"

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _agendaItems = MutableStateFlow<List<AgendaItem>>(emptyList())
    val agendaItems: StateFlow<List<AgendaItem>> = _agendaItems.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null

            // Load event details first to get the agenda dates
            when (val eventResult = eventRepository.getDetails(eventId)) {
                is Resource.Success -> {
                    _event.value = eventResult.data

                    // Load agenda items for all dates
                    val allItems = mutableListOf<AgendaItem>()
                    for (agendaDate in eventResult.data.agendaDates) {
                        when (val itemsResult = agendaItemRepository.getAgendaItemsByEventAndDate(
                            eventId = eventId,
                            date = agendaDate.date,
                            first = 100 // Load more items for management view
                        )) {
                            is Resource.Success -> {
                                allItems.addAll(itemsResult.data.agendaItems)
                            }
                            is Resource.Error -> {
                                Logger.d(tag, "Failed to load items for date ${agendaDate.date}: ${itemsResult.message}")
                            }
                            else -> {}
                        }
                    }
                    _agendaItems.value = allItems.sortedBy { it.startTime?.instant }
                }
                is Resource.Error -> {
                    _loadError.value = eventResult.message ?: "Failed to load event"
                    Logger.e(tag, "Failed to load event: ${eventResult.message}")
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun deleteAgendaItem(agendaItemId: Int) {
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteError.value = null

            when (val result = agendaItemRepository.deleteAgendaItem(agendaItemId)) {
                is Resource.Success -> {
                    // Reload the list
                    loadData()
                }
                is Resource.Error -> {
                    _deleteError.value = result.message ?: "Failed to delete agenda item"
                    Logger.e(tag, "Failed to delete agenda item: ${result.message}")
                }
                else -> {}
            }

            _isDeleting.value = false
        }
    }

    fun clearDeleteError() {
        _deleteError.value = null
    }

    fun retry() {
        loadData()
    }
}
