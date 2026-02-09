package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.VenueDetails
import com.district37.toastmasters.graphql.fragment.VenuePreview
import com.district37.toastmasters.models.PagedField
import com.district37.toastmasters.models.Venue

/**
 * Transforms GraphQL VenuePreview fragment to domain Venue model
 */
fun VenuePreview.toVenue(): Venue {
    return Venue(
        id = id,
        name = name,
        address = address,
        city = city,
        state = state,
        zipCode = zipCode,
        capacity = null,
        latitude = latitude,
        longitude = longitude,
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() }
    )
}

/**
 * Transforms GraphQL VenueDetails fragment to domain Venue model
 */
fun VenueDetails.toVenue(): Venue {
    return Venue(
        id = id,
        name = name,
        address = address,
        city = city,
        state = state,
        zipCode = zipCode,
        capacity = capacity,
        latitude = latitude,
        longitude = longitude,
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() },
        events = PagedField(
            items = eventsConnection.edges.map { it.node.eventPreview.toEvent() },
            totalCount = eventCount,
            hasMore = eventsConnection.pageInfo.paginationInfo.hasNextPage,
            cursor = eventsConnection.pageInfo.paginationInfo.endCursor
        ),
        permissions = permissions?.entityPermissionsDetails?.toEntityPermissions()
    )
}
