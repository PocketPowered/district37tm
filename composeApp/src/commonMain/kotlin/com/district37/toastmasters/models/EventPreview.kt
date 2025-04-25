package com.district37.toastmasters.models

data class EventPreview(
    val id: Int,
    val primaryImage: String?,
    val title: String,
    val time: TimeRange,
    val locationInfo: String,
    val isFavorited: Boolean = false,
    val tag: EventTag = EventTag.NORMAL
) 