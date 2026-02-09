package com.district37.toastmasters.features.engagement

import com.district37.toastmasters.data.repository.FriendRsvpRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.FriendRsvp
import com.district37.toastmasters.models.FriendRsvpConnection
import com.district37.toastmasters.util.PaginatedDataLoader
import com.district37.toastmasters.util.PaginationResult
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Feature for managing friend RSVP display and pagination.
 * Loads friend RSVPs for a specific entity (Event or ScheduleItem).
 *
 * Usage:
 * ```kotlin
 * class EventDetailViewModel(...) {
 *     private var _friendRsvpsFeature: FriendRsvpsFeature? = null
 *     val friendRsvpsFeature: FriendRsvpsFeature?
 *         get() = _friendRsvpsFeature
 *
 *     init {
 *         if (authFeature.isAuthenticated) {
 *             _friendRsvpsFeature = FriendRsvpsFeature(
 *                 entityType = EntityType.EVENT,
 *                 entityId = eventId,
 *                 friendRsvpRepository = friendRsvpRepository,
 *                 coroutineScope = viewModelScope
 *             )
 *         }
 *     }
 * }
 * ```
 */
class FriendRsvpsFeature(
    private val entityType: EntityType,
    private val entityId: Int,
    private val friendRsvpRepository: FriendRsvpRepository,
    private val coroutineScope: CoroutineScope
) {
    // Preview state (first 3 friends for display in engagement bar)
    private val _preview = MutableStateFlow<Resource<FriendRsvpConnection>>(Resource.Loading)
    val preview: StateFlow<Resource<FriendRsvpConnection>> = _preview.asStateFlow()

    // Full list state (for modal with pagination) - using PaginatedDataLoader
    private val fullListLoader = PaginatedDataLoader<FriendRsvp>(
        scope = coroutineScope,
        loader = { after ->
            friendRsvpRepository.getFriendRsvps(
                entityType = entityType,
                entityId = entityId,
                first = 20,
                after = after
            ).map { connection ->
                PaginationResult(
                    items = connection.rsvps,
                    endCursor = connection.endCursor,
                    hasNextPage = connection.hasNextPage
                )
            }
        }
    )

    val fullList: StateFlow<Resource<List<FriendRsvp>>> = fullListLoader.data
    val isLoadingMore: StateFlow<Boolean> = fullListLoader.isLoadingMore

    init {
        // Automatically load preview on initialization
        loadPreview()
    }

    /**
     * Load the first 3 friends for preview display in engagement bar
     */
    fun loadPreview() {
        coroutineScope.launch {
            _preview.value = Resource.Loading
            _preview.value = friendRsvpRepository.getFriendRsvpsPreview(entityType, entityId)
        }
    }

    /**
     * Load full paginated list (for modal)
     */
    fun loadFullList() {
        fullListLoader.loadInitial()
    }

    /**
     * Load more friends (pagination in modal)
     */
    fun loadMore() {
        fullListLoader.loadMore()
    }

    /**
     * Refresh friend RSVPs (call when user changes their own RSVP)
     */
    fun refresh() {
        loadPreview()
        // Also refresh full list if it was already loaded
        if (fullList.value !is Resource.NotLoading) {
            loadFullList()
        }
    }
}
