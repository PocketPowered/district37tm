package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.PerformerDetails
import com.district37.toastmasters.graphql.fragment.PerformerPreview
import com.district37.toastmasters.models.PagedField
import com.district37.toastmasters.models.Performer

/**
 * Transforms GraphQL PerformerPreview fragment to domain Performer model
 */
fun PerformerPreview.toPerformer(): Performer {
    return Performer(
        id = id,
        name = name,
        bio = bio,
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() }
    )
}

/**
 * Transforms GraphQL PerformerDetails fragment to domain Performer model
 */
fun PerformerDetails.toPerformer(): Performer {
    return Performer(
        id = id,
        name = name,
        bio = bio,
        performerType = performerType,
        createdAt = createdAt,
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
