package com.district37.toastmasters.features.venues

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading venue events
 *
 * @param venueRepository Repository for fetching venue data
 * @param venueId ID of the venue whose events to load
 */
class EventsByVenuePaginationDataSource(
    private val venueRepository: VenueRepository,
    private val venueId: Int
) : PaginationDataSource<Event> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Event> {
        return when (val result = venueRepository.getVenueEvents(venueId, cursor)) {
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
