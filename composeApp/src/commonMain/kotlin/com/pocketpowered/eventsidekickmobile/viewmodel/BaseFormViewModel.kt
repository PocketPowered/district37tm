package com.district37.toastmasters.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Generic base ViewModel for entity creation forms
 *
 * This class consolidates the common pattern across all create form ViewModels:
 * - CreateEventViewModel
 * - CreateVenueViewModel
 * - CreatePerformerViewModel
 *
 * Subclasses must implement:
 * - [tag] for logging
 * - [validate] to perform field validation
 * - [submitForm] to perform the actual mutation
 *
 * @param T The type of entity this form creates
 */
abstract class BaseFormViewModel<T> : ViewModel() {

    /**
     * Tag for logging
     */
    protected abstract val tag: String

    /**
     * Whether the form is currently submitting
     */
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    /**
     * The result of the form submission (null while not submitted or editing)
     */
    private val _formResult = MutableStateFlow<FormResult<T>?>(null)
    val formResult: StateFlow<FormResult<T>?> = _formResult.asStateFlow()

    /**
     * Field-level validation errors (field name -> error message)
     */
    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    /**
     * Validate the form fields
     * @return true if valid, false otherwise
     */
    protected abstract fun validate(): Boolean

    /**
     * Submit the form data to the server
     * @return Resource containing the created entity or an error
     */
    protected abstract suspend fun submitForm(): Resource<T>

    /**
     * Set a validation error for a specific field
     */
    protected fun setFieldError(field: String, error: String) {
        _fieldErrors.update { it + (field to error) }
    }

    /**
     * Clear the validation error for a specific field
     */
    protected fun clearFieldError(field: String) {
        _fieldErrors.update { it - field }
    }

    /**
     * Clear all validation errors
     */
    protected fun clearAllFieldErrors() {
        _fieldErrors.update { emptyMap() }
    }

    /**
     * Apply all validation results, setting field errors for invalid fields.
     * Returns true if all validations passed.
     *
     * Usage:
     * ```kotlin
     * override fun validate(): Boolean = validateAll(
     *     validateRequired("name", _name.value, "Name is required"),
     *     validateRequired("email", _email.value, "Email is required"),
     *     validateEmail("email", _email.value)
     * )
     * ```
     */
    protected fun validateAll(vararg results: ValidationResult): Boolean {
        var allValid = true
        results.forEach { result ->
            if (!result.isValid) {
                setFieldError(result.fieldName, result.errorMessage!!)
                allValid = false
            }
        }
        return allValid
    }

    /**
     * Clear the form result (e.g., when user starts editing after an error)
     */
    fun clearFormResult() {
        _formResult.update { null }
    }

    /**
     * Submit the form
     * 1. Validates the form
     * 2. If valid, calls submitForm()
     * 3. Updates formResult with success or error
     */
    fun submit() {
        // Clear previous errors
        clearAllFieldErrors()
        clearFormResult()

        // Validate
        if (!validate()) {
            Logger.d(tag, "Form validation failed")
            return
        }

        // Submit
        viewModelScope.launch {
            _isSubmitting.update { true }
            try {
                val result = submitForm()
                _formResult.update {
                    when (result) {
                        is Resource.Success -> {
                            Logger.d(tag, "Form submitted successfully")
                            FormResult.Success(result.data)
                        }
                        is Resource.Error -> {
                            Logger.e(tag, "Form submission failed: ${result.message}")
                            FormResult.Error(result.message)
                        }
                        else -> null
                    }
                }
            } catch (e: Exception) {
                Logger.e(tag, "Exception during form submission: ${e.message}", e)
                _formResult.update { FormResult.Error(e.message ?: "An unexpected error occurred") }
            } finally {
                _isSubmitting.update { false }
            }
        }
    }
}
