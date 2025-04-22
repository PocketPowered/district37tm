package com.district37.toastmasters.notifications

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Notification(
    val id: Long = 0,
    val header: String,
    val description: String,
    val seen: Boolean = false,
    val timeReceived: Instant = Clock.System.now()
)