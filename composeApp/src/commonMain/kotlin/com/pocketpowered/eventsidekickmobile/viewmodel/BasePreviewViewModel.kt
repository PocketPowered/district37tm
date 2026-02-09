package com.district37.toastmasters.viewmodel

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Generic base class for preview ViewModels that load a single item by ID
 * Consolidates the common pattern used by all preview screens
 *
 * @param T The type of item being loaded
 * @param R The repository type, must implement BasePreviewRepository<T>
 * @param itemId The ID of the item to load
 * @param repository The repository to fetch data from
 */
abstract class BasePreviewViewModel<T, R : BasePreviewRepository<T>>(
    protected val itemId: Int,
    protected val repository: R
) : LoggingViewModel() {

    protected abstract val tag: String

    private val _item = MutableStateFlow<Resource<T>>(Resource.Loading)
    val item: StateFlow<Resource<T>> = _item.asStateFlow()

    init {
        loadPreview()
    }

    protected open fun loadPreview() {
        viewModelScope.launch {
            _item.update { Resource.Loading }
            val result = repository.getPreview(itemId)
            _item.update { result }
            if (result is Resource.Error) {
                Logger.e(tag, "Failed to load preview: ${result.message}")
            }
        }
    }

    open fun refresh() {
        loadPreview()
    }
}
