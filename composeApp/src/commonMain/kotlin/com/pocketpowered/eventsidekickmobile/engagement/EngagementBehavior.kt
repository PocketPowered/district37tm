package com.district37.toastmasters.engagement

import com.district37.toastmasters.graphql.type.EntityType

/**
 * Defines engagement behavior for different entity types.
 *
 * This abstraction allows the UI to adapt based on entity type:
 * - Events and Agenda Items support RSVP status (Going/Not Going/Undecided)
 * - All other entities (Performers, Venues, Locations, etc.) are subscribe-only
 *
 * All entity types use "Subscribe" terminology uniformly.
 * Subscribing means receiving notifications for changes to that entity.
 */
sealed class EngagementBehavior {
    abstract val actionLabel: String      // "Subscribe"
    abstract val engagedLabel: String     // "Subscribed"
    abstract val disengageLabel: String   // "Unsubscribe"
    abstract val supportsStatus: Boolean  // true for Events/Agenda Items, false for others

    /**
     * Subscribe behavior with RSVP status support.
     * Used for Events and Agenda Items.
     */
    data object SubscribeWithStatus : EngagementBehavior() {
        override val actionLabel = "Subscribe"
        override val engagedLabel = "Subscribed"
        override val disengageLabel = "Unsubscribe"
        override val supportsStatus = true
    }

    /**
     * Subscribe behavior without status support.
     * Used for Performers, Venues, Locations, and Organizations.
     */
    data object SubscribeOnly : EngagementBehavior() {
        override val actionLabel = "Subscribe"
        override val engagedLabel = "Subscribed"
        override val disengageLabel = "Unsubscribe"
        override val supportsStatus = false
    }

    companion object {
        /**
         * Get the appropriate behavior for an entity type.
         */
        fun forEntityType(entityType: EntityType): EngagementBehavior = when (entityType) {
            EntityType.EVENT, EntityType.AGENDAITEM -> SubscribeWithStatus
            EntityType.PERFORMER, EntityType.VENUE, EntityType.LOCATION, EntityType.ORGANIZATION -> SubscribeOnly
            EntityType.UNKNOWN__ -> SubscribeOnly // Default to subscribe-only for unknown types
        }
    }
}
