package com.district37.toastmasters.models

/**
 * Connection type for paginated user events (saved or attending)
 */
data class UserEventsConnection(
    val events: List<Event>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
) {
    companion object {
        fun empty() = UserEventsConnection(
            events = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}
