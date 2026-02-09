package com.district37.toastmasters.features.performers

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading performer events
 *
 * @param performerRepository Repository for fetching performer data
 * @param performerId ID of the performer whose events to load
 */
class EventsByPerformerPaginationDataSource(
    private val performerRepository: PerformerRepository,
    private val performerId: Int
) : PaginationDataSource<Event> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Event> {
        return when (val result = performerRepository.getPerformerEvents(performerId, cursor)) {
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
