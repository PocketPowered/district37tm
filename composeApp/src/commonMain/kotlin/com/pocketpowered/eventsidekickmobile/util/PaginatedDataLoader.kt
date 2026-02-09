package com.district37.toastmasters.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic pagination loader that eliminates boilerplate for list pagination.
 * Handles initial loading, load more, refresh, and maintains pagination state.
 *
 * Features:
 * - Thread-safe loading operations with mutex
 * - Supports refresh without losing existing data on error
 * - Configurable page size via AppConstants
 * - Automatic deduplication support (optional)
 *
 * Usage:
 * ```kotlin
 * private val activityLoader = PaginatedDataLoader(
 *     scope = viewModelScope,
 *     loader = { after -> activityFeedRepository.getFriendActivityFeed(after = after) }
 * )
 *
 * val activityFeed = activityLoader.data
 * val isLoadingMore = activityLoader.isLoadingMore
 * val hasMore = activityLoader.hasMore
 *
 * init {
 *     activityLoader.loadInitial()
 * }
 *
 * fun loadMore() = activityLoader.loadMore()
 * fun refresh() = activityLoader.refresh()
 * ```
 *
 * @param T The type of items being paginated
 * @param scope The CoroutineScope for launching load operations
 * @param loader The suspend function that loads a page of data
 * @param deduplicateBy Optional function to extract unique key for deduplication
 */
class PaginatedDataLoader<T>(
    private val scope: CoroutineScope,
    private val loader: suspend (after: String?) -> Resource<PaginationResult<T>>,
    private val deduplicateBy: ((T) -> Any)? = null
) {
    private val _data = MutableStateFlow<Resource<List<T>>>(Resource.NotLoading)
    val data: StateFlow<Resource<List<T>>> = _data.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var endCursor: String? = null
    private val mutex = Mutex()

    /**
     * Load the initial page of data.
     * Shows loading state while fetching.
     */
    fun loadInitial() {
        scope.launch {
            mutex.withLock {
                _data.value = Resource.Loading
                endCursor = null
                _hasMore.value = true

                val result = loader(null)
                result.handle(
                    onSuccess = { page ->
                        _data.value = Resource.Success(deduplicate(page.items))
                        endCursor = page.endCursor
                        _hasMore.value = page.hasNextPage
                    },
                    onError = { errorType, message ->
                        _data.value = Resource.Error(errorType, message)
                    }
                )
            }
        }
    }

    /**
     * Refresh the data from the beginning.
     * Keeps existing data if refresh fails.
     */
    fun refresh() {
        scope.launch {
            mutex.withLock {
                _isRefreshing.value = true
                val previousData = (_data.value as? Resource.Success)?.data

                val result = loader(null)
                result.handle(
                    onSuccess = { page ->
                        _data.value = Resource.Success(deduplicate(page.items))
                        endCursor = page.endCursor
                        _hasMore.value = page.hasNextPage
                    },
                    onError = { errorType, message ->
                        // Keep previous data on refresh error
                        if (previousData != null) {
                            Logger.e("PaginatedDataLoader", "Refresh failed, keeping existing data: $message")
                        } else {
                            _data.value = Resource.Error(errorType, message)
                        }
                    }
                )
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Load the next page of data and append to existing list.
     * Does nothing if already loading or no more pages.
     */
    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value) return

        scope.launch {
            mutex.withLock {
                _isLoadingMore.value = true

                loader(endCursor).handle(
                    onSuccess = { page ->
                        val current = (_data.value as? Resource.Success)?.data ?: emptyList()
                        _data.value = Resource.Success(deduplicate(current + page.items))
                        endCursor = page.endCursor
                        _hasMore.value = page.hasNextPage
                    }
                    // On error, keep existing data and don't update pagination state
                )
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Reset the loader to its initial state.
     */
    fun reset() {
        _data.value = Resource.NotLoading
        _isLoadingMore.value = false
        _isRefreshing.value = false
        _hasMore.value = true
        endCursor = null
    }

    private fun deduplicate(items: List<T>): List<T> {
        return if (deduplicateBy != null) {
            items.distinctBy(deduplicateBy)
        } else {
            items
        }
    }
}

/**
 * Result of a paginated query
 */
data class PaginationResult<T>(
    val items: List<T>,
    val endCursor: String?,
    val hasNextPage: Boolean
)

/**
 * Extension function to convert PagedConnection to PaginationResult
 */
fun <T> com.district37.toastmasters.models.PagedConnection<T>.toPaginationResult(): PaginationResult<T> {
    return PaginationResult(
        items = items,
        endCursor = endCursor,
        hasNextPage = hasNextPage
    )
}
