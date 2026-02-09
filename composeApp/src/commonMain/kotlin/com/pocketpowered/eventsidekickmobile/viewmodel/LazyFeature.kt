package com.district37.toastmasters.viewmodel

import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.auth.AuthFeature
import com.district37.toastmasters.features.engagement.EntityEngagementFeature
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.HasUserEngagement
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Manages lazy initialization of a feature based on a trigger condition.
 *
 * This eliminates the boilerplate of nullable fields with manual null checks
 * for features that should only initialize when certain conditions are met
 * (e.g., when entity data is loaded, when user is authenticated, etc.)
 *
 * Usage:
 * ```kotlin
 * class EventDetailViewModel(...) {
 *     private val engagementFeature = LazyFeature(
 *         scope = viewModelScope,
 *         trigger = item.map { it is Resource.Success },
 *         factory = {
 *             EntityEngagementFeature(
 *                 entityType = EntityType.EVENT,
 *                 entityId = eventId,
 *                 engagementManager = engagementManager,
 *                 authFeature = authFeature,
 *                 scope = viewModelScope
 *             )
 *         }
 *     )
 *
 *     val engagement = engagementFeature.instance
 *     val isEngagementReady = engagementFeature.isInitialized
 * }
 * ```
 */
class LazyFeature<T>(
    private val scope: CoroutineScope,
    private val trigger: Flow<Boolean>,
    private val factory: () -> T
) {
    private var _instance: T? = null

    /**
     * The feature instance (null until initialized)
     */
    val instance: T? get() = _instance

    private val _isInitialized = MutableStateFlow(false)

    /**
     * Whether the feature has been initialized
     */
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        scope.launch {
            // Wait for trigger condition to become true
            trigger.first { it }
            // Create the feature instance
            _instance = factory()
            _isInitialized.value = true
        }
    }

    companion object {
        /**
         * Factory to create a LazyFeature for EntityEngagementFeature.
         *
         * This consolidates the common pattern used across detail ViewModels
         * (EventDetailViewModel, VenueDetailViewModel, PerformerDetailViewModel)
         * for lazy engagement feature initialization.
         *
         * Usage:
         * ```kotlin
         * private val engagementFeatureLazy = LazyFeature.forEngagement(
         *     scope = viewModelScope,
         *     itemFlow = item,
         *     entityType = EntityType.EVENT,
         *     entityId = eventId,
         *     engagementManager = engagementManager,
         *     authFeature = authFeature
         * )
         *
         * val engagementFeature: EntityEngagementFeature?
         *     get() = engagementFeatureLazy.instance
         * ```
         *
         * @param scope The coroutine scope for the lazy initialization
         * @param itemFlow The StateFlow of the entity being loaded (Resource<T>)
         * @param entityType The type of entity (EVENT, VENUE, PERFORMER, etc.)
         * @param entityId The ID of the entity
         * @param engagementManager The engagement manager for handling engagement operations
         * @param authFeature The auth feature for authentication state
         * @param getEntityId Optional function to extract entity ID from the loaded data (defaults to using entityId param)
         */
        fun <T : HasUserEngagement> forEngagement(
            scope: CoroutineScope,
            itemFlow: StateFlow<Resource<T>>,
            entityType: EntityType,
            entityId: Int,
            engagementManager: EngagementManager,
            authFeature: AuthFeature,
            getEntityId: ((T) -> Int)? = null
        ): LazyFeature<EntityEngagementFeature> = LazyFeature(
            scope = scope,
            trigger = itemFlow.map { it is Resource.Success }
        ) {
            val entity = (itemFlow.value as? Resource.Success)?.data
            EntityEngagementFeature(
                entityType = entityType,
                entityId = getEntityId?.invoke(entity!!) ?: entity?.let { entityId } ?: entityId,
                engagementManager = engagementManager,
                authFeature = authFeature,
                coroutineScope = scope
            ).apply {
                entity?.userEngagement?.let { initialize(it) }
            }
        }
    }
}
