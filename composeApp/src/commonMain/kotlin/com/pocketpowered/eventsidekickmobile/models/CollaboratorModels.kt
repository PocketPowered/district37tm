package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Represents a pending collaboration request
 */
data class CollaborationRequest(
    val id: Int,
    val senderId: String,
    val receiverId: String,
    val entityType: String,
    val entityId: Int,
    val entityName: String?,
    val permissionLevel: String,
    val createdAt: Instant,
    val senderDisplayName: String? = null,
    val senderProfileImageUrl: String? = null,
    val receiverDisplayName: String? = null,
    val receiverProfileImageUrl: String? = null
) {
    /**
     * Human-readable entity type (Event, Venue, Performer)
     */
    val entityTypeDisplay: String
        get() = entityType.replaceFirstChar { it.uppercase() }

    /**
     * Human-readable permission level (Editor, Admin)
     */
    val permissionLevelDisplay: String
        get() = permissionLevel.replaceFirstChar { it.uppercase() }
}

/**
 * Connection wrapper for paginated collaboration requests
 */
data class CollaborationRequestConnection(
    val requests: List<CollaborationRequest>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
) {
    companion object {
        val EMPTY = CollaborationRequestConnection(
            requests = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}

/**
 * Represents a user who has been granted permission to collaborate on an entity
 */
data class Collaborator(
    val id: String,
    val entityType: String,
    val entityId: Int,
    val userId: String,
    val permissionLevel: String,
    val isOwner: Boolean,
    val canManageCollaborators: Boolean,
    val createdAt: Instant,
    val displayName: String? = null,
    val profileImageUrl: String? = null
) {
    /**
     * Human-readable permission level (Owner, Admin, Editor)
     */
    val permissionLevelDisplay: String
        get() = permissionLevel.replaceFirstChar { it.uppercase() }
}

/**
 * Connection wrapper for paginated collaborators
 */
data class CollaboratorConnection(
    val collaborators: List<Collaborator>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
) {
    companion object {
        val EMPTY = CollaboratorConnection(
            collaborators = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )
    }
}

/**
 * Permission levels for collaborators
 */
enum class PermissionLevel(val value: String, val displayName: String) {
    EDITOR("editor", "Editor"),
    ADMIN("admin", "Admin");

    companion object {
        fun fromValue(value: String): PermissionLevel? =
            entries.find { it.value == value }
    }
}
