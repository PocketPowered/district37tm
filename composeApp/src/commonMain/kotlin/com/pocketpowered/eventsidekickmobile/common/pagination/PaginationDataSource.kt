package com.district37.toastmasters.common.pagination

/**
 * Result of loading a page of items
 *
 * @param T The type of items in the list
 * @param items The items loaded in this page
 * @param hasMore Whether there are more items to load
 * @param nextCursor The cursor for loading the next page, null if no more items
 */
data class PaginationResult<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val nextCursor: String?
)

/**
 * Data source interface for loading paginated data
 *
 * @param T The type of items to load
 */
interface PaginationDataSource<T> {
    /**
     * Load more items using the provided cursor
     *
     * @param cursor The cursor for the next page, or null to fetch the first page
     * @return Result containing the new items, whether there are more, and the next cursor
     */
    suspend fun loadMore(cursor: String?): PaginationResult<T>
}
