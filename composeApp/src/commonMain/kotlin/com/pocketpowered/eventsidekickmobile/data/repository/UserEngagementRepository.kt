package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.transformers.toEvent
import com.district37.toastmasters.graphql.GetMyAttendingEventsQuery
import com.district37.toastmasters.graphql.GetMySubscribedEventsQuery
import com.district37.toastmasters.graphql.SubscribeToEntityMutation
import com.district37.toastmasters.graphql.SetEntityStatusMutation
import com.district37.toastmasters.graphql.UnsubscribeFromEntityMutation
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.UserEngagement
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.util.Resource

/**
 * Repository for managing user engagement actions (subscribe, status)
 */
class UserEngagementRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "UserEngagementRepository"

    /**
     * Subscribe to an entity for the current user.
     * Subscribing means the user will receive notifications for changes to this entity.
     */
    suspend fun subscribeToEntity(entityType: EntityType, entityId: Int): Resource<UserEngagement> {
        return executeMutation(
            mutationName = "subscribeToEntity(entityType=$entityType, entityId=$entityId)",
            mutation = {
                apolloClient.mutation(
                    SubscribeToEntityMutation(entityType = entityType, entityId = entityId)
                ).execute()
            },
            transform = { data ->
                data.subscribeToEntity.toUserEngagement()
            }
        )
    }

    /**
     * Unsubscribe from an entity for the current user.
     * Unsubscribing means the user will no longer receive notifications for this entity.
     */
    suspend fun unsubscribeFromEntity(entityType: EntityType, entityId: Int): Resource<UserEngagement> {
        return executeMutation(
            mutationName = "unsubscribeFromEntity(entityType=$entityType, entityId=$entityId)",
            mutation = {
                apolloClient.mutation(
                    UnsubscribeFromEntityMutation(entityType = entityType, entityId = entityId)
                ).execute()
            },
            transform = { data ->
                // Server returns null when the engagement record is deleted
                // In this case, return a default engagement with isSubscribed=false
                data.unsubscribeFromEntity?.toUserEngagement() ?: UserEngagement(
                    isSubscribed = false,
                    status = null
                )
            }
        )
    }

    /**
     * Set the engagement status for an entity (RSVP for events/agenda items)
     * Pass null to clear the status
     */
    suspend fun setEntityStatus(
        entityType: EntityType,
        entityId: Int,
        status: UserEngagementStatus?
    ): Resource<UserEngagement> {
        return executeMutation(
            mutationName = "setEntityStatus(entityType=$entityType, entityId=$entityId, status=$status)",
            mutation = {
                apolloClient.mutation(
                    SetEntityStatusMutation(
                        entityType = entityType,
                        entityId = entityId,
                        // Use Optional.present(null) to explicitly clear status, not Optional.absent()
                        status = Optional.present(status)
                    )
                ).execute()
            },
            transform = { data ->
                // Server returns null when clearing status and no other engagement exists
                // In this case, return a default engagement with cleared values
                data.setEntityStatus?.toUserEngagement() ?: UserEngagement(
                    isSubscribed = false,
                    status = null
                )
            }
        )
    }

    // Extension functions to convert mutation results to UserEngagement
    private fun SubscribeToEntityMutation.SubscribeToEntity.toUserEngagement() = UserEngagement(
        isSubscribed = userEngagementDetails.isSubscribed,
        status = userEngagementDetails.status
    )

    private fun UnsubscribeFromEntityMutation.UnsubscribeFromEntity.toUserEngagement() = UserEngagement(
        isSubscribed = userEngagementDetails.isSubscribed,
        status = userEngagementDetails.status
    )

    private fun SetEntityStatusMutation.SetEntityStatus.toUserEngagement() = UserEngagement(
        isSubscribed = userEngagementDetails.isSubscribed,
        status = userEngagementDetails.status
    )

    /**
     * Get the current user's subscribed events
     */
    suspend fun getMySubscribedEvents(
        first: Int? = null,
        after: String? = null
    ): Resource<UserEventsConnection> {
        return executeQuery(
            queryName = "getMySubscribedEvents(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMySubscribedEventsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val connection = data.mySubscriptionsConnection
                UserEventsConnection(
                    events = connection.edges.mapNotNull { edge ->
                        edge.node.event?.eventPreview?.toEvent()
                    },
                    hasNextPage = connection.pageInfo.hasNextPage,
                    endCursor = connection.pageInfo.endCursor,
                    totalCount = connection.totalCount
                )
            }
        )
    }

    /**
     * Get the current user's attending events (RSVP'd as Going)
     */
    suspend fun getMyAttendingEvents(
        first: Int? = null,
        after: String? = null
    ): Resource<UserEventsConnection> {
        return executeQuery(
            queryName = "getMyAttendingEvents(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyAttendingEventsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val connection = data.myEngagementsConnectionByStatus
                UserEventsConnection(
                    events = connection.edges.mapNotNull { edge ->
                        edge.node.event?.eventPreview?.toEvent()
                    },
                    hasNextPage = connection.pageInfo.hasNextPage,
                    endCursor = connection.pageInfo.endCursor,
                    totalCount = connection.totalCount
                )
            }
        )
    }

    /**
     * Get a specific user's attending events (RSVP'd as Going)
     *
     * TODO: This method is temporarily stubbed out - now handled by unified getUserProfile endpoint.
     * The GetUserAttendingEvents.graphql query has been renamed to .disabled.
     * After server deployment and schema update, this may no longer be needed as the unified
     * getUserProfile endpoint returns attending events directly.
     */
    @Deprecated(
        message = "Use UserProfileRepository.getUserProfile() instead - it returns attending events for friends",
        replaceWith = ReplaceWith("userProfileRepository.getUserProfile(userId)")
    )
    suspend fun getUserAttendingEvents(
        userId: String,
        first: Int? = null,
        after: String? = null
    ): Resource<UserEventsConnection> {
        throw NotImplementedError("Use UserProfileRepository.getUserProfile() instead")
    }
}
