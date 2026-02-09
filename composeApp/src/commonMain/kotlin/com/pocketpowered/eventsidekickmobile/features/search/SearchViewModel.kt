package com.district37.toastmasters.features.search

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.SearchRepository
import com.district37.toastmasters.models.OmnisearchResult
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the full-screen Search experience
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchRepository: SearchRepository
) : LoggingViewModel() {

    companion object {
        private const val DEBOUNCE_MS = 350L
        private const val MIN_QUERY_LENGTH = 2
        private const val DEFAULT_LIMIT = 20
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<OmnisearchResult>>(Resource.NotLoading)
    val searchResults: StateFlow<Resource<OmnisearchResult>> = _searchResults.asStateFlow()

    init {
        // Set up debounced search
        _searchQuery
            .debounce(DEBOUNCE_MS)
            .distinctUntilChanged()
            .filter { it.length >= MIN_QUERY_LENGTH }
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update the search query - triggers debounced search
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length < MIN_QUERY_LENGTH) {
            _searchResults.update { Resource.NotLoading }
        }
    }

    /**
     * Clear the search query and results
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.update { Resource.NotLoading }
    }

    /**
     * Refresh the current search results
     */
    fun refresh() {
        val currentQuery = _searchQuery.value
        if (currentQuery.length >= MIN_QUERY_LENGTH) {
            performSearch(currentQuery)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _searchResults.update { Resource.Loading }
            val result = searchRepository.omnisearch(
                query = query,
                limit = DEFAULT_LIMIT
            )
            _searchResults.update { result }
        }
    }
}
