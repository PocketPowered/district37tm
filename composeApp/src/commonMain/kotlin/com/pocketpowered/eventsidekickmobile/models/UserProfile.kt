package com.district37.toastmasters.models

/**
 * Unified user profile containing all contextual data for viewing another user's profile.
 * Server resolves all permission-gated fields based on friendship status.
 *
 * Fields:
 * - user: Always populated with the requested user's profile
 * - relationshipStatus: Always populated with the relationship between current user and requested user
 * - attendingEvents: Populated only if users are friends, null otherwise
 * - activityFeed: Populated only if users are friends, null otherwise
 */
data class UserProfile(
    val user: User,
    val relationshipStatus: UserRelationshipStatus,
    val attendingEvents: UserEventsConnection?,
    val activityFeed: ActivityFeedConnection?
)
