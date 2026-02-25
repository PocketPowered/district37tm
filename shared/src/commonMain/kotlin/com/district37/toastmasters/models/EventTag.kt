package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventTag {
    NORMAL,
    HIGHLIGHTED,
    BREAK
}
