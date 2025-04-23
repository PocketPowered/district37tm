package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.EventDetails
import com.district37.toastmasters.models.ExternalLink
import com.district37.toastmasters.util.EpochTimeTransformer
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventDetailsTransformer(
    private val epochTimeTransformer: EpochTimeTransformer
) : Transformer<BackendEventDetails, EventDetails> {
    override fun transform(input: BackendEventDetails): EventDetails? {
        return safeLet(
            input.images, input.title, input.description, input.time,
            input.locationInfo
        ) { images, title, description, time, locationInfo ->
            EventDetails(
                id = input.id,
                images = images,
                title = title,
                description = description,
                time = time,
                locationInfo = locationInfo,
                agenda = input.agenda?.map {
                    AgendaItem(
                        title = it.title,
                        description = it.description,
                        time = it.time,
                        locationInfo = it.locationInfo
                    )
                } ?: emptyList(),
                additionalLinks = input.additionalLinks?.map {
                    ExternalLink(
                        displayName = it.displayName,
                        url = it.url
                    )
                } ?: emptyList()
            )
        }
    }

}