package com.district37.toastmasters.models

/**
 * Generic type representing a paginated connection result from GraphQL.
 *
 * This type consolidates all pagination result types into a single reusable generic type,
 * replacing specific connection types like EventConnection, AgendaItemConnection, etc.
 *
 * ## Usage
 *
 * Creating a new PagedConnection:
 * ```kotlin
 * val connection = PagedConnection(
 *     items = listOf(event1, event2),
 *     hasNextPage = true,
 *     endCursor = "cursor123"
 * )
 * ```
 *
 * Using with pagination:
 * ```kotlin
 * val nextPage = repository.getEvents(after = connection.endCursor)
 * val merged = connection.append(nextPage)
 * ```
 *
 * @param T The type of items in the connection
 */
data class PagedConnection<T>(
    val items: List<T>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int = items.size
) {
    /**
     * Append another page to this connection.
     * Combines items and uses pagination info from the new page.
     */
    fun append(other: PagedConnection<T>): PagedConnection<T> = copy(
        items = items + other.items,
        hasNextPage = other.hasNextPage,
        endCursor = other.endCursor,
        totalCount = other.totalCount
    )

    /**
     * Map items to a different type while preserving pagination info.
     */
    fun <R> map(transform: (T) -> R): PagedConnection<R> = PagedConnection(
        items = items.map(transform),
        hasNextPage = hasNextPage,
        endCursor = endCursor,
        totalCount = totalCount
    )

    /**
     * Filter items while preserving pagination info.
     * Note: totalCount will reflect the filtered count.
     */
    fun filter(predicate: (T) -> Boolean): PagedConnection<T> {
        val filtered = items.filter(predicate)
        return copy(items = filtered, totalCount = filtered.size)
    }

    companion object {
        /**
         * Create an empty connection with no items and no more pages.
         */
        fun <T> empty(): PagedConnection<T> = PagedConnection(
            items = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}

/**
 * Type alias for Event pagination results.
 * Use this instead of EventConnection for new code.
 */
typealias EventPage = PagedConnection<Event>

/**
 * Type alias for AgendaItem pagination results.
 * Use this instead of AgendaItemConnection for new code.
 */
typealias AgendaItemPage = PagedConnection<AgendaItem>

/**
 * Type alias for User (friends) pagination results.
 * Use this instead of FriendsConnection for new code.
 */
typealias UserPage = PagedConnection<User>

/**
 * Type alias for ActivityFeedItem pagination results.
 * Use this instead of ActivityFeedConnection for new code.
 */
typealias ActivityFeedPage = PagedConnection<ActivityFeedItem>
