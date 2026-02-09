package com.district37.toastmasters.features.create.viewall

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading user's editable performers (owned + collaborated)
 */
class MyEditablePerformersPaginationDataSource(
    private val createHubRepository: CreateHubRepository
) : PaginationDataSource<Performer> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Performer> {
        return when (val result = createHubRepository.getMyEditablePerformers(cursor)) {
            is Resource.Success -> {
                val data = result.data
                PaginationResult(
                    items = data.items,
                    hasMore = data.hasNextPage,
                    nextCursor = data.endCursor
                )
            }
            is Resource.Error -> {
                throw Exception(result.message ?: "Failed to load performers")
            }
            else -> {
                throw Exception("Unexpected state while loading performers")
            }
        }
    }
}
