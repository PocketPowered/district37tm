package com.district37.toastmasters.models

import com.district37.toastmasters.graphql.type.EventType

/**
 * Display format for carousel cards, determined by the server.
 */
enum class CarouselDisplayFormat {
    /** Hero/featured format - large cards, prominent display */
    HERO,
    /** Medium format - standard cards */
    MEDIUM
}

/**
 * Represents a carousel of events with metadata
 */
data class EventCarousel(
    val title: String,
    val events: List<Event>,
    val hasMore: Boolean,
    val totalCount: Int,
    val displayFormat: CarouselDisplayFormat,
    val eventType: EventType? = null
)

/**
 * Represents a city near the user's location with event information
 */
data class NearbyCity(
    val city: String,
    val state: String?,
    val eventCount: Int,
    val distanceMiles: Double
)

/**
 * Represents the explore page containing event carousels.
 * All carousels (including upcoming events and nearby city) are in the carousels list.
 * The server controls the order and display format of each carousel.
 */
data class ExplorePage(
    val carousels: List<EventCarousel>,
    val nearbyCity: NearbyCity? = null
)
