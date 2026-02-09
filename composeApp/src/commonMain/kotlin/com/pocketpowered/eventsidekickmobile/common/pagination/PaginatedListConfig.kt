package com.district37.toastmasters.common.pagination

/**
 * Configuration for a paginated list screen
 *
 * @param T The type of items in the list
 * @param title The title to display at the top of the screen
 * @param subtitle Optional subtitle (e.g., "X items")
 * @param initialItems The initial items already loaded from the entry point
 * @param totalCount Total number of items available
 * @param initialCursor The cursor for loading the next page
 * @param emptyMessage Message to display when there are no items
 */
data class PaginatedListConfig<T>(
    val title: String,
    val subtitle: String? = null,
    val initialItems: List<T>,
    val totalCount: Int,
    val initialCursor: String?,
    val emptyMessage: String = "No items found"
)
