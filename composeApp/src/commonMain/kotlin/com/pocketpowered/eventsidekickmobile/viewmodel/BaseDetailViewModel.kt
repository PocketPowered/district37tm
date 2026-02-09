package com.district37.toastmasters.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.models.EntityPermissions
import com.district37.toastmasters.models.HasPermissions
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Generic base ViewModel for detail screens
 *
 * This class consolidates the common pattern across all detail ViewModels:
 * - EventDetailViewModel
 * - VenueDetailViewModel
 * - PerformerDetailViewModel
 * - LocationDetailViewModel
 * - ScheduleItemDetailViewModel
 *
 * By extending this base class and implementing just the [tag] property,
 * you get a complete ViewModel with loading, success, and error states.
 *
 * For entities that implement [HasPermissions], this base class automatically
 * extracts and exposes permissions state, eliminating duplicate permissions
 * handling code across detail ViewModels.
 *
 * @param T The type of entity this ViewModel provides details for
 * @param R The type of repository (must extend BaseDetailRepository<T>)
 */
abstract class BaseDetailViewModel<T, R : BaseDetailRepository<T>>(
    protected val itemId: Int,
    protected val repository: R
) : ViewModel() {

    /**
     * Tag for logging
     */
    protected abstract val tag: String

    /**
     * State flow containing the current resource state
     */
    private val _item = MutableStateFlow<Resource<T>>(Resource.Loading)
    val item: StateFlow<Resource<T>> = _item.asStateFlow()

    /**
     * Permissions extracted from the entity (if it implements HasPermissions)
     */
    private val _permissions = MutableStateFlow<EntityPermissions?>(null)
    val permissions: StateFlow<EntityPermissions?> = _permissions.asStateFlow()

    /**
     * Convenience property: whether the current user can edit this entity
     */
    val canEdit: Boolean get() = _permissions.value?.canEdit == true

    /**
     * Convenience property: whether the current user can delete this entity
     */
    val canDelete: Boolean get() = _permissions.value?.canDelete == true

    init {
        loadDetails()
    }

    /**
     * Load the entity details
     */
    protected open fun loadDetails() {
        viewModelScope.launch {
            _item.update { Resource.Loading }
            val result = repository.getDetails(itemId)
            _item.update { result }

            // Extract permissions if the entity implements HasPermissions
            if (result is Resource.Success) {
                val entity = result.data
                if (entity is HasPermissions) {
                    _permissions.value = entity.permissions
                }
            }

            if (result is Resource.Error) {
                Logger.e(tag, "Failed to load details: ${result.message}")
            }
        }
    }

    /**
     * Refresh the entity details
     */
    fun refresh() {
        loadDetails()
    }

    /**
     * Safe accessor for the loaded data.
     * Returns null if the resource is not in Success state.
     *
     * This eliminates the common pattern of:
     * ```kotlin
     * val entity = (item.value as? Resource.Success)?.data ?: return
     * ```
     *
     * Instead, use:
     * ```kotlin
     * val entity = itemData ?: return
     * ```
     */
    val itemData: T?
        get() = (item.value as? Resource.Success)?.data
}
