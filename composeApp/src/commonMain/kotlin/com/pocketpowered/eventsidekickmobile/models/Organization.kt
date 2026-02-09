package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Domain model for Organization.
 * Organizations can host events and have members with tiered roles.
 */
data class Organization(
    val id: Int,
    val slug: String? = null,
    val name: String,
    val tag: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val primaryColor: String? = null,
    val secondaryColor: String? = null,
    val createdAt: Instant? = null,
    val images: List<Image> = emptyList(),
    val events: PagedField<Event> = PagedField(),
    val members: PagedField<OrganizationMember> = PagedField(),
    val userEngagement: UserEngagement? = null,
    override val permissions: EntityPermissions? = null
) : HasPermissions
