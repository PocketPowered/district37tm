package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.util.EpochTimeTransformer
import com.wongislandd.nexus.util.Transformer

class EventPreviewTransformer(
    private val epochTimeTransformer: EpochTimeTransformer
): Transformer<BackendEventPreview, EventPreview> {
    override fun transform(input: BackendEventPreview): EventPreview {
        return EventPreview(
            id = input.id,
            image = input.image,
            title = input.title,
            time = input.time,
            locationInfo = input.locationInfo
        )
    }
}