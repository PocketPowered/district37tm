package com.district37.toastmasters.util

import kotlin.time.Duration.Companion.milliseconds

/**
 * Centralized configuration constants for the app.
 * Ensures consistency across all features.
 */
object AppConstants {

    /**
     * Debounce timings for various user input scenarios
     */
    object Debounce {
        /** Standard debounce for search inputs */
        val SEARCH = 300.milliseconds

        /** Debounce for mark-as-read operations to batch multiple reads */
        val MARK_AS_READ = 500.milliseconds

        /** Debounce for text input validation */
        val VALIDATION = 200.milliseconds

        // Long versions for when Duration is not available
        const val SEARCH_MS = 300L
        const val MARK_AS_READ_MS = 500L
        const val VALIDATION_MS = 200L
    }

    /**
     * Pagination defaults
     */
    object Pagination {
        /** Default page size for list queries */
        const val DEFAULT_PAGE_SIZE = 20

        /** Page size for message queries */
        const val MESSAGES_PAGE_SIZE = 30

        /** Page size for search results */
        const val SEARCH_PAGE_SIZE = 20
    }

    /**
     * Retry configuration for network operations
     */
    object Retry {
        /** Maximum number of retry attempts */
        const val MAX_ATTEMPTS = 3

        /** Initial delay between retries in milliseconds */
        const val INITIAL_DELAY_MS = 1000L

        /** Maximum delay between retries in milliseconds */
        const val MAX_DELAY_MS = 5000L
    }

    /**
     * Cache configuration
     */
    object Cache {
        /** Maximum number of conversations to cache locally */
        const val MAX_CACHED_CONVERSATIONS = 50

        /** Maximum number of messages per conversation to cache */
        const val MAX_CACHED_MESSAGES_PER_CONVERSATION = 100

        /** Cache TTL for frequently changing data (in milliseconds) */
        const val SHORT_TTL_MS = 60_000L // 1 minute

        /** Cache TTL for relatively static data (in milliseconds) */
        const val LONG_TTL_MS = 300_000L // 5 minutes
    }
}
