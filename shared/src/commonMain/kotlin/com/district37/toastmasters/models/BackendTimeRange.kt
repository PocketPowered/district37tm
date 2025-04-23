package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendTimeRange(
    val startTime: Long = -1,
    val endTime: Long = -1
) 