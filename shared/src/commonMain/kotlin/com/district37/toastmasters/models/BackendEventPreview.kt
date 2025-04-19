package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendEventPreview(
    val id: Int,
    val image: String,
    val title: String,
    val time: String,
    val isDayOne: Boolean,
    val locationInfo: String,
)
