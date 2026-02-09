package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.AcceptOrganizationMemberInvitationMutation
import com.district37.toastmasters.graphql.CancelOrganizationMemberInvitationMutation
import com.district37.toastmasters.graphql.GetMyOrganizationInvitationsQuery
import com.district37.toastmasters.graphql.GetOrganizationMemberInvitationsQuery
import com.district37.toastmasters.graphql.RejectOrganizationMemberInvitationMutation
import com.district37.toastmasters.graphql.SendOrganizationMemberInvitationMutation
import com.district37.toastmasters.graphql.type.OrganizationRole as GraphQLOrganizationRole
import com.district37.toastmasters.graphql.type.SendOrganizationMemberInvitationInput
import com.district37.toastmasters.data.transformers.toOrganizationMember
import com.district37.toastmasters.data.transformers.toOrganizationMemberInvitation
import com.district37.toastmasters.models.OrganizationMember
import com.district37.toastmasters.models.OrganizationMemberInvitation
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for organization member invitations.
 */
class OrganizationMemberInvitationRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "OrganizationMemberInvitationRepository"

    /**
     * Get pending invitations for an organization (for admins/owners)
     */
    suspend fun getInvitationsForOrganization(
        organizationId: Int,
        first: Int = 20,
        after: String? = null
    ): Resource<PagedConnection<OrganizationMemberInvitation>> {
        return executeQuery(
            queryName = "getInvitationsForOrganization(orgId=$organizationId)",
            query = {
                apolloClient.query(
                    GetOrganizationMemberInvitationsQuery(
                        organizationId = organizationId,
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                data.organizationMemberInvitations.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.organizationMemberInvitationDetails.toOrganizationMemberInvitation()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor,
                        totalCount = connection.totalCount
                    )
                }
            }
        )
    }

    /**
     * Get current user's incoming organization invitations
     */
    suspend fun getMyIncomingInvitations(
        first: Int = 20,
        after: String? = null
    ): Resource<PagedConnection<OrganizationMemberInvitation>> {
        return executeQuery(
            queryName = "getMyIncomingInvitations",
            query = {
                apolloClient.query(
                    GetMyOrganizationInvitationsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                data.myOrganizationInvitations.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.organizationMemberInvitationDetails.toOrganizationMemberInvitation()
                        },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor,
                        totalCount = connection.totalCount
                    )
                }
            }
        )
    }

    /**
     * Send an invitation to a user to join an organization
     */
    suspend fun sendInvitation(
        organizationId: Int,
        receiverId: String,
        role: OrganizationRole = OrganizationRole.MEMBER
    ): Resource<OrganizationMemberInvitation> {
        val input = SendOrganizationMemberInvitationInput(
            organizationId = organizationId,
            receiverId = receiverId,
            role = role.toGraphQL()
        )

        return executeMutation(
            mutationName = "sendInvitation(orgId=$organizationId, receiver=$receiverId)",
            mutation = {
                apolloClient.mutation(
                    SendOrganizationMemberInvitationMutation(input = input)
                ).execute()
            },
            transform = { data ->
                data.sendOrganizationMemberInvitation.organizationMemberInvitationDetails.toOrganizationMemberInvitation()
            }
        )
    }

    /**
     * Accept an organization invitation
     */
    suspend fun acceptInvitation(invitationId: Int): Resource<OrganizationMember> {
        return executeMutation(
            mutationName = "acceptInvitation(id=$invitationId)",
            mutation = {
                apolloClient.mutation(
                    AcceptOrganizationMemberInvitationMutation(invitationId = invitationId)
                ).execute()
            },
            transform = { data ->
                data.acceptOrganizationMemberInvitation.organizationMemberDetails.toOrganizationMember()
            }
        )
    }

    /**
     * Reject an organization invitation
     */
    suspend fun rejectInvitation(invitationId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "rejectInvitation(id=$invitationId)",
            mutation = {
                apolloClient.mutation(
                    RejectOrganizationMemberInvitationMutation(invitationId = invitationId)
                ).execute()
            },
            transform = { data ->
                data.rejectOrganizationMemberInvitation
            }
        )
    }

    /**
     * Cancel a sent invitation
     */
    suspend fun cancelInvitation(invitationId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "cancelInvitation(id=$invitationId)",
            mutation = {
                apolloClient.mutation(
                    CancelOrganizationMemberInvitationMutation(invitationId = invitationId)
                ).execute()
            },
            transform = { data ->
                data.cancelOrganizationMemberInvitation
            }
        )
    }
}

/**
 * Extension to convert domain role to GraphQL role
 */
private fun OrganizationRole.toGraphQL(): GraphQLOrganizationRole {
    return when (this) {
        OrganizationRole.OWNER -> GraphQLOrganizationRole.OWNER
        OrganizationRole.ADMIN -> GraphQLOrganizationRole.ADMIN
        OrganizationRole.MEMBER -> GraphQLOrganizationRole.MEMBER
    }
}
