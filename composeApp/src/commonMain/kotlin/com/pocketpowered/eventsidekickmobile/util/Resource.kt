package com.district37.toastmasters.util

/**
 * A generic class that holds a value with its loading status.
 * Inspired by the Resource pattern from Android Architecture Components
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val errorType: ErrorType, val message: String? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data object NotLoading : Resource<Nothing>()

    val isLoading: Boolean
        get() = this is Loading

    val isError: Boolean
        get() = this is Error

    val isSuccess: Boolean
        get() = this is Success

    /**
     * Map the success value to another type
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
            is NotLoading -> NotLoading
        }
    }

    /**
     * Handle different resource states
     */
    inline fun handle(
        onSuccess: (T) -> Unit = {},
        onError: (ErrorType, String?) -> Unit = { _, _ -> },
        onLoading: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(errorType, message)
            is Loading -> onLoading()
            is NotLoading -> {}
        }
    }

    /**
     * Get data or null
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    /**
     * Get data or throw exception
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is Error -> throw Exception(message ?: "Unknown error: $errorType")
            is Loading -> throw Exception("Resource is still loading")
            is NotLoading -> throw Exception("Resource not loaded")
        }
    }
}

/**
 * Error types for the Resource class
 */
enum class ErrorType {
    NETWORK_ERROR,
    SERVER_ERROR,
    CLIENT_ERROR,
    UNKNOWN_ERROR
}

/**
 * Extension function for Resource.Loading to handle different states
 */
inline fun <T> Resource.Loading.handle(
    onSuccess: (T) -> Unit = {},
    onError: (ErrorType, String?) -> Unit = { _, _ -> }
): Unit = Unit
