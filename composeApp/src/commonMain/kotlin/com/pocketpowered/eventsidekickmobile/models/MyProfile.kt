package com.district37.toastmasters.models

/**
 * Unified self-profile containing all data needed for viewing your own profile.
 * Unlike UserProfile (for viewing others), this includes subscribed events and friend requests,
 * and excludes relationship status.
 *
 * Fields:
 * - user: The current user's profile data (always populated)
 * - subscribedEvents: User's subscribed events (always populated)
 * - attendingEvents: User's attending/RSVP'd events (always populated)
 * - activityFeed: User's own activity feed (always populated)
 * - incomingFriendRequests: Pending friend requests to action (always populated)
 * - friendsCount: Total number of friends the user has
 */
data class MyProfile(
    val user: User,
    val subscribedEvents: UserEventsConnection,
    val attendingEvents: UserEventsConnection,
    val activityFeed: ActivityFeedConnection,
    val incomingFriendRequests: FriendRequestConnection,
    val friendsCount: Int
)
