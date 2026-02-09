package com.district37.toastmasters.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Reusable feature for delete operations in ViewModels
 *
 * Provides state management and execution for delete operations:
 * - Loading state (isDeleting)
 * - Success state (deleteSuccess)
 * - Centralized error handling
 * - Logging
 *
 * Usage:
 * ```kotlin
 * class EditEventViewModel(...) : BaseEditViewModel<Event>(...) {
 *     private val deleteHandler = DeleteOperationFeature(
 *         tag = tag,
 *         scope = viewModelScope
 *     )
 *
 *     val isDeleting = deleteHandler.isDeleting
 *     val deleteSuccess = deleteHandler.deleteSuccess
 *
 *     fun deleteEvent() {
 *         deleteHandler.performDelete {
 *             eventRepository.deleteEvent(eventId)
 *         }
 *     }
 * }
 * ```
 */
class DeleteOperationFeature(
    private val tag: String,
    private val scope: CoroutineScope
) {
    // Delete loading state
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Delete success state
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    /**
     * Perform a delete operation
     *
     * @param operation Suspend function that performs the actual delete (returns Resource<Unit>)
     */
    fun performDelete(operation: suspend () -> Resource<Unit>) {
        scope.launch {
            _isDeleting.update { true }
            val result = operation()
            _isDeleting.update { false }

            when (result) {
                is Resource.Success -> {
                    _deleteSuccess.update { true }
                    Logger.d(tag, "Delete operation completed successfully")
                }
                is Resource.Error -> {
                    Logger.e(tag, "Delete operation failed: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Reset the delete success state (e.g., after handling the success)
     */
    fun resetDeleteSuccess() {
        _deleteSuccess.update { false }
    }
}
