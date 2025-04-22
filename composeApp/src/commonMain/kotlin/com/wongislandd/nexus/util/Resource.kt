package com.wongislandd.nexus.util

/**
 * Useful to put in a resource where we really only care about loading state
 */
object Empty

sealed class Resource<out T> {

    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(
        val error: ErrorType,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()

    fun <R> map(transform: (T) -> R?): Resource<R> {
        return when (this) {
            is Success -> {
                val transformedData = transform(data)
                if (transformedData != null) {
                    Success(transformedData)
                } else {
                    Error(ErrorType.CLIENT_ERROR)
                }
            }
            is Error -> this
            is Loading -> this
        }
    }


    fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
        if (this is Success) {
            block(data)
        }
        return this
    }

    fun Resource<*>.onError(block: (error: ErrorType?, throwable: Throwable?) -> Unit): Resource<*> {
        if (this is Error) {
            block(error, throwable)
        }
        return this
    }

    fun <T> Resource<T>.handle(
        onSuccess: (T) -> Unit,
        onError: (error: ErrorType, throwable: Throwable?) -> Unit
    ): Resource<T> {
        return when (this) {
            is Success -> {
                onSuccess(data)
                this
            }
            is Error -> {
                onError(error, throwable)
                this
            }
            else -> this
        }
    }
}