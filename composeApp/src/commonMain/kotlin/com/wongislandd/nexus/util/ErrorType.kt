package com.wongislandd.nexus.util

enum class ErrorType(val errorMessage: String = "Something went wrong!", val isRetryable: Boolean = false) {

    // 404 Not Found
    NOT_FOUND("Resource not found"),

    // 400 Bad Request
    BAD_REQUEST("Bad request, please check your parameters"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("An internal server error occurred"),

    // 401 Unauthorized
    UNAUTHORIZED("Unauthorized access, please check your credentials"),

    // 403 Forbidden
    FORBIDDEN("Access is forbidden"),

    // 408 Request Timeout
    REQUEST_TIMEOUT("The request timed out", isRetryable = true),

    SOCKET_TIMEOUT("Socket timeout", isRetryable = true),

    // 429 Rate Limited
    RATE_LIMITED("Rate limited, please try again later", isRetryable = true),

    // General Network Error
    NETWORK_ERROR("Network error, please check your connection", isRetryable = true),

    NO_INTERNET("No internet connection", isRetryable = true),

    SERIALIZATION("Serialization error"),

    // Client-side Error
    CLIENT_ERROR,

    // Success with no data
    SUCCESS_WITH_NO_DATA,

    UNKNOWN
}