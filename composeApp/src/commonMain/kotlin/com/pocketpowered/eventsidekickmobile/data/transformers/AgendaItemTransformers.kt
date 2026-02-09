package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.AgendaItemDetails
import com.district37.toastmasters.graphql.fragment.AgendaItemPreview
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemTag

/**
 * Transforms GraphQL AgendaItemPreview fragment to domain AgendaItem model
 */
fun AgendaItemPreview.toAgendaItem(): AgendaItem {
    return AgendaItem(
        id = id,
        eventId = eventId,
        title = title,
        description = description,
        startTime = timeRange?.start?.localizedTimeFragment?.toLocalizedTime(),
        endTime = timeRange?.end?.localizedTimeFragment?.toLocalizedTime(),
        performerIds = emptyList(),
        locationId = location?.locationPreview?.id,
        tag = AgendaItemTag.fromString(tag?.name),
        performers = performers.mapNotNull { it.performerPreview.toPerformer() },
        location = location?.locationPreview?.toLocation(),
        userEngagement = userEngagement?.userEngagementDetails?.toUserEngagement()
    )
}

/**
 * Transforms GraphQL AgendaItemDetails fragment to domain AgendaItem model
 */
fun AgendaItemDetails.toAgendaItem(): AgendaItem {
    return AgendaItem(
        id = id,
        eventId = eventId,
        title = title,
        description = description,
        startTime = timeRange?.start?.localizedTimeFragment?.toLocalizedTime(),
        endTime = timeRange?.end?.localizedTimeFragment?.toLocalizedTime(),
        performerIds = performerIds,
        locationId = locationId,
        tag = AgendaItemTag.fromString(tag?.name),
        performers = performers.mapNotNull { it.performerPreview.toPerformer() },
        location = location?.locationPreview?.toLocation(),
        event = event.eventPreview.toEvent(),
        userEngagement = userEngagement?.userEngagementDetails?.toUserEngagement(),
        permissions = permissions?.entityPermissionsDetails?.toEntityPermissions()
    )
}
