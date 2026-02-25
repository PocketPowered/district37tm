package com.district37.toastmasters.eventlist

import com.district37.toastmasters.graphql.EventPreviewsByDateQuery
import com.district37.toastmasters.models.EventTag
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.TimeRange
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventPreviewTransformer : Transformer<EventPreviewsByDateQuery.Node, EventPreview> {
    private fun toEventTag(value: String?): EventTag {
        return when (value) {
            EventTag.HIGHLIGHTED.name -> EventTag.HIGHLIGHTED
            EventTag.BREAK.name -> EventTag.BREAK
            else -> EventTag.NORMAL
        }
    }

    override fun transform(input: EventPreviewsByDateQuery.Node): EventPreview? {
        return safeLet(
            input.title,
            input.start_time,
            input.end_time,
            input.location_info
        ) { title, startTime, endTime, locationInfo ->
            EventPreview(
                id = input.id.toInt(),
                primaryImage = input.images.firstOrNull(),
                title = title,
                time = TimeRange(
                    startTime = startTime,
                    endTime = endTime
                ),
                locationInfo = locationInfo,
                tag = toEventTag(input.tag)
            )
        }
    }
}
