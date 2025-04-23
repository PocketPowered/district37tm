package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendAgendaItem(
    val title: String? = null,
    val description: String? = null,
    val time: TimeRange? = null,
    val locationInfo: String? = null
)
