package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendEventPreview(
    val id: Int = -1,
    val image: String? = null,
    val title: String? = null,
    val time: BackendTimeRange? = null,
    val locationInfo: String? = null,
    val tag: EventTag = EventTag.NORMAL
)
