package com.district37.toastmasters.features.organizations

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource

/**
 * Pagination data source for loading organization events
 *
 * @param organizationRepository Repository for fetching organization data
 * @param organizationId ID of the organization whose events to load
 */
class EventsByOrganizationPaginationDataSource(
    private val organizationRepository: OrganizationRepository,
    private val organizationId: Int
) : PaginationDataSource<Event> {

    override suspend fun loadMore(cursor: String?): PaginationResult<Event> {
        return when (val result = organizationRepository.getOrganizationEvents(organizationId, cursor)) {
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
