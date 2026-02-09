package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.UserEngagementDetails
import com.district37.toastmasters.models.UserEngagement

/**
 * Transforms GraphQL UserEngagementDetails fragment to domain UserEngagement model
 */
fun UserEngagementDetails.toUserEngagement(): UserEngagement {
    return UserEngagement(
        isSubscribed = isSubscribed,
        status = status
    )
}
