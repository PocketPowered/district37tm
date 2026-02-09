package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.LocationDetails
import com.district37.toastmasters.graphql.fragment.LocationPreview
import com.district37.toastmasters.models.Location

/**
 * Transforms GraphQL LocationPreview fragment to domain Location model
 */
fun LocationPreview.toLocation(): Location {
    return Location(
        id = id,
        name = name,
        description = description,
        venueId = null
    )
}

/**
 * Transforms GraphQL LocationDetails fragment to domain Location model
 */
fun LocationDetails.toLocation(): Location {
    return Location(
        id = id,
        name = name,
        description = description,
        venueId = venueId,
        locationType = locationType,
        capacity = capacity,
        floorLevel = floorLevel,
        createdAt = createdAt,
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() }
    )
}
