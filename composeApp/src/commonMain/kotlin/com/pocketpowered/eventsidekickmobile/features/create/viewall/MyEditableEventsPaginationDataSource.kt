package com.district37.toastmasters.features.create.viewall

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading user's editable events (owned + collaborated)
 */
class MyEditableEventsPaginationDataSource(
    private val createHubRepository: CreateHubRepository
) : PaginationDataSource<Event> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Event> {
        return when (val result = createHubRepository.getMyEditableEvents(cursor)) {
            is Resource.Success -> {
                val data = result.data
                PaginationResult(
                    items = data.items,
                    hasMore = data.hasNextPage,
                    nextCursor = data.endCursor
                )
            }
            is Resource.Error -> {
                throw Exception(result.message ?: "Failed to load events")
            }
            else -> {
                throw Exception("Unexpected state while loading events")
            }
        }
    }
}
