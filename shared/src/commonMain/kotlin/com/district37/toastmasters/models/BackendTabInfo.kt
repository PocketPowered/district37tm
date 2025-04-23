package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendTabInfo(
    val displayName: String = "",
    val dateKey: String = "",
)