package com.district37.toastmasters.features.organizations

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseDetailViewModel
import com.district37.toastmasters.viewmodel.LazyFeature
import kotlinx.coroutines.flow.map

/**
 * ViewModel for OrganizationDetail screen
 *
 * Loads organization details with nested events and members, supports engagement (follow).
 * Permissions are automatically extracted by BaseDetailViewModel.
 */
class OrganizationDetailViewModel(
    private val organizationRepository: OrganizationRepository,
    private val engagementManager: EngagementManager,
    private val authRepository: AuthRepository,
    private val organizationId: Int
) : BaseDetailViewModel<Organization, OrganizationRepository>(organizationId, organizationRepository) {

    override val tag = "OrganizationDetailViewModel"

    /**
     * Feature for managing authentication state and login flows
     */
    val authFeature = AuthFeature(authRepository, viewModelScope)

    /**
     * Feature for managing engagement (follow) for this organization
     */
    private val engagementFeatureLazy = LazyFeature(
        scope = viewModelScope,
        trigger = item.map { it is Resource.Success }
    ) {
        val organization = (item.value as? Resource.Success)?.data
        EntityEngagementFeature(
            entityType = EntityType.ORGANIZATION,
            entityId = organization?.id ?: organizationId,
            engagementManager = engagementManager,
            authFeature = authFeature,
            coroutineScope = viewModelScope
        ).apply {
            organization?.userEngagement?.let { initialize(it) }
        }
    }

    val engagementFeature: EntityEngagementFeature?
        get() = engagementFeatureLazy.instance
}
