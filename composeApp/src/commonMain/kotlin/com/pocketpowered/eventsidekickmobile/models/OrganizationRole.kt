package com.district37.toastmasters.models

/**
 * Role within an organization with tiered permissions.
 * OWNER > ADMIN > MEMBER
 */
enum class OrganizationRole {
    OWNER,
    ADMIN,
    MEMBER;

    /**
     * Check if this role can manage members (add/remove/update roles)
     */
    fun canManageMembers(): Boolean = this == OWNER || this == ADMIN

    /**
     * Check if this role can manage organization settings
     */
    fun canManageSettings(): Boolean = this == OWNER || this == ADMIN

    /**
     * Check if this role can delete the organization
     */
    fun canDeleteOrganization(): Boolean = this == OWNER

    /**
     * Check if this role can tag events to this organization
     */
    fun canTagEvents(): Boolean = this == OWNER || this == ADMIN

    /**
     * Check if this role can promote members to the given role
     */
    fun canPromoteTo(role: OrganizationRole): Boolean = when (this) {
        OWNER -> true
        ADMIN -> role == MEMBER
        MEMBER -> false
    }

    companion object {
        fun fromString(value: String): OrganizationRole = when (value.uppercase()) {
            "OWNER" -> OWNER
            "ADMIN" -> ADMIN
            "MEMBER" -> MEMBER
            else -> MEMBER
        }
    }
}
