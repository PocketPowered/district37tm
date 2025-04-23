package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.TimeRange
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventPreviewTransformer : Transformer<BackendEventPreview, EventPreview> {
    override fun transform(input: BackendEventPreview): EventPreview? {
        return safeLet(
            input.title,
            input.time,
            input.locationInfo
        ) { title, time, locationInfo ->
            EventPreview(
                id = input.id,
                primaryImage = input.image,
                title = title,
                time = TimeRange(
                    startTime = time.startTime,
                    endTime = time.endTime
                ),
                locationInfo = locationInfo
            )
        }
    }
}