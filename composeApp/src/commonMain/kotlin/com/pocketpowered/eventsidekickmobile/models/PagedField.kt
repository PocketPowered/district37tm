package com.district37.toastmasters.models

/**
 * Generic container for paginated fields within models
 * Replaces the pattern of having separate preview, count, hasMore, and cursor fields
 *
 * @param T The type of items in the paged collection
 * @param items List of preview/sample items
 * @param totalCount Total count of items available (including those not in preview)
 * @param hasMore Whether there are more items available beyond the current preview
 * @param cursor Pagination cursor for fetching the next page
 */
data class PagedField<T>(
    val items: List<T> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val cursor: String? = null
)
