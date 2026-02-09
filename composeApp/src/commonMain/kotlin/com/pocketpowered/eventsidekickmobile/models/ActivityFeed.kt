package com.district37.toastmasters.models

import com.district37.toastmasters.graphql.type.ActivityType
import com.district37.toastmasters.graphql.type.EntityType
import kotlinx.datetime.Instant

/**
 * Domain model for Activity Feed Item
 */
data class ActivityFeedItem(
    val id: Int,
    val userId: String,
    val activityType: ActivityType,
    val entityType: EntityType,
    val entityId: Int,
    val createdAt: Instant,
    val user: UserProfilePreview?,
    val event: Event?,
    val agendaItem: AgendaItem?,
    val venue: Venue?,
    val performer: Performer?,
    val location: Location?,
    val organization: Organization?,
    val isCurrentUser: Boolean = false
) {
    /**
     * Get the display name of the entity
     */
    val entityName: String
        get() = event?.name
            ?: agendaItem?.title
            ?: venue?.name
            ?: performer?.name
            ?: location?.name
            ?: organization?.name
            ?: "Unknown"

    /**
     * Get the first image URL for the entity
     */
    val entityImageUrl: String?
        get() = event?.images?.firstOrNull()?.url
            ?: performer?.images?.firstOrNull()?.url
            ?: location?.images?.firstOrNull()?.url
            ?: organization?.logoUrl
            ?: organization?.images?.firstOrNull()?.url
}

/**
 * Preview version of UserProfile for activity feed
 */
data class UserProfilePreview(
    val id: String,
    val displayName: String?,
    val profileImageUrl: String?
)

/**
 * Connection type for paginated activity feed items
 */
data class ActivityFeedConnection(
    val items: List<ActivityFeedItem>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
) {
    companion object {
        fun empty() = ActivityFeedConnection(
            items = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}
