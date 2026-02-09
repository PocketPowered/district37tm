package com.district37.toastmasters.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.UserEngagementRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Subscribed Events screen (View All)
 */
class SavedEventsViewModel(
    private val userEngagementRepository: UserEngagementRepository
) : ViewModel() {

    private val tag = "SavedEventsViewModel"

    private val _events = MutableStateFlow<Resource<List<Event>>>(Resource.NotLoading)
    val events: StateFlow<Resource<List<Event>>> = _events.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreEvents = MutableStateFlow(false)
    val hasMoreEvents: StateFlow<Boolean> = _hasMoreEvents.asStateFlow()

    private var endCursor: String? = null

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.update { Resource.Loading }
            endCursor = null
            val result = userEngagementRepository.getMySubscribedEvents(first = PAGE_SIZE)

            when (result) {
                is Resource.Success -> {
                    _events.update { Resource.Success(result.data.events) }
                    _hasMoreEvents.update { result.data.hasNextPage }
                    endCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _events.update { Resource.Error(result.errorType, result.message) }
                    Logger.e(tag, "Failed to load subscribed events: ${result.message}")
                }
                else -> {
                    // Loading or NotLoading states handled elsewhere
                }
            }
        }
    }

    fun loadMore() {
        val currentEvents = (_events.value as? Resource.Success)?.data ?: return
        if (!_hasMoreEvents.value || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.update { true }
            val result = userEngagementRepository.getMySubscribedEvents(
                first = PAGE_SIZE,
                after = endCursor
            )

            if (result is Resource.Success) {
                _events.update { Resource.Success(currentEvents + result.data.events) }
                _hasMoreEvents.update { result.data.hasNextPage }
                endCursor = result.data.endCursor
            }
            _isLoadingMore.update { false }
        }
    }

    fun refresh() {
        loadEvents()
    }
}
