package com.district37.toastmasters.engagement

import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.UserEngagementRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.UserEngagement
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/**
 * Key for identifying engagement state per entity
 */
data class EngagementKey(
    val entityType: EntityType,
    val entityId: Int
)

/**
 * State for engagement operations
 */
sealed class EngagementOperationState {
    data object Idle : EngagementOperationState()
    data object Loading : EngagementOperationState()
    data class Success(val engagement: UserEngagement) : EngagementOperationState()
    data class Error(val message: String, val previousEngagement: UserEngagement) : EngagementOperationState()
}

/**
 * Event emitted when engagement changes for an entity
 */
data class EngagementUpdateEvent(
    val key: EngagementKey,
    val engagement: UserEngagement
)

/**
 * Event emitted when user needs to login to perform engagement action
 */
data object LoginRequiredEvent

/**
 * Centralized manager for user engagement actions across the app.
 * Handles optimistic updates, error rollback, and auth state checking.
 */
class EngagementManager(
    private val userEngagementRepository: UserEngagementRepository,
    private val authRepository: AuthRepository
) {
    private val TAG = "EngagementManager"

    // Cache of engagement states per entity
    private val _engagementCache = MutableStateFlow<Map<EngagementKey, UserEngagement>>(emptyMap())
    val engagementCache: StateFlow<Map<EngagementKey, UserEngagement>> = _engagementCache.asStateFlow()

    // Event flow for engagement updates (for ViewModels to observe)
    private val _engagementUpdates = MutableSharedFlow<EngagementUpdateEvent>()
    val engagementUpdates: SharedFlow<EngagementUpdateEvent> = _engagementUpdates.asSharedFlow()

    // Event flow for login required prompts
    private val _loginRequiredEvents = MutableSharedFlow<LoginRequiredEvent>()
    val loginRequiredEvents: SharedFlow<LoginRequiredEvent> = _loginRequiredEvents.asSharedFlow()

    // Operation states per entity for UI loading states
    private val _operationStates = MutableStateFlow<Map<EngagementKey, EngagementOperationState>>(emptyMap())
    val operationStates: StateFlow<Map<EngagementKey, EngagementOperationState>> = _operationStates.asStateFlow()

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return authRepository.authState.value is AuthState.Authenticated
    }

    /**
     * Update the cache with initial engagement data from API responses
     */
    fun updateCache(entityType: EntityType, entityId: Int, engagement: UserEngagement?) {
        if (engagement == null) return
        val key = EngagementKey(entityType, entityId)
        _engagementCache.update { it + (key to engagement) }
    }

    /**
     * Get engagement from cache or return default
     */
    fun getEngagement(entityType: EntityType, entityId: Int): UserEngagement {
        val key = EngagementKey(entityType, entityId)
        return _engagementCache.value[key] ?: UserEngagement.DEFAULT
    }

    /**
     * Toggle subscription state for any entity type.
     * Subscribing means receiving notifications for changes to this entity.
     */
    suspend fun toggleSubscription(entityType: EntityType, entityId: Int) {
        val key = EngagementKey(entityType, entityId)
        val currentEngagement = getEngagement(entityType, entityId)

        if (!checkAuthAndEmit()) return

        // Optimistic update
        val optimisticEngagement = currentEngagement.copy(isSubscribed = !currentEngagement.isSubscribed)
        applyOptimisticUpdate(key, optimisticEngagement)

        // Execute mutation
        val result = if (currentEngagement.isSubscribed) {
            userEngagementRepository.unsubscribeFromEntity(entityType, entityId)
        } else {
            userEngagementRepository.subscribeToEntity(entityType, entityId)
        }

        handleMutationResult(key, result, currentEngagement)
    }

    /**
     * Set engagement status for an entity (RSVP for events/agenda items)
     */
    suspend fun setStatus(entityType: EntityType, entityId: Int, status: UserEngagementStatus?) {
        val key = EngagementKey(entityType, entityId)
        val currentEngagement = getEngagement(entityType, entityId)

        if (!checkAuthAndEmit()) return

        // Optimistic update
        val optimisticEngagement = currentEngagement.copy(status = status)
        applyOptimisticUpdate(key, optimisticEngagement)

        // Execute mutation
        val result = userEngagementRepository.setEntityStatus(entityType, entityId, status)

        handleMutationResult(key, result, currentEngagement)
    }

    /**
     * Check if user is authenticated, emit login required event if not
     */
    private suspend fun checkAuthAndEmit(): Boolean {
        if (!isAuthenticated()) {
            Logger.d(TAG, "User not authenticated, emitting login required event")
            _loginRequiredEvents.emit(LoginRequiredEvent)
            return false
        }
        return true
    }

    /**
     * Apply optimistic update to cache and emit update event
     */
    private suspend fun applyOptimisticUpdate(key: EngagementKey, engagement: UserEngagement) {
        Logger.d(TAG, "Applying optimistic update for $key")
        _engagementCache.update { it + (key to engagement) }
        _operationStates.update { it + (key to EngagementOperationState.Loading) }
        _engagementUpdates.emit(EngagementUpdateEvent(key, engagement))
    }

    /**
     * Handle mutation result - update cache on success, rollback on failure
     */
    private suspend fun handleMutationResult(
        key: EngagementKey,
        result: Resource<UserEngagement>,
        previousEngagement: UserEngagement
    ) {
        when (result) {
            is Resource.Success -> {
                Logger.d(TAG, "Mutation successful for $key")
                _engagementCache.update { it + (key to result.data) }
                _operationStates.update { it + (key to EngagementOperationState.Success(result.data)) }
                _engagementUpdates.emit(EngagementUpdateEvent(key, result.data))
            }
            is Resource.Error -> {
                Logger.e(TAG, "Mutation failed for $key: ${result.message}")
                // Rollback to previous state
                _engagementCache.update { it + (key to previousEngagement) }
                _operationStates.update {
                    it + (key to EngagementOperationState.Error(
                        result.message ?: "Unknown error",
                        previousEngagement
                    ))
                }
                _engagementUpdates.emit(EngagementUpdateEvent(key, previousEngagement))
            }
            is Resource.Loading, Resource.NotLoading -> {
                // Already handled in applyOptimisticUpdate or shouldn't happen
            }
        }
    }

    /**
     * Clear operation state for an entity (e.g., after showing error snackbar)
     */
    fun clearOperationState(entityType: EntityType, entityId: Int) {
        val key = EngagementKey(entityType, entityId)
        _operationStates.update { it + (key to EngagementOperationState.Idle) }
    }
}
