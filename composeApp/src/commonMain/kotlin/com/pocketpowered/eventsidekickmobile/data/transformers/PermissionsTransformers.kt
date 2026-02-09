package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.EntityPermissionsDetails
import com.district37.toastmasters.models.EntityPermissions
import com.district37.toastmasters.models.PermissionRole

/**
 * Transforms GraphQL EntityPermissionsDetails fragment to domain EntityPermissions model
 */
fun EntityPermissionsDetails.toEntityPermissions(): EntityPermissions {
    return EntityPermissions(
        canEdit = canEdit,
        canDelete = canDelete,
        canManageCollaborators = canManageCollaborators,
        role = role.toPermissionRole()
    )
}

/**
 * Transforms GraphQL PermissionRole to domain PermissionRole
 */
fun com.district37.toastmasters.graphql.type.PermissionRole.toPermissionRole(): PermissionRole {
    return when (this) {
        com.district37.toastmasters.graphql.type.PermissionRole.OWNER -> PermissionRole.OWNER
        com.district37.toastmasters.graphql.type.PermissionRole.EDITOR -> PermissionRole.EDITOR
        com.district37.toastmasters.graphql.type.PermissionRole.VIEWER -> PermissionRole.VIEWER
        else -> PermissionRole.NONE
    }
}
