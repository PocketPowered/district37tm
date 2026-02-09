package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.OrganizationDetails
import com.district37.toastmasters.graphql.fragment.OrganizationMemberDetails
import com.district37.toastmasters.graphql.fragment.OrganizationMemberInvitationDetails
import com.district37.toastmasters.graphql.fragment.OrganizationPreview
import com.district37.toastmasters.graphql.type.OrganizationRole as GraphQLOrganizationRole
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.models.OrganizationMember
import com.district37.toastmasters.models.OrganizationMemberInvitation
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.models.PagedField
import com.district37.toastmasters.models.User

/**
 * Transforms GraphQL OrganizationRole to domain OrganizationRole model
 */
fun GraphQLOrganizationRole.toOrganizationRole(): OrganizationRole {
    return when (this) {
        GraphQLOrganizationRole.OWNER -> OrganizationRole.OWNER
        GraphQLOrganizationRole.ADMIN -> OrganizationRole.ADMIN
        GraphQLOrganizationRole.MEMBER -> OrganizationRole.MEMBER
        GraphQLOrganizationRole.UNKNOWN__ -> OrganizationRole.MEMBER
    }
}

/**
 * Transforms GraphQL OrganizationMemberDetails fragment to domain OrganizationMember model
 */
fun OrganizationMemberDetails.toOrganizationMember(): OrganizationMember {
    return OrganizationMember(
        id = id,
        organizationId = organizationId,
        userId = userId,
        role = role.toOrganizationRole(),
        createdAt = createdAt,
        userProfile = userProfile?.userPreview?.let { profile ->
            User(
                id = profile.id,
                email = "", // Not available in preview
                displayName = profile.displayName,
                profileImageUrl = profile.profileImageUrl
            )
        }
    )
}

/**
 * Transforms GraphQL OrganizationPreview fragment to domain Organization model
 */
fun OrganizationPreview.toOrganization(): Organization {
    return Organization(
        id = id,
        name = name,
        tag = tag,
        logoUrl = logoUrl,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        description = null,
        website = null,
        email = null,
        phone = null,
        createdAt = null,
        images = emptyList(),
        events = PagedField(),
        members = PagedField()
    )
}

/**
 * Transforms GraphQL OrganizationDetails fragment to domain Organization model
 */
fun OrganizationDetails.toOrganization(): Organization {
    return Organization(
        id = id,
        name = name,
        tag = tag,
        description = description,
        logoUrl = logoUrl,
        website = website,
        email = email,
        phone = phone,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        createdAt = createdAt,
        images = imagesConnection.edges.map { it.node.imageDetails.toImage() },
        events = PagedField(
            items = eventsConnection.edges.map { it.node.eventPreview.toEvent() },
            hasMore = eventsConnection.pageInfo.paginationInfo.hasNextPage,
            cursor = eventsConnection.pageInfo.paginationInfo.endCursor
        ),
        members = PagedField(
            items = membersConnection.edges.map { it.node.organizationMemberDetails.toOrganizationMember() },
            hasMore = membersConnection.pageInfo.paginationInfo.hasNextPage,
            cursor = membersConnection.pageInfo.paginationInfo.endCursor
        ),
        userEngagement = userEngagement?.userEngagementDetails?.toUserEngagement(),
        permissions = permissions?.entityPermissionsDetails?.toEntityPermissions()
    )
}

/**
 * Transforms GraphQL OrganizationMemberInvitationDetails fragment to domain model
 */
fun OrganizationMemberInvitationDetails.toOrganizationMemberInvitation(): OrganizationMemberInvitation {
    return OrganizationMemberInvitation(
        id = id,
        organizationId = organizationId,
        senderId = senderId,
        receiverId = receiverId,
        role = role.toOrganizationRole(),
        createdAt = createdAt,
        organization = organization?.organizationPreview?.toOrganization(),
        sender = sender?.userPreview?.let { profile ->
            User(
                id = profile.id,
                email = "",
                displayName = profile.displayName,
                profileImageUrl = profile.profileImageUrl
            )
        },
        receiver = receiver?.userPreview?.let { profile ->
            User(
                id = profile.id,
                email = "",
                displayName = profile.displayName,
                profileImageUrl = profile.profileImageUrl
            )
        }
    )
}
