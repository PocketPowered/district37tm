package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * A pending invitation for a user to join an organization
 */
data class OrganizationMemberInvitation(
    val id: Int,
    val organizationId: Int,
    val senderId: String,
    val receiverId: String,
    val role: OrganizationRole,
    val createdAt: Instant? = null,
    // Resolved relationships
    val organization: Organization? = null,
    val sender: User? = null,
    val receiver: User? = null
)
