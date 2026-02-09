package com.district37.toastmasters.features.venues

import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.viewmodel.BasePreviewViewModel

/**
 * ViewModel for venue preview component
 * Fetches and caches venue data by ID
 */
class VenuePreviewViewModel(
    venueRepository: VenueRepository,
    venueId: Int
) : BasePreviewViewModel<Venue, VenueRepository>(venueId, venueRepository) {
    override val tag = "VenuePreviewViewModel"
}
