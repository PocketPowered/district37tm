package com.district37.toastmasters.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Reusable feature for archive operations in ViewModels
 *
 * Provides state management and execution for archive operations:
 * - Loading state (isArchiving)
 * - Success state (archiveSuccess)
 * - Centralized error handling
 * - Logging
 *
 * Usage:
 * ```kotlin
 * class EditEventViewModel(...) : BaseEditViewModel<Event>(...) {
 *     private val archiveHandler = ArchiveOperationFeature(
 *         tag = tag,
 *         scope = viewModelScope
 *     )
 *
 *     val isArchiving = archiveHandler.isArchiving
 *     val archiveSuccess = archiveHandler.archiveSuccess
 *
 *     fun archiveEvent() {
 *         archiveHandler.performArchive {
 *             eventRepository.archiveEvent(eventId)
 *         }
 *     }
 * }
 * ```
 */
class ArchiveOperationFeature(
    private val tag: String,
    private val scope: CoroutineScope
) {
    // Archive loading state
    private val _isArchiving = MutableStateFlow(false)
    val isArchiving: StateFlow<Boolean> = _isArchiving.asStateFlow()

    // Archive success state
    private val _archiveSuccess = MutableStateFlow(false)
    val archiveSuccess: StateFlow<Boolean> = _archiveSuccess.asStateFlow()

    /**
     * Perform an archive operation
     *
     * @param operation Suspend function that performs the actual archive (returns Resource<Unit>)
     */
    fun performArchive(operation: suspend () -> Resource<Unit>) {
        scope.launch {
            _isArchiving.update { true }
            val result = operation()
            _isArchiving.update { false }

            when (result) {
                is Resource.Success -> {
                    _archiveSuccess.update { true }
                    Logger.d(tag, "Archive operation completed successfully")
                }
                is Resource.Error -> {
                    Logger.e(tag, "Archive operation failed: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Reset the archive success state (e.g., after handling the success)
     */
    fun resetArchiveSuccess() {
        _archiveSuccess.update { false }
    }
}
