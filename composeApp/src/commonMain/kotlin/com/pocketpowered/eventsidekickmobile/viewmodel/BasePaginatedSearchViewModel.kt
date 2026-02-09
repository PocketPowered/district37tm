package com.district37.toastmasters.viewmodel

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.AppConstants
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
 * Base ViewModel for screens that need search with pagination.
 * Provides common functionality for:
 * - Debounced search query handling
 * - Cursor-based pagination
 * - Loading/error state management
 * - Item list management
 *
 * Subclasses must implement [performSearchOperation] to define how to fetch data.
 *
 * @param T The type of items being paginated
 * @param debounceMs Milliseconds to debounce search queries (default from AppConstants)
 */
@OptIn(FlowPreview::class)
abstract class BasePaginatedSearchViewModel<T>(
    private val debounceMs: Long = AppConstants.Debounce.SEARCH_MS
) : LoggingViewModel() {

    /**
     * Tag for logging - must be provided by subclass
     */
    protected abstract val tag: String

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Items state
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Pagination state
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    protected var currentCursor: String? = null
        private set

    init {
        // Initial load with empty query
        search(null)

        // Set up debounced search
        viewModelScope.launch {
            searchQuery
                .debounce(debounceMs)
                .distinctUntilChanged()
                .collect { query ->
                    search(query.takeIf { it.isNotBlank() })
                }
        }
    }

    /**
     * Update the search query
     * This will trigger a debounced search after [debounceMs] milliseconds
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    /**
     * Load more items (pagination)
     * Does nothing if already loading or no more items available
     */
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

    /**
     * Perform a new search, resetting pagination
     * @param query The search query, or null for no filter
     */
    protected fun search(query: String?) {
        currentCursor = null
        _hasMore.value = true
        viewModelScope.launch {
            performSearch(query = query, cursor = null, append = false)
        }
    }

    /**
     * Execute the search operation
     */
    private suspend fun performSearch(query: String?, cursor: String?, append: Boolean) {
        _isLoading.update { true }
        _error.update { null }

        val result = performSearchOperation(query, cursor)

        when (result) {
            is Resource.Success -> {
                _items.update { current ->
                    if (append) current + result.data.items else result.data.items
                }
                currentCursor = result.data.endCursor
                _hasMore.update { result.data.hasNextPage }
            }
            is Resource.Error -> {
                Logger.e(tag, "Search failed: ${result.message}")
                _error.update { result.message ?: "Search failed" }
            }
            else -> {}
        }

        _isLoading.update { false }
    }

    /**
     * Abstract method to be implemented by subclasses
     * Defines how to fetch data from the repository
     *
     * @param query Optional search query string
     * @param cursor Optional pagination cursor
     * @return Resource containing PagedConnection of items
     */
    protected abstract suspend fun performSearchOperation(
        query: String?,
        cursor: String?
    ): Resource<PagedConnection<T>>

    /**
     * Clear the current error message
     */
    fun clearError() {
        _error.update { null }
    }
}
