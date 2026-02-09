package com.district37.toastmasters.features.create.viewall

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading user's editable venues (owned + collaborated)
 */
class MyEditableVenuesPaginationDataSource(
    private val createHubRepository: CreateHubRepository
) : PaginationDataSource<Venue> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Venue> {
        return when (val result = createHubRepository.getMyEditableVenues(cursor)) {
            is Resource.Success -> {
                val data = result.data
                PaginationResult(
                    items = data.items,
                    hasMore = data.hasNextPage,
                    nextCursor = data.endCursor
                )
            }
            is Resource.Error -> {
                throw Exception(result.message ?: "Failed to load venues")
            }
            else -> {
                throw Exception("Unexpected state while loading venues")
            }
        }
    }
}
