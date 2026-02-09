package com.district37.toastmasters.models

/**
 * Represents permissions for an entity (Event, Venue, Performer)
 */
data class EntityPermissions(
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canManageCollaborators: Boolean,
    val role: PermissionRole
) {
    companion object {
        val NONE = EntityPermissions(
            canEdit = false,
            canDelete = false,
            canManageCollaborators = false,
            role = PermissionRole.NONE
        )
    }
}

/**
 * Permission role for an entity
 */
enum class PermissionRole {
    NONE,
    OWNER,
    EDITOR,
    VIEWER
}
