package com.district37.toastmasters.resources

import com.district37.toastmasters.models.ExternalLink
import com.wongislandd.nexus.util.safeLet

class ResourceLinkTransformer {
    fun transform(displayName: String?, url: String?, description: String?): ExternalLink? {
        return safeLet(displayName, url) { safeDisplayName, safeUrl ->
            ExternalLink(
                displayName = safeDisplayName,
                url = safeUrl,
                description = description
            )
        }
    }
}
