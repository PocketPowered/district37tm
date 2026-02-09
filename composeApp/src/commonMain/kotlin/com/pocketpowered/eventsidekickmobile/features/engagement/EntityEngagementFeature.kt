package com.district37.toastmasters.features.engagement

import com.district37.toastmasters.engagement.EngagementBehavior
import com.district37.toastmasters.engagement.EngagementKey
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.UserEngagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Composable feature for managing engagement for a specific entity.
 * Wraps EngagementManager operations and provides per-entity state.
 *
 * Usage:
 * ```kotlin
 * class MyViewModel(...) {
 *     val authFeature = AuthFeature(authRepository, viewModelScope)
 *
 *     private var _engagementFeature: EntityEngagementFeature? = null
 *     val engagementFeature: EntityEngagementFeature?
 *         get() = _engagementFeature
 *
 *     init {
 *         viewModelScope.launch {
 *             item.collect { resource ->
 *                 if (resource is Resource.Success) {
 *                     // Only initialize once to avoid overwriting optimistic updates
 *                     if (_engagementFeature == null) {
 *                         _engagementFeature = EntityEngagementFeature(
 *                             entityType = EntityType.EVENT,
 *                             entityId = event.id,
 *                             engagementManager = engagementManager,
 *                             authFeature = authFeature,
 *                             coroutineScope = viewModelScope
 *                         ).apply {
 *                             initialize(event.userEngagement)
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
class EntityEngagementFeature(
    private val entityType: EntityType,
    private val entityId: Int,
    private val engagementManager: EngagementManager,
    private val authFeature: AuthFeature,
    private val coroutineScope: CoroutineScope
) {
    private val _engagement = MutableStateFlow(UserEngagement.DEFAULT)
    val engagement: StateFlow<UserEngagement> = _engagement.asStateFlow()

    val key = EngagementKey(entityType, entityId)

    /**
     * Engagement behavior for this entity type
     */
    val behavior: EngagementBehavior = EngagementBehavior.forEntityType(entityType)

    /**
     * Whether this entity supports status (RSVP) - true for Events and Agenda Items
     */
    val supportsStatus: Boolean = behavior.supportsStatus

    init {
        // Listen for engagement updates from the manager
        coroutineScope.launch {
            engagementManager.engagementUpdates.collect { event ->
                if (event.key == key) {
                    _engagement.update { event.engagement }
                }
            }
        }

        // Listen for login required events and delegate to AuthFeature
        coroutineScope.launch {
            engagementManager.loginRequiredEvents.collect {
                authFeature.requestLogin()
            }
        }
    }

    /**
     * Initialize with engagement data from entity.
     * Should be called after the entity data is loaded.
     */
    fun initialize(engagement: UserEngagement?) {
        engagement?.let {
            engagementManager.updateCache(entityType, entityId, it)
            _engagement.update { _ -> it }
        }
    }

    /**
     * Toggle subscription state for this entity.
     * Subscribing means receiving notifications for changes to this entity.
     */
    fun toggleSubscription() {
        coroutineScope.launch {
            engagementManager.toggleSubscription(entityType, entityId)
        }
    }

    /**
     * Set engagement status for this entity (RSVP for events/agenda items)
     */
    fun setStatus(status: UserEngagementStatus?) {
        coroutineScope.launch {
            engagementManager.setStatus(entityType, entityId, status)
        }
    }
}
