package com.district37.toastmasters.resources

import com.district37.toastmasters.models.ExternalLink

data class CategorizedResources(
    val resourceType: String,
    val title: String,
    val resources: List<ExternalLink>
)
