package com.district37.toastmasters.models

import kotlinx.serialization.Serializable

@Serializable
data class BackendExternalLink(
    val displayName: String? = null,
    val url: String? = null
)