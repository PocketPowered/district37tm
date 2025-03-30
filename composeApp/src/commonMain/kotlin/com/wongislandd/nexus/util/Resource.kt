package com.wongislandd.nexus.util

object Empty

sealed class Resource<T>(
    open val data: T? = null
) {
    class Loading<T> : Resource<T>()
    class Success<T>(override val data: T) : Resource<T>(data)
    class Error<T>(val errorType: ErrorType, val throwable: Throwable? = null) :
        Resource<T>()

    fun <R> map(transform: (T) -> R?): Resource<R> {
        return when (this) {
            is Success -> {
                if (data == null) {
                    // success with no data
                    return Error(ErrorType.SUCCESS_WITH_NO_DATA)
                }
                val transformedData = transform(data)
                transform(data)?.let {
                    Success(it)
                } ?: run {
                    Error(ErrorType.CLIENT_ERROR)
                }
            }

            is Error -> Error(errorType, throwable)
            is Loading -> Loading()
        }
    }

    fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
        if (this is Success) {
            block(data)
        }
        return this
    }
}