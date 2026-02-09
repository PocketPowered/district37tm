package com.district37.toastmasters.models

import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.graphql.type.EntityType

/**
 * Domain model for user engagement with entities.
 * Tracks whether the user is subscribed to an entity (receiving notifications for changes)
 * and optionally their RSVP status (for events and agenda items only).
 */
data class UserEngagement(
    val isSubscribed: Boolean,
    val status: UserEngagementStatus?
) {
    companion object {
        /**
         * Default engagement state for entities with no user engagement data.
         */
        val DEFAULT = UserEngagement(
            isSubscribed = false,
            status = null
        )
    }
}

// Re-export GraphQL types for convenience
typealias EngagementStatus = UserEngagementStatus
typealias EngagementEntityType = EntityType
