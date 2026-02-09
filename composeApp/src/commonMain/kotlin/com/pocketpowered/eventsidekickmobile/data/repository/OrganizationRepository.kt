package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.*
import com.district37.toastmasters.graphql.type.CreateOrganizationInput
import com.district37.toastmasters.graphql.type.UpdateOrganizationInput
import com.district37.toastmasters.graphql.type.AddOrganizationMemberInput
import com.district37.toastmasters.graphql.type.UpdateOrganizationMemberInput
import com.district37.toastmasters.graphql.type.OrganizationRole as GraphQLOrganizationRole
import com.district37.toastmasters.data.transformers.toOrganization
import com.district37.toastmasters.data.transformers.toOrganizationMember
import com.district37.toastmasters.data.transformers.toOrganizationRole
import com.district37.toastmasters.data.repository.interfaces.BaseDetailRepository
import com.district37.toastmasters.data.repository.interfaces.BasePreviewRepository
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.models.OrganizationMember
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for Organization data.
 * Handles CRUD operations and member management for organizations.
 */
class OrganizationRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient),
    BaseDetailRepository<Organization>,
    BasePreviewRepository<Organization> {

    override val tag = "OrganizationRepository"

    /**
     * Get a single organization by ID with full details
     */
    suspend fun getOrganization(id: Int): Resource<Organization> {
        return executeQuery(
            queryName = "getOrganization(id=$id)",
            query = { apolloClient.query(GetOrganizationQuery(id = id)).execute() },
            transform = { data -> data.organization?.organizationDetails?.toOrganization() }
        )
    }

    /**
     * BaseDetailRepository implementation
     */
    override suspend fun getDetails(id: Int): Resource<Organization> = getOrganization(id)

    override suspend fun getPreview(id: Int): Resource<Organization> = getOrganization(id)

    /**
     * Search organizations with pagination
     */
    suspend fun searchOrganizations(
        searchQuery: String? = null,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Organization>> {
        return executeQuery(
            queryName = "searchOrganizations(query=$searchQuery, cursor=$cursor)",
            query = {
                apolloClient.query(
                    SearchOrganizationsQuery(
                        first = Optional.present(first),
                        after = Optional.presentIfNotNull(cursor),
                        searchQuery = Optional.presentIfNotNull(searchQuery)
                    )
                ).execute()
            },
            transform = { data ->
                data.organizationsConnection.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { edge ->
                            edge.node.organizationPreview.toOrganization()
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
     * Get current user's role in an organization
     */
    suspend fun getMyRoleInOrganization(organizationId: Int): Resource<OrganizationRole?> {
        return executeQuery(
            queryName = "getMyRoleInOrganization(orgId=$organizationId)",
            query = { apolloClient.query(GetMyRoleInOrganizationQuery(organizationId = organizationId)).execute() },
            transform = { data -> data.myRoleInOrganization?.toOrganizationRole() }
        )
    }

    /**
     * Check if current user can manage events for an organization
     */
    suspend fun canManageOrganizationEvents(organizationId: Int): Resource<Boolean> {
        return executeQuery(
            queryName = "canManageOrganizationEvents(orgId=$organizationId)",
            query = { apolloClient.query(CanManageOrganizationEventsQuery(organizationId = organizationId)).execute() },
            transform = { data -> data.canManageOrganizationEvents }
        )
    }

    /**
     * Get organization events with pagination (for View All events screen)
     */
    suspend fun getOrganizationEvents(
        organizationId: Int,
        cursor: String? = null,
        first: Int = 20
    ): Resource<PagedConnection<Event>> {
        return executeQuery(
            queryName = "getOrganizationEvents(orgId=$organizationId, cursor=$cursor)",
            query = {
                apolloClient.query(
                    GetMoreOrganizationEventsQuery(
                        id = organizationId,
                        first = Optional.present(first),
                        after = if (cursor != null) Optional.present(cursor) else Optional.absent()
                    )
                ).execute()
            },
            transform = { data ->
                data.organization?.eventsConnection?.let { connection ->
                    PagedConnection(
                        items = connection.edges.map { it.node.eventPreview.toEvent() },
                        hasNextPage = connection.pageInfo.paginationInfo.hasNextPage,
                        endCursor = connection.pageInfo.paginationInfo.endCursor
                    )
                }
            }
        )
    }

    /**
     * Create a new organization
     */
    suspend fun createOrganization(input: CreateOrganizationInput): Resource<Organization> {
        return executeMutation(
            mutationName = "createOrganization(name=${input.name})",
            mutation = {
                apolloClient.mutation(CreateOrganizationMutation(input = input)).execute()
            },
            transform = { data ->
                data.createOrganization.organizationDetails.toOrganization()
            }
        )
    }

    /**
     * Update an existing organization
     */
    suspend fun updateOrganization(id: Int, input: UpdateOrganizationInput): Resource<Organization> {
        return executeMutation(
            mutationName = "updateOrganization(id=$id)",
            mutation = {
                apolloClient.mutation(UpdateOrganizationMutation(id = id, input = input)).execute()
            },
            transform = { data ->
                data.updateOrganization.organizationDetails.toOrganization()
            }
        )
    }

    /**
     * Delete an organization
     */
    suspend fun deleteOrganization(id: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "deleteOrganization(id=$id)",
            mutation = {
                apolloClient.mutation(DeleteOrganizationMutation(id = id)).execute()
            },
            transform = { data ->
                data.deleteOrganization
            }
        )
    }

    // ===== Member Management =====

    /**
     * Add a member to an organization
     */
    suspend fun addOrganizationMember(input: AddOrganizationMemberInput): Resource<OrganizationMember> {
        return executeMutation(
            mutationName = "addOrganizationMember(orgId=${input.organizationId}, userId=${input.userId})",
            mutation = {
                apolloClient.mutation(AddOrganizationMemberMutation(input = input)).execute()
            },
            transform = { data ->
                data.addOrganizationMember.organizationMemberDetails.toOrganizationMember()
            }
        )
    }

    /**
     * Update a member's role
     */
    suspend fun updateOrganizationMemberRole(input: UpdateOrganizationMemberInput): Resource<OrganizationMember> {
        return executeMutation(
            mutationName = "updateOrganizationMemberRole(orgId=${input.organizationId}, userId=${input.userId})",
            mutation = {
                apolloClient.mutation(UpdateOrganizationMemberRoleMutation(input = input)).execute()
            },
            transform = { data ->
                data.updateOrganizationMemberRole.organizationMemberDetails.toOrganizationMember()
            }
        )
    }

    /**
     * Remove a member from an organization
     */
    suspend fun removeOrganizationMember(organizationId: Int, userId: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeOrganizationMember(orgId=$organizationId, userId=$userId)",
            mutation = {
                apolloClient.mutation(RemoveOrganizationMemberMutation(
                    organizationId = organizationId,
                    userId = userId
                )).execute()
            },
            transform = { data ->
                data.removeOrganizationMember
            }
        )
    }
}
