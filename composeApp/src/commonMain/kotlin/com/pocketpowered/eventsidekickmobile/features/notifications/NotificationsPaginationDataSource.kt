package com.district37.toastmasters.features.notifications

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.models.Notification
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for notifications
 */
class NotificationsPaginationDataSource(
    private val notificationRepository: NotificationRepository,
    private val unreadOnly: Boolean = false
) : PaginationDataSource<Notification> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Notification> {
        return when (val result = notificationRepository.getNotifications(
            cursor = cursor,
            unreadOnly = if (unreadOnly) true else null
        )) {
            is Resource.Success -> {
                PaginationResult(
                    items = result.data.items,
                    hasMore = result.data.hasNextPage,
                    nextCursor = result.data.endCursor
                )
            }
            is Resource.Error -> {
                throw Exception(result.message ?: "Failed to load notifications")
            }
            is Resource.Loading -> {
                PaginationResult(
                    items = emptyList(),
                    hasMore = false,
                    nextCursor = null
                )
            }
            is Resource.NotLoading -> {
                PaginationResult(
                    items = emptyList(),
                    hasMore = false,
                    nextCursor = null
                )
            }
        }
    }
}
