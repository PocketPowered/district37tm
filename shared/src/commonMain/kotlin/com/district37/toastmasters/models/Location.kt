package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: String = "",
    val locationName: String,
    val locationImages: List<String> = emptyList()
) 