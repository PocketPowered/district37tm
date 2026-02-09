package com.district37.toastmasters.models

/**
 * Interface for entities that support user engagement (RSVP, follow, save, etc.)
 *
 * This interface allows LazyFeature.forEngagement() to automatically extract
 * and initialize engagement state from any entity that implements it,
 * eliminating duplicate engagement initialization code across detail ViewModels.
 *
 * Implement this interface in model classes that have userEngagement field:
 * - Event
 * - Venue
 * - Performer
 * - Location
 * - Organization
 * - AgendaItem
 */
interface HasUserEngagement {
    val userEngagement: UserEngagement?
}
