package com.district37.toastmasters.components.events

import com.district37.toastmasters.common.pagination.PaginationDataSource
import com.district37.toastmasters.common.pagination.PaginationResult
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.Resource

class EventsPagedDataSource(
    private val eventRepository: EventRepository,
    private val eventType: EventType? = null
) : PaginationDataSource<Event> {
    override suspend fun loadMore(cursor: String?): PaginationResult<Event> {
        return when (val result = eventRepository.getEvents(after = cursor, eventType = eventType)) {
            is Resource.Success -> {
                val data = result.data
                PaginationResult(
                    items = data.events,
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