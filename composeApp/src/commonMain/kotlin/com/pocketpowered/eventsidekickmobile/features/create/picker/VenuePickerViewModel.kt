package com.district37.toastmasters.features.create.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the venue picker screen
 * Provides search and pagination functionality for selecting venues
 */
@OptIn(FlowPreview::class)
class VenuePickerViewModel(
    private val venueRepository: VenueRepository
) : ViewModel() {

    private val tag = "VenuePickerViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _venues = MutableStateFlow<List<Venue>>(emptyList())
    val venues: StateFlow<List<Venue>> = _venues.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentCursor: String? = null

    init {
        // Initial load
        search(null)

        // Debounced search
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    search(query.takeIf { it.isNotBlank() })
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoading.value) return

        viewModelScope.launch {
            performSearch(
                query = _searchQuery.value.takeIf { it.isNotBlank() },
                cursor = currentCursor,
                append = true
            )
        }
    }

    private fun search(query: String?) {
        currentCursor = null
        _hasMore.value = true
        viewModelScope.launch {
            performSearch(query = query, cursor = null, append = false)
        }
    }

    private suspend fun performSearch(query: String?, cursor: String?, append: Boolean) {
        Logger.d(tag, "performSearch: query=$query, cursor=$cursor, append=$append")
        _isLoading.update { true }
        _error.update { null }

        val result = venueRepository.searchVenues(
            searchQuery = query,
            cursor = cursor,
            first = 20
        )

        Logger.d(tag, "performSearch result: $result")

        when (result) {
            is Resource.Success -> {
                val connection = result.data
                Logger.d(tag, "Search returned ${connection.items.size} venues")
                _venues.update { current ->
                    if (append) current + connection.items else connection.items
                }
                currentCursor = connection.endCursor
                _hasMore.update { connection.hasNextPage }
            }
            is Resource.Error -> {
                Logger.e(tag, "Search failed: ${result.message}")
                _error.update { result.message ?: "Search failed" }
            }
            else -> {
                Logger.d(tag, "Search result: $result")
            }
        }

        _isLoading.update { false }
    }

    fun clearError() {
        _error.update { null }
    }
}
