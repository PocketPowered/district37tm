package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * A member of an organization with their role
 */
data class OrganizationMember(
    val id: Int,
    val organizationId: Int,
    val userId: String,
    val role: OrganizationRole,
    val createdAt: Instant? = null,
    val userProfile: User? = null
)
