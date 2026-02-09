package com.district37.toastmasters.viewmodel

/**
 * Sealed class representing the result of a form submission
 */
sealed class FormResult<out T> {
    /**
     * Form was submitted successfully
     */
    data class Success<T>(val data: T) : FormResult<T>()

    /**
     * Form submission failed with an error
     */
    data class Error(val message: String?) : FormResult<Nothing>()
}
