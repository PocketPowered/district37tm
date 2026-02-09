package com.district37.toastmasters.viewmodel

import kotlinx.datetime.Instant

/**
 * Reusable validation rules for form fields.
 *
 * Eliminates repetitive validation patterns like:
 * ```kotlin
 * if (_name.value.isBlank()) {
 *     setFieldError("name", "Event name is required")
 *     isValid = false
 * }
 * ```
 *
 * Usage:
 * ```kotlin
 * class CreateEventViewModel : BaseFormViewModel<Event>() {
 *     override fun validate(): Boolean {
 *         return validateAll(
 *             validateRequired("name", _name.value, "Event name is required"),
 *             validateRequired("venue", _selectedVenueId.value, "Venue is required"),
 *             validateRequired("startDate", _startDate.value, "Start date is required"),
 *             validateRequired("endDate", _endDate.value, "End date is required"),
 *             validateDateAfter("endDate", _endDate.value, _startDate.value, "End date must be after start date")
 *         )
 *     }
 * }
 * ```
 */

/**
 * Validation result containing field name and optional error message.
 */
data class ValidationResult(
    val fieldName: String,
    val errorMessage: String?
) {
    val isValid: Boolean get() = errorMessage == null
}

// ============ Validation Functions ============

/**
 * Validates that a string value is not blank.
 */
fun validateRequired(
    fieldName: String,
    value: String?,
    errorMessage: String = "This field is required"
): ValidationResult {
    return if (value.isNullOrBlank()) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a nullable value is not null.
 */
fun <T> validateRequired(
    fieldName: String,
    value: T?,
    errorMessage: String = "This field is required"
): ValidationResult {
    return if (value == null) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a string has a minimum length.
 */
fun validateMinLength(
    fieldName: String,
    value: String?,
    minLength: Int,
    errorMessage: String = "Must be at least $minLength characters"
): ValidationResult {
    return if (value != null && value.length < minLength) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a string has a maximum length.
 */
fun validateMaxLength(
    fieldName: String,
    value: String?,
    maxLength: Int,
    errorMessage: String = "Must be at most $maxLength characters"
): ValidationResult {
    return if (value != null && value.length > maxLength) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a date is after another date.
 */
fun validateDateAfter(
    fieldName: String,
    date: Instant?,
    afterDate: Instant?,
    errorMessage: String = "Date must be after the specified date"
): ValidationResult {
    return if (date != null && afterDate != null && date < afterDate) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a date is before another date.
 */
fun validateDateBefore(
    fieldName: String,
    date: Instant?,
    beforeDate: Instant?,
    errorMessage: String = "Date must be before the specified date"
): ValidationResult {
    return if (date != null && beforeDate != null && date > beforeDate) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates using a custom predicate.
 */
fun <T> validateCustom(
    fieldName: String,
    value: T,
    isValid: (T) -> Boolean,
    errorMessage: String
): ValidationResult {
    return if (!isValid(value)) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a number is positive.
 */
fun validatePositive(
    fieldName: String,
    value: Number?,
    errorMessage: String = "Must be a positive number"
): ValidationResult {
    return if (value != null && value.toDouble() <= 0) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a number is non-negative.
 */
fun validateNonNegative(
    fieldName: String,
    value: Number?,
    errorMessage: String = "Must be zero or greater"
): ValidationResult {
    return if (value != null && value.toDouble() < 0) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a string matches an email pattern.
 */
fun validateEmail(
    fieldName: String,
    value: String?,
    errorMessage: String = "Invalid email address"
): ValidationResult {
    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return if (value != null && value.isNotBlank() && !emailPattern.matches(value)) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

/**
 * Validates that a string is a valid URL.
 */
fun validateUrl(
    fieldName: String,
    value: String?,
    errorMessage: String = "Invalid URL"
): ValidationResult {
    val urlPattern = Regex("^(https?://)?[\\w.-]+\\.[a-z]{2,}(/.*)?$", RegexOption.IGNORE_CASE)
    return if (value != null && value.isNotBlank() && !urlPattern.matches(value)) {
        ValidationResult(fieldName, errorMessage)
    } else {
        ValidationResult(fieldName, null)
    }
}

// NOTE: The validateAll function is defined in BaseFormViewModel (and inherited by BaseEditViewModel).
// This allows subclasses to use it to apply validation results:
//
// override fun validate(): Boolean = validateAll(
//     validateRequired("name", _name.value, "Name is required"),
//     validateRequired("email", _email.value, "Email is required"),
//     validateEmail("email", _email.value)
// )
