package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.AgendaDateFragment
import com.district37.toastmasters.graphql.fragment.EventDetails
import com.district37.toastmasters.graphql.fragment.EventPreview
import com.district37.toastmasters.graphql.fragment.LocalizedTimeFragment
import com.district37.toastmasters.models.AgendaDate
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.LocalizedTime
import kotlinx.datetime.LocalDate

/**
 * Transforms GraphQL LocalizedTimeFragment to domain LocalizedTime model
 */
fun LocalizedTimeFragment.toLocalizedTime(): LocalizedTime {
    return LocalizedTime(
        instant = instant,
        timezone = timezone
    )
}

/**
 * Transforms GraphQL EventPreview fragment to domain Event model
 */
fun EventPreview.toEvent(): Event {
    return Event(
        id = id,
        slug = slug,
        name = name,
        description = description,
        eventType = eventType,
        startDate = timeRange?.start?.localizedTimeFragment?.toLocalizedTime(),
        endDate = timeRange?.end?.localizedTimeFragment?.toLocalizedTime(),
        venueId = venueId,
        images = imagesConnection.edges.take(1).map { it.node.imageDetails.toImage() },
        userEngagement = userEngagement?.userEngagementDetails?.toUserEngagement(),
        archivedAt = archivedAt
    )
}

/**
 * Transforms GraphQL EventDetails fragment to domain Event model
 */
fun EventDetails.toEvent(): Event {
    return Event(
        id = id,
        slug = slug,
        name = name,
        description = description,
        eventType = eventType,
        startDate = timeRange?.start?.localizedTimeFragment?.toLocalizedTime(),
        endDate = timeRange?.end?.localizedTimeFragment?.toLocalizedTime(),
        venueId = venueId,
        venue = venue?.venuePreview?.toVenue(),
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() },
        agendaDates = agendaDates.map { it.agendaDateFragment.toAgendaDate() },
        userEngagement = userEngagement?.userEngagementDetails?.toUserEngagement(),
        permissions = permissions?.entityPermissionsDetails?.toEntityPermissions(),
        archivedAt = archivedAt
    )
}

/**
 * Transforms GraphQL AgendaDateFragment to domain AgendaDate model
 */
fun AgendaDateFragment.toAgendaDate(): AgendaDate {
    return AgendaDate(
        date = LocalDate.parse(date.toString()),
        itemCount = itemCount
    )
}
