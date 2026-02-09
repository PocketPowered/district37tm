package com.district37.toastmasters.models

/**
 * Domain model for Venue
 */
data class Venue(
    val id: Int,
    val slug: String? = null,
    val name: String,
    val address: String?,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val capacity: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val images: List<Image> = emptyList(),
    // Paged field for events (replaces events, eventCount, hasMoreEvents, eventsCursor)
    val events: PagedField<Event> = PagedField(),
    override val userEngagement: UserEngagement? = null,
    override val permissions: EntityPermissions? = null
) : HasPermissions, HasUserEngagement
