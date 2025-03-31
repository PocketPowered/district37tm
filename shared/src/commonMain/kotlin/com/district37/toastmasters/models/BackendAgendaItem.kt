package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendAgendaItem(
    val title: String,
    val description: String,
    val time: Long,
    val locationInfo: String
)
