package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class TimeRange(
    val startTime: Long,
    val endTime: Long
) 