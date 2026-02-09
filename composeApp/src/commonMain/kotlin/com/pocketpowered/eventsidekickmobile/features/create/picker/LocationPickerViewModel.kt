package com.district37.toastmasters.features.create.picker

import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BasePaginatedSearchViewModel

/**
 * ViewModel for the location picker screen
 * Provides search and pagination functionality for selecting locations,
 * optionally filtered by venue ID
 *
 * Refactored to extend BasePaginatedSearchViewModel to eliminate duplicate
 * search and pagination logic.
 */
class LocationPickerViewModel(
    private val locationRepository: LocationRepository,
    private val venueId: Int?
) : BasePaginatedSearchViewModel<Location>() {

    override val tag = "LocationPickerViewModel"

    /**
     * Implement search operation by calling repository
     * Includes optional venueId filter
     */
    override suspend fun performSearchOperation(
        query: String?,
        cursor: String?
    ): Resource<PagedConnection<Location>> {
        return locationRepository.searchLocations(
            venueId = venueId,
            searchQuery = query,
            cursor = cursor,
            first = 20
        )
    }
}
