package com.district37.toastmasters.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A reusable lazy loading utility that defers data loading until explicitly triggered.
 * Useful for screens where data should only be loaded when the user navigates to them,
 * or for tabs that should load content on first access.
 *
 * Features:
 * - Thread-safe loading with mutex
 * - Configurable reload behavior
 * - Integrated loading state management
 *
 * Usage:
 * ```kotlin
 * private val friendsLoader = LazyLoader(
 *     scope = viewModelScope,
 *     loader = { friendsRepository.getMyFriends() }
 * )
 *
 * val friends = friendsLoader.data
 * val isLoading = friendsLoader.isLoading
 *
 * // Called when screen becomes visible
 * fun onScreenVisible() {
 *     friendsLoader.loadIfNeeded()
 * }
 *
 * // Force refresh
 * fun refresh() {
 *     friendsLoader.reload()
 * }
 * ```
 *
 * @param T The type of data being loaded
 * @param scope The CoroutineScope for launching load operations
 * @param loader The suspend function that loads the data
 */
class LazyLoader<T>(
    private val scope: CoroutineScope,
    private val loader: suspend () -> Resource<T>
) {
    private val _data = MutableStateFlow<Resource<T>>(Resource.NotLoading)
    val data: StateFlow<Resource<T>> = _data.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasLoaded = MutableStateFlow(false)
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    private val mutex = Mutex()

    /**
     * Load data if it hasn't been loaded yet.
     * Safe to call multiple times - will only load once.
     */
    fun loadIfNeeded() {
        if (_hasLoaded.value || _isLoading.value) return
        load()
    }

    /**
     * Force reload the data, even if already loaded.
     */
    fun reload() {
        load()
    }

    private fun load() {
        scope.launch {
            mutex.withLock {
                _isLoading.value = true
                _data.value = Resource.Loading

                val result = loader()
                _data.value = result
                _hasLoaded.value = true
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset the loader to its initial state.
     * The next call to [loadIfNeeded] will trigger a fresh load.
     */
    fun reset() {
        _data.value = Resource.NotLoading
        _hasLoaded.value = false
        _isLoading.value = false
    }
}

/**
 * Extension to create a LazyLoader with transform.
 * Useful when the repository returns a different type than what the UI needs.
 */
fun <T, R> LazyLoader<T>.map(transform: (T) -> R): StateFlow<Resource<R>> {
    return MutableStateFlow(data.value.map(transform)).also { mapped ->
        // Note: In a real implementation, you'd want to collect and update
        // This is a simplified version for demonstration
    }
}

/**
 * A variant of LazyLoader for lists with built-in empty state handling.
 */
class LazyListLoader<T>(
    private val scope: CoroutineScope,
    private val loader: suspend () -> Resource<List<T>>
) {
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _loadState = MutableStateFlow<Resource<Unit>>(Resource.NotLoading)
    val loadState: StateFlow<Resource<Unit>> = _loadState.asStateFlow()

    private val _hasLoaded = MutableStateFlow(false)
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    val isEmpty: Boolean
        get() = _hasLoaded.value && _items.value.isEmpty()

    private val mutex = Mutex()

    /**
     * Load data if it hasn't been loaded yet.
     */
    fun loadIfNeeded() {
        if (_hasLoaded.value || _loadState.value.isLoading) return
        load()
    }

    /**
     * Force reload the data.
     */
    fun reload() {
        load()
    }

    private fun load() {
        scope.launch {
            mutex.withLock {
                _loadState.value = Resource.Loading

                when (val result = loader()) {
                    is Resource.Success -> {
                        _items.value = result.data
                        _loadState.value = Resource.Success(Unit)
                    }
                    is Resource.Error -> {
                        _loadState.value = Resource.Error(result.errorType, result.message)
                    }
                    else -> {}
                }
                _hasLoaded.value = true
            }
        }
    }

    /**
     * Reset to initial state.
     */
    fun reset() {
        _items.value = emptyList()
        _loadState.value = Resource.NotLoading
        _hasLoaded.value = false
    }
}
