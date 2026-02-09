package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.OmnisearchQuery
import com.district37.toastmasters.graphql.type.OmnisearchResultType
import com.district37.toastmasters.models.OmnisearchResult
import com.district37.toastmasters.models.OmnisearchResultItem
import com.district37.toastmasters.models.User

/**
 * Transforms GraphQL omnisearch response to domain model
 */
fun OmnisearchQuery.Omnisearch.toOmnisearchResult(): OmnisearchResult {
    val transformedResults = results.mapNotNull { result ->
        when (result.resultType) {
            OmnisearchResultType.EVENT -> {
                result.event?.eventPreview?.toEvent()?.let { OmnisearchResultItem.EventResult(it) }
            }
            OmnisearchResultType.VENUE -> {
                result.venue?.venuePreview?.toVenue()?.let { OmnisearchResultItem.VenueResult(it) }
            }
            OmnisearchResultType.PERFORMER -> {
                result.performer?.performerPreview?.toPerformer()?.let { OmnisearchResultItem.PerformerResult(it) }
            }
            OmnisearchResultType.USER -> {
                result.user?.let {
                    OmnisearchResultItem.UserResult(
                        User(
                            id = it.id,
                            email = "",  // Email not exposed in search results
                            username = it.username,
                            displayName = it.displayName,
                            bio = it.bio,
                            profileImageUrl = it.profileImageUrl,
                            preferences = null,
                            createdAt = null,
                            updatedAt = null
                        )
                    )
                }
            }
            OmnisearchResultType.ORGANIZATION -> {
                result.organization?.organizationPreview?.toOrganization()?.let {
                    OmnisearchResultItem.OrganizationResult(it)
                }
            }
            OmnisearchResultType.UNKNOWN__ -> null
        }
    }

    return OmnisearchResult(
        results = transformedResults,
        totalCount = totalCount,
        hasMore = hasMore
    )
}
