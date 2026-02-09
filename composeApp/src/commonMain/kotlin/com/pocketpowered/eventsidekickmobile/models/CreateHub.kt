package com.district37.toastmasters.models

/**
 * Wrapper for a list of events with pagination metadata
 */
data class EventsWithPagination(
    val items: List<Event>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Wrapper for a list of venues with pagination metadata
 */
data class VenuesWithPagination(
    val items: List<Venue>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Wrapper for a list of performers with pagination metadata
 */
data class PerformersWithPagination(
    val items: List<Performer>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Wrapper for a list of organizations with pagination metadata
 */
data class OrganizationsWithPagination(
    val items: List<Organization>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Represents the Create Hub page data containing user's editable entities (owned + collaborated)
 */
data class CreateHub(
    val myEvents: EventsWithPagination,
    val myVenues: VenuesWithPagination,
    val myPerformers: PerformersWithPagination,
    val myOrganizations: OrganizationsWithPagination
)
