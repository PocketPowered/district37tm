package com.district37.toastmasters.eventdetails

import com.district37.toastmasters.graphql.EventDetailsQuery
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.EventDetails
import com.district37.toastmasters.models.ExternalLink
import com.district37.toastmasters.models.TimeRange
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventDetailsTransformer : Transformer<EventDetailsQuery.Node, EventDetails> {
    private fun parseAgenda(raw: Any): List<AgendaItem> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val title = map["title"] as? String ?: return@mapNotNull null
            val description = map["description"] as? String ?: return@mapNotNull null
            val locationInfo = map["locationInfo"] as? String ?: return@mapNotNull null
            val timeMap = map["time"] as? Map<*, *> ?: return@mapNotNull null
            val startTime = (timeMap["startTime"] as? Number)?.toLong() ?: return@mapNotNull null
            val endTime = (timeMap["endTime"] as? Number)?.toLong() ?: return@mapNotNull null

            AgendaItem(
                title = title,
                description = description,
                time = TimeRange(startTime, endTime),
                locationInfo = locationInfo
            )
        }
    }

    private fun parseLinks(raw: Any): List<ExternalLink> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val displayName = map["displayName"] as? String ?: return@mapNotNull null
            val url = map["url"] as? String ?: return@mapNotNull null
            val description = map["description"] as? String

            ExternalLink(
                displayName = displayName,
                url = url,
                description = description
            )
        }
    }

    override fun transform(input: EventDetailsQuery.Node): EventDetails? {
        return safeLet(input.title, input.description, input.location_info, input.start_time, input.end_time
        ) { title, description, locationInfo, startTime, endTime ->
            EventDetails(
                id = input.id.toInt(),
                images = input.imagesFilterNotNull(),
                title = title,
                description = description,
                time = TimeRange(
                    startTime,
                    endTime
                ),
                locationInfo = locationInfo,
                agenda = parseAgenda(input.agenda),
                additionalLinks = parseLinks(input.additional_links)
            )
        }
    }

}
