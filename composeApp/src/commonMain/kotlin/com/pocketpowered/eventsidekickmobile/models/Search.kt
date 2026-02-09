package com.district37.toastmasters.models

/**
 * Sealed class representing different types of omnisearch results
 */
sealed class OmnisearchResultItem {
    data class EventResult(val event: Event) : OmnisearchResultItem()
    data class VenueResult(val venue: Venue) : OmnisearchResultItem()
    data class PerformerResult(val performer: Performer) : OmnisearchResultItem()
    data class UserResult(val user: User) : OmnisearchResultItem()
    data class OrganizationResult(val organization: Organization) : OmnisearchResultItem()
}

/**
 * Container for omnisearch results
 */
data class OmnisearchResult(
    val results: List<OmnisearchResultItem>,
    val totalCount: Int,
    val hasMore: Boolean
)
