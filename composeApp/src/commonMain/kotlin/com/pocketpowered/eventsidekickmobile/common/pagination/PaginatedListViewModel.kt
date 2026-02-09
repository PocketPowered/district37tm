package com.district37.toastmasters.common.pagination

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Generic ViewModel for managing paginated list state
 *
 * @param T The type of items in the list
 * @param config Initial configuration for the paginated list
 * @param dataSource Data source for loading more items
 */
class PaginatedListViewModel<T>(
    private val config: PaginatedListConfig<T>,
    private val dataSource: PaginationDataSource<T>
) : LoggingViewModel() {

    private val _items = MutableStateFlow(config.initialItems)
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentCursor: String? = config.initialCursor

    val title: String = config.title
    val subtitle: String? = config.subtitle
    val totalCount: Int = config.totalCount
    val emptyMessage: String = config.emptyMessage

    init {
        // If we start with an empty list, load the first page
        if (config.initialItems.isEmpty()) {
            loadMore()
        } else {
            // If we have initial items, set hasMore based on the initial state
            _hasMoreItems.update { config.initialItems.size < config.totalCount }
        }
    }

    /**
     * Load the next page of items
     */
    fun loadMore() {
        // Allow loading if cursor is null (initial load) or if there's a valid cursor
        if (_isLoadingMore.value || !_hasMoreItems.value) return

        viewModelScope.launch {
            _isLoadingMore.update { true }
            _error.update { null }

            try {
                val result = dataSource.loadMore(currentCursor)
                _items.update { it + result.items }
                _hasMoreItems.update { result.hasMore }
                currentCursor = result.nextCursor
            } catch (e: Exception) {
                _error.update { e.message ?: "Failed to load more items" }
            } finally {
                _isLoadingMore.update { false }
            }
        }
    }

    /**
     * Refresh the entire list (not implemented yet, would require data source support)
     */
    fun refresh() {
        // For now, this would require the data source to support fetching from the beginning
        // Which we can implement later if needed
    }
}
