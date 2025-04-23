package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.EventDetails
import com.district37.toastmasters.models.ExternalLink
import com.district37.toastmasters.models.TimeRange
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventDetailsTransformer : Transformer<BackendEventDetails, EventDetails> {
    override fun transform(input: BackendEventDetails): EventDetails? {
        return safeLet(input.title, input.description, input.time,
            input.locationInfo
        ) { title, description, time, locationInfo ->
            EventDetails(
                id = input.id,
                images = input.images,
                title = title,
                description = description,
                time = TimeRange(
                    time
                        .startTime,
                    time.endTime
                ),
                locationInfo = locationInfo,
                agenda = input.agenda?.mapNotNull {
                    safeLet(
                        it.title,
                        it.description,
                        it.time,
                        it.locationInfo
                    ) { title, description, time, locationInfo ->
                        AgendaItem(
                            title = title,
                            description = description,
                            time = TimeRange(
                                time
                                    .startTime,
                                time.endTime
                            ),
                            locationInfo = locationInfo
                        )
                    }
                } ?: emptyList(),
                additionalLinks = input.additionalLinks?.mapNotNull {
                    safeLet(
                        it.displayName,
                        it.url
                    ) { displayName, url ->
                        ExternalLink(
                            displayName = displayName,
                            url = url
                        )
                    }
                } ?: emptyList()
            )
        }
    }

}