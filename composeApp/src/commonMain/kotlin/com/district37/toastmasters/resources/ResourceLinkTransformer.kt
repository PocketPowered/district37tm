package com.district37.toastmasters.resources

import com.district37.toastmasters.graphql.ResourcesByTypeQuery
import com.district37.toastmasters.models.ExternalLink
import com.wongislandd.nexus.util.Transformer
import com.wongislandd.nexus.util.safeLet

class ResourceLinkTransformer : Transformer<ResourcesByTypeQuery.Node, ExternalLink> {
    override fun transform(input: ResourcesByTypeQuery.Node): ExternalLink? {
        return safeLet(input.display_name, input.url) { displayName, url ->
            ExternalLink(
                displayName = displayName,
                url = url,
                description = input.description
            )
        }
    }
}
