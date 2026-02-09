package com.district37.toastmasters.features.locations

import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.viewmodel.BaseDetailViewModel

/**
 * ViewModel for location detail screen
 * Loads location details
 */
class LocationDetailViewModel(
    private val locationRepository: LocationRepository,
    private val locationId: Int
) : BaseDetailViewModel<Location, LocationRepository>(locationId, locationRepository) {

    override val tag = "LocationDetailViewModel"
}
