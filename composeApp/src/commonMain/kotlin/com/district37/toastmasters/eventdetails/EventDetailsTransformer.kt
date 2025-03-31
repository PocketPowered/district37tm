package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.EventDetails
import com.district37.toastmasters.models.ExternalLink
import com.district37.toastmasters.util.EpochTimeTransformer
import com.wongislandd.nexus.util.Transformer

class EventDetailsTransformer(
    private val epochTimeTransformer: EpochTimeTransformer
): Transformer<BackendEventDetails, EventDetails> {
    override fun transform(input: BackendEventDetails): EventDetails {
        return EventDetails(
            id = input.id,
            images = input.images,
            title = input.title,
            description = input.description,
            time = epochTimeTransformer.transform(input.time),
            locationInfo = input.locationInfo,
            agenda = input.agenda.map {
                AgendaItem(
                    title = it.title,
                    description = it.description,
                    time = epochTimeTransformer.transform(it.time),
                    locationInfo = it.locationInfo
                )
            },
            additionalLinks = input.additionalLinks.map {
                ExternalLink(
                    displayName = it.displayName,
                    url = it.url
                )
            }
        )
    }

}