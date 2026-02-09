package com.district37.toastmasters.features.venues

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseDetailViewModel
import com.district37.toastmasters.viewmodel.LazyFeature
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for VenueDetail screen
 *
 * Loads venue details with nested events and supports pagination.
 * Permissions are automatically extracted by BaseDetailViewModel.
 */
class VenueDetailViewModel(
    private val venueRepository: VenueRepository,
    private val engagementManager: EngagementManager,
    private val authRepository: AuthRepository,
    private val venueId: Int
) : BaseDetailViewModel<Venue, VenueRepository>(venueId, venueRepository) {

    override val tag = "VenueDetailViewModel"

    /**
     * Feature for managing authentication state and login flows
     */
    val authFeature = AuthFeature(authRepository, viewModelScope)

    /**
     * Feature for managing engagement (follow) for this venue
     */
    private val engagementFeatureLazy = LazyFeature(
        scope = viewModelScope,
        trigger = item.map { it is Resource.Success }
    ) {
        val venue = (item.value as? Resource.Success)?.data
        EntityEngagementFeature(
            entityType = EntityType.VENUE,
            entityId = venue?.id ?: venueId,
            engagementManager = engagementManager,
            authFeature = authFeature,
            coroutineScope = viewModelScope
        ).apply {
            venue?.userEngagement?.let { initialize(it) }
        }
    }

    val engagementFeature: EntityEngagementFeature?
        get() = engagementFeatureLazy.instance
}
