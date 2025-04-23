package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.util.EpochTimeTransformer
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class EventPreviewTransformer(
    private val epochTimeTransformer: EpochTimeTransformer
) : Transformer<BackendEventPreview, EventPreview> {
    override fun transform(input: BackendEventPreview): EventPreview? {
        return safeLet(
            input.image,
            input.title,
            input.time,
            input.locationInfo
        ) { image, title, time, locationInfo ->
            EventPreview(
                id = input.id,
                image = image,
                title = title,
                time = time,
                locationInfo = locationInfo
            )
        }
    }
}