package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.AcceptCollaborationRequestMutation
import com.district37.toastmasters.graphql.CancelCollaborationRequestMutation
import com.district37.toastmasters.graphql.GetCollaboratorsForEntityQuery
import com.district37.toastmasters.graphql.GetMyIncomingCollaborationRequestsQuery
import com.district37.toastmasters.graphql.GetMyOutgoingCollaborationRequestsQuery
import com.district37.toastmasters.graphql.GetMyPermissionForEntityQuery
import com.district37.toastmasters.graphql.RejectCollaborationRequestMutation
import com.district37.toastmasters.graphql.RemoveCollaboratorMutation
import com.district37.toastmasters.graphql.SendCollaborationRequestMutation
import com.district37.toastmasters.graphql.TransferOwnershipMutation
import com.district37.toastmasters.graphql.UpdateCollaboratorPermissionMutation
import com.district37.toastmasters.models.CollaborationRequest
import com.district37.toastmasters.models.CollaborationRequestConnection
import com.district37.toastmasters.models.Collaborator
import com.district37.toastmasters.models.CollaboratorConnection
import com.district37.toastmasters.util.Resource
import kotlinx.datetime.Instant

/**
 * Repository for collaborator-related operations
 */
class CollaboratorsRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "CollaboratorsRepository"

    // ========== Queries ==========

    /**
     * Get incoming collaboration requests for the current user
     */
    suspend fun getMyIncomingCollaborationRequests(
        first: Int = 20,
        after: String? = null
    ): Resource<CollaborationRequestConnection> {
        return executeQuery(
            queryName = "getMyIncomingCollaborationRequests(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyIncomingCollaborationRequestsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val requests = data.myIncomingCollaborationRequests.edges.map { edge ->
                    CollaborationRequest(
                        id = edge.node.id,
                        senderId = edge.node.senderId,
                        receiverId = edge.node.receiverId,
                        entityType = edge.node.entityType,
                        entityId = edge.node.entityId,
                        entityName = edge.node.entityName,
                        permissionLevel = edge.node.permissionLevel,
                        createdAt = Instant.parse(edge.node.createdAt.toString()),
                        senderDisplayName = edge.node.sender?.userPreview?.displayName,
                        senderProfileImageUrl = edge.node.sender?.userPreview?.profileImageUrl
                    )
                }
                CollaborationRequestConnection(
                    requests = requests,
                    hasNextPage = data.myIncomingCollaborationRequests.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.myIncomingCollaborationRequests.pageInfo.paginationInfo.endCursor,
                    totalCount = data.myIncomingCollaborationRequests.totalCount
                )
            }
        )
    }

    /**
     * Get outgoing collaboration requests for a specific entity (filtered client-side)
     */
    suspend fun getMyOutgoingCollaborationRequestsForEntity(
        entityType: String,
        entityId: Int
    ): Resource<List<CollaborationRequest>> {
        return executeQuery(
            queryName = "getMyOutgoingCollaborationRequestsForEntity(entityType=$entityType, entityId=$entityId)",
            query = {
                apolloClient.query(
                    GetMyOutgoingCollaborationRequestsQuery(
                        first = Optional.present(50), // Fetch enough to find entity-specific requests
                        after = Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.myOutgoingCollaborationRequests.edges
                    .filter { edge ->
                        edge.node.entityType == entityType && edge.node.entityId == entityId
                    }
                    .map { edge ->
                        CollaborationRequest(
                            id = edge.node.id,
                            senderId = edge.node.senderId,
                            receiverId = edge.node.receiverId,
                            entityType = edge.node.entityType,
                            entityId = edge.node.entityId,
                            entityName = edge.node.entityName,
                            permissionLevel = edge.node.permissionLevel,
                            createdAt = Instant.parse(edge.node.createdAt.toString()),
                            receiverDisplayName = edge.node.receiver?.userPreview?.displayName,
                            receiverProfileImageUrl = edge.node.receiver?.userPreview?.profileImageUrl
                        )
                    }
            }
        )
    }

    /**
     * Get collaborators for a specific entity
     */
    suspend fun getCollaboratorsForEntity(
        entityType: String,
        entityId: Int,
        first: Int = 20,
        after: String? = null
    ): Resource<CollaboratorConnection> {
        return executeQuery(
            queryName = "getCollaboratorsForEntity(entityType=$entityType, entityId=$entityId)",
            query = {
                apolloClient.query(
                    GetCollaboratorsForEntityQuery(
                        entityType = entityType,
                        entityId = entityId,
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val collaborators = data.collaboratorsForEntity.edges.map { edge ->
                    Collaborator(
                        id = edge.node.id,
                        entityType = edge.node.entityType,
                        entityId = edge.node.entityId,
                        userId = edge.node.userId,
                        permissionLevel = edge.node.permissionLevel,
                        isOwner = edge.node.isOwner,
                        canManageCollaborators = edge.node.canManageCollaborators,
                        createdAt = Instant.parse(edge.node.createdAt.toString()),
                        displayName = edge.node.user?.userPreview?.displayName,
                        profileImageUrl = edge.node.user?.userPreview?.profileImageUrl
                    )
                }
                CollaboratorConnection(
                    collaborators = collaborators,
                    hasNextPage = data.collaboratorsForEntity.pageInfo.paginationInfo.hasNextPage,
                    endCursor = data.collaboratorsForEntity.pageInfo.paginationInfo.endCursor,
                    totalCount = data.collaboratorsForEntity.totalCount
                )
            }
        )
    }

    /**
     * Get current user's permission for a specific entity
     */
    suspend fun getMyPermissionForEntity(
        entityType: String,
        entityId: Int
    ): Resource<Collaborator?> {
        return executeQuery(
            queryName = "getMyPermissionForEntity(entityType=$entityType, entityId=$entityId)",
            query = {
                apolloClient.query(
                    GetMyPermissionForEntityQuery(
                        entityType = entityType,
                        entityId = entityId
                    )
                ).execute()
            },
            transform = { data ->
                data.myPermissionForEntity?.let { perm ->
                    Collaborator(
                        id = perm.id,
                        entityType = perm.entityType,
                        entityId = perm.entityId,
                        userId = perm.userId,
                        permissionLevel = perm.permissionLevel,
                        isOwner = perm.isOwner,
                        canManageCollaborators = perm.canManageCollaborators,
                        createdAt = Instant.parse(perm.createdAt.toString()),
                        displayName = null,
                        profileImageUrl = null
                    )
                }
            }
        )
    }

    // ========== Mutations ==========

    /**
     * Send a collaboration request
     */
    suspend fun sendCollaborationRequest(
        receiverId: String,
        entityType: String,
        entityId: Int,
        permissionLevel: String
    ): Resource<CollaborationRequest> {
        return executeMutation(
            mutationName = "sendCollaborationRequest",
            mutation = {
                apolloClient.mutation(
                    SendCollaborationRequestMutation(
                        receiverId = receiverId,
                        entityType = entityType,
                        entityId = entityId,
                        permissionLevel = permissionLevel
                    )
                ).execute()
            },
            transform = { data ->
                val request = data.sendCollaborationRequest
                CollaborationRequest(
                    id = request.id,
                    senderId = request.senderId,
                    receiverId = request.receiverId,
                    entityType = request.entityType,
                    entityId = request.entityId,
                    entityName = request.entityName,
                    permissionLevel = request.permissionLevel,
                    createdAt = Instant.parse(request.createdAt.toString())
                )
            }
        )
    }

    /**
     * Cancel a pending collaboration request
     */
    suspend fun cancelCollaborationRequest(requestId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "cancelCollaborationRequest",
            mutation = {
                apolloClient.mutation(CancelCollaborationRequestMutation(requestId = requestId)).execute()
            },
            transform = { data -> data.cancelCollaborationRequest }
        )
    }

    /**
     * Accept an incoming collaboration request
     */
    suspend fun acceptCollaborationRequest(requestId: Int): Resource<Collaborator> {
        return executeMutation(
            mutationName = "acceptCollaborationRequest",
            mutation = {
                apolloClient.mutation(AcceptCollaborationRequestMutation(requestId = requestId)).execute()
            },
            transform = { data ->
                val collab = data.acceptCollaborationRequest
                Collaborator(
                    id = collab.id,
                    entityType = collab.entityType,
                    entityId = collab.entityId,
                    userId = collab.userId,
                    permissionLevel = collab.permissionLevel,
                    isOwner = collab.isOwner,
                    canManageCollaborators = collab.canManageCollaborators,
                    createdAt = Instant.parse(collab.createdAt.toString()),
                    displayName = collab.user?.userPreview?.displayName,
                    profileImageUrl = collab.user?.userPreview?.profileImageUrl
                )
            }
        )
    }

    /**
     * Reject an incoming collaboration request
     */
    suspend fun rejectCollaborationRequest(requestId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "rejectCollaborationRequest",
            mutation = {
                apolloClient.mutation(RejectCollaborationRequestMutation(requestId = requestId)).execute()
            },
            transform = { data -> data.rejectCollaborationRequest }
        )
    }

    /**
     * Update a collaborator's permission level
     */
    suspend fun updateCollaboratorPermission(
        collaboratorId: String,
        newPermissionLevel: String
    ): Resource<Collaborator> {
        return executeMutation(
            mutationName = "updateCollaboratorPermission",
            mutation = {
                apolloClient.mutation(
                    UpdateCollaboratorPermissionMutation(
                        collaboratorId = collaboratorId,
                        newPermissionLevel = newPermissionLevel
                    )
                ).execute()
            },
            transform = { data ->
                val collab = data.updateCollaboratorPermission
                Collaborator(
                    id = collab.id,
                    entityType = collab.entityType,
                    entityId = collab.entityId,
                    userId = collab.userId,
                    permissionLevel = collab.permissionLevel,
                    isOwner = collab.isOwner,
                    canManageCollaborators = collab.canManageCollaborators,
                    createdAt = Instant.parse(collab.createdAt.toString()),
                    displayName = collab.user?.userPreview?.displayName,
                    profileImageUrl = collab.user?.userPreview?.profileImageUrl
                )
            }
        )
    }

    /**
     * Remove a collaborator from an entity
     */
    suspend fun removeCollaborator(collaboratorId: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeCollaborator",
            mutation = {
                apolloClient.mutation(RemoveCollaboratorMutation(collaboratorId = collaboratorId)).execute()
            },
            transform = { data -> data.removeCollaborator }
        )
    }

    /**
     * Transfer ownership to another collaborator
     */
    suspend fun transferOwnership(
        entityType: String,
        entityId: Int,
        newOwnerId: String
    ): Resource<Boolean> {
        return executeMutation(
            mutationName = "transferOwnership",
            mutation = {
                apolloClient.mutation(
                    TransferOwnershipMutation(
                        entityType = entityType,
                        entityId = entityId,
                        newOwnerId = newOwnerId
                    )
                ).execute()
            },
            transform = { data -> data.transferOwnership }
        )
    }
}
