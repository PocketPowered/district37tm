package com.district37.toastmasters.features.performers

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.AgendaItemConnection
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseDetailViewModel
import com.district37.toastmasters.viewmodel.LazyFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for performer detail screen
 *
 * Loads performer details and supports engagement features.
 * Permissions are automatically extracted by BaseDetailViewModel.
 */
class PerformerDetailViewModel(
    private val performerRepository: PerformerRepository,
    private val engagementManager: EngagementManager,
    private val authRepository: AuthRepository,
    private val performerId: Int
) : BaseDetailViewModel<Performer, PerformerRepository>(performerId, performerRepository) {

    override val tag = "PerformerDetailViewModel"

    /**
     * Feature for managing authentication state and login flows
     */
    val authFeature = AuthFeature(authRepository, viewModelScope)

    /**
     * Feature for managing engagement (follow) for this performer
     */
    private val engagementFeatureLazy = LazyFeature(
        scope = viewModelScope,
        trigger = item.map { it is Resource.Success }
    ) {
        val performer = (item.value as? Resource.Success)?.data
        EntityEngagementFeature(
            entityType = EntityType.PERFORMER,
            entityId = performer?.id ?: performerId,
            engagementManager = engagementManager,
            authFeature = authFeature,
            coroutineScope = viewModelScope
        ).apply {
            performer?.userEngagement?.let { initialize(it) }
        }
    }

    val engagementFeature: EntityEngagementFeature?
        get() = engagementFeatureLazy.instance

    /**
     * State flow for performer's agenda items (schedule)
     */
    private val _agendaItems = MutableStateFlow<Resource<AgendaItemConnection>>(Resource.Loading)
    val agendaItems: StateFlow<Resource<AgendaItemConnection>> = _agendaItems.asStateFlow()

    init {
        // Load agenda items when performer details load successfully
        viewModelScope.launch {
            item.collect { resource ->
                if (resource is Resource.Success) {
                    loadAgendaItems()
                }
            }
        }
    }

    /**
     * Load agenda items for this performer
     */
    private fun loadAgendaItems() {
        viewModelScope.launch {
            _agendaItems.value = Resource.Loading
            _agendaItems.value = performerRepository.getPerformerAgendaItems(performerId)
        }
    }

    /**
     * Load more agenda items (pagination)
     */
    fun loadMoreAgendaItems() {
        val currentState = _agendaItems.value
        if (currentState !is Resource.Success) return

        val cursor = currentState.data.endCursor ?: return
        if (!currentState.data.hasNextPage) return

        viewModelScope.launch {
            val result = performerRepository.getPerformerAgendaItems(performerId, cursor)
            if (result is Resource.Success) {
                // Append new items to existing list
                _agendaItems.value = Resource.Success(
                    AgendaItemConnection(
                        agendaItems = currentState.data.agendaItems + result.data.agendaItems,
                        hasNextPage = result.data.hasNextPage,
                        endCursor = result.data.endCursor,
                        totalCount = result.data.totalCount
                    )
                )
            }
        }
    }
}
