package com.district37.toastmasters.features.locations

import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.viewmodel.BasePreviewViewModel

/**
 * ViewModel for location preview component
 * Fetches location by ID
 */
class LocationPreviewViewModel(
    locationRepository: LocationRepository,
    locationId: Int
) : BasePreviewViewModel<Location, LocationRepository>(locationId, locationRepository) {
    override val tag = "LocationPreviewViewModel"
}
