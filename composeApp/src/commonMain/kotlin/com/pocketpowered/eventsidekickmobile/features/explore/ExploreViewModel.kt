package com.district37.toastmasters.features.explore

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.ActivityFeedRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.infra.location.GeolocationService
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.models.EventCarousel
import com.district37.toastmasters.models.NearbyCity
import com.district37.toastmasters.util.PaginatedDataLoader
import com.district37.toastmasters.util.PaginationResult
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Explore screen.
 * Fetches carousels from the server which include all carousel types
 * (upcoming events, nearby city, event types) in server-controlled order.
 */
class ExploreViewModel(
    private val eventRepository: EventRepository,
    private val activityFeedRepository: ActivityFeedRepository,
    private val geolocationService: GeolocationService,
    private val eventsPerCarousel: Int = 7
) : LoggingViewModel() {

    private val _carousels = MutableStateFlow<Resource<List<EventCarousel>>>(Resource.Loading)
    val carousels: StateFlow<Resource<List<EventCarousel>>> = _carousels.asStateFlow()

    private val _nearbyCity = MutableStateFlow<NearbyCity?>(null)
    val nearbyCity: StateFlow<NearbyCity?> = _nearbyCity.asStateFlow()

    // Activity feed using PaginatedDataLoader
    private val activityFeedLoader = PaginatedDataLoader<ActivityFeedItem>(
        scope = viewModelScope,
        loader = { after ->
            activityFeedRepository.getFriendActivityFeed(
                after = after,
                includeSelf = true
            ).map { connection ->
                PaginationResult(
                    items = connection.items,
                    endCursor = connection.endCursor,
                    hasNextPage = connection.hasNextPage
                )
            }
        }
    )

    val activityFeed: StateFlow<Resource<List<ActivityFeedItem>>> = activityFeedLoader.data
    val isLoadingMoreActivity: StateFlow<Boolean> = activityFeedLoader.isLoadingMore

    init {
        loadExplorePage()
        loadActivityFeed()
    }

    private fun loadExplorePage() {
        viewModelScope.launch {
            _carousels.update { Resource.Loading }

            // Try to get user location (silent request - no prompts if denied)
            val location = try {
                geolocationService.requestPermission()
                geolocationService.getCurrentLocation()
            } catch (e: Exception) {
                null // Gracefully handle any location errors
            }

            // Fetch explore page with optional location
            val result = eventRepository.getExplorePage(
                eventsPerCarousel = eventsPerCarousel,
                latitude = location?.latitude,
                longitude = location?.longitude
            )

            // Update states - extract nearby city metadata if successful
            if (result is Resource.Success) {
                _nearbyCity.update { result.data.nearbyCity }
            }
            _carousels.update { result.map { it.carousels } }
        }
    }

    private fun loadActivityFeed() {
        activityFeedLoader.loadInitial()
    }

    fun loadMoreActivity() {
        activityFeedLoader.loadMore()
    }

    fun refresh() {
        loadExplorePage()
        loadActivityFeed()
    }
}
