package com.district37.toastmasters.viewmodel

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Generic base ViewModel for editing existing entities
 *
 * This class consolidates the common pattern across all edit form ViewModels:
 * - EditEventViewModel
 * - EditVenueViewModel
 * - EditPerformerViewModel
 *
 * It combines BaseFormViewModel's form submission capabilities with
 * initial entity loading logic, eliminating ~30 lines of duplicate code
 * from each edit ViewModel.
 *
 * Subclasses must implement:
 * - [tag] for logging
 * - [validate] to perform field validation
 * - [submitForm] to perform the actual update mutation
 * - [mapEntityToFields] to populate form fields from loaded entity
 *
 * @param T The type of entity this form edits
 * @param entityId The ID of the entity to load and edit
 * @param repository The repository to fetch the entity from
 */
abstract class BaseEditViewModel<T>(
    protected val entityId: Int,
    protected val repository: BaseDetailRepository<T>
) : BaseFormViewModel<T>() {

    /**
     * Loading state for initial data fetch
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Error message if initial data fetch failed
     */
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    /**
     * The loaded entity (available after successful load)
     */
    private val _loadedEntity = MutableStateFlow<T?>(null)
    protected val loadedEntity: StateFlow<T?> = _loadedEntity.asStateFlow()

    init {
        loadEntity()
    }

    /**
     * Map the loaded entity data to form fields
     *
     * Subclasses implement this to populate their specific form fields
     * from the loaded entity data.
     *
     * @param entity The loaded entity to extract data from
     */
    protected abstract fun mapEntityToFields(entity: T)

    /**
     * Load the entity from the repository
     */
    private fun loadEntity() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null

            when (val result = repository.getDetails(entityId)) {
                is Resource.Success -> {
                    val entity = result.data
                    _loadedEntity.value = entity
                    mapEntityToFields(entity)
                    Logger.d(tag, "Loaded entity with id=$entityId")
                }
                is Resource.Error -> {
                    _loadError.value = result.message ?: "Failed to load data"
                    Logger.e(tag, "Failed to load entity: ${result.message}")
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Retry loading the entity after a failure
     */
    fun retry() {
        loadEntity()
    }
}
