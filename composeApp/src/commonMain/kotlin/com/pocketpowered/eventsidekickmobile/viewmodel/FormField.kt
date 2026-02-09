package com.district37.toastmasters.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the state of a form field with validation
 */
data class FormField<T>(
    val value: T,
    val error: String? = null,
    val isModified: Boolean = false
) {
    val isValid: Boolean get() = error == null
}

/**
 * Mutable form field with state management
 *
 * Eliminates the need for boilerplate like:
 * ```kotlin
 * private val _name = MutableStateFlow("")
 * val name: StateFlow<String> = _name.asStateFlow()
 *
 * fun updateName(value: String) {
 *     _name.update { value }
 *     clearFieldError("name")
 * }
 * ```
 *
 * Usage:
 * ```kotlin
 * class CreateEventViewModel : BaseFormViewModel<Event>() {
 *     val name = MutableFormField("", autoClearError = true)
 *     val description = MutableFormField("")
 *     val startDate = MutableFormField<Instant?>(null, autoClearError = true)
 *
 *     override fun validate(): Boolean {
 *         var isValid = true
 *         if (name.value.isBlank()) {
 *             name.setError("Name is required")
 *             isValid = false
 *         }
 *         return isValid
 *     }
 * }
 * ```
 *
 * In Compose UI:
 * ```kotlin
 * val nameState by viewModel.name.state.collectAsState()
 * TextField(
 *     value = nameState.value,
 *     onValueChange = { viewModel.name.update(it) },
 *     isError = nameState.error != null,
 *     supportingText = nameState.error?.let { { Text(it) } }
 * )
 * ```
 */
class MutableFormField<T>(
    initialValue: T,
    private val autoClearError: Boolean = true
) {
    private val _state = MutableStateFlow(FormField(initialValue))
    val state: StateFlow<FormField<T>> = _state.asStateFlow()

    val value: T get() = _state.value.value
    val error: String? get() = _state.value.error
    val isValid: Boolean get() = _state.value.isValid
    val isModified: Boolean get() = _state.value.isModified

    /**
     * Update the field value and mark as modified.
     * If [autoClearError] is true (default), also clears any validation error.
     */
    fun update(value: T) {
        _state.value = if (autoClearError) {
            _state.value.copy(value = value, error = null, isModified = true)
        } else {
            _state.value.copy(value = value, isModified = true)
        }
    }

    /**
     * Set a validation error
     */
    fun setError(error: String?) {
        _state.value = _state.value.copy(error = error)
    }

    /**
     * Clear any validation error
     */
    fun clearError() = setError(null)

    /**
     * Set value without triggering isModified or clearing errors.
     * Useful for populating initial values from loaded entities.
     */
    fun setValue(value: T) {
        _state.value = FormField(value)
    }

    /**
     * Reset the field to a specific value
     */
    fun reset(value: T) {
        _state.value = FormField(value)
    }
}
