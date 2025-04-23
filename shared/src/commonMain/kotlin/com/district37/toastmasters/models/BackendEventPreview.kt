package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendEventPreview(
    val id: Int = -1,
    val image: String = "",
    val title: String = "",
    val time: String = "",
    val locationInfo: String = "",
)
