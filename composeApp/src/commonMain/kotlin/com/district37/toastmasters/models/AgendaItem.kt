package com.district37.toastmasters.models

data class AgendaItem(
    val title: String,
    val description: String,
    val time: TimeRange,
    val locationInfo: String
) 