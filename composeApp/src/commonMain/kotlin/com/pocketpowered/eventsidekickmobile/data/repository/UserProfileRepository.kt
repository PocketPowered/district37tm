package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.graphql.CheckUsernameAvailabilityMutation
import com.district37.toastmasters.graphql.GetMyProfileQuery
import com.district37.toastmasters.graphql.GetUserProfileQuery
import com.district37.toastmasters.graphql.UpdateMyProfileMutation
import com.district37.toastmasters.graphql.type.UpdateUserInput
import com.district37.toastmasters.graphql.type.UserPreferencesInput
import com.district37.toastmasters.data.transformers.toMyProfile
import com.district37.toastmasters.data.transformers.toUser
import com.district37.toastmasters.data.transformers.toUserProfile
import com.district37.toastmasters.models.MyProfile
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.UserProfile
import com.district37.toastmasters.util.Resource

/**
 * Repository for User Profile data
 * Fetches and updates user profile from GraphQL API using Apollo client
 */
class UserProfileRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "UserProfileRepository"

    /**
     * Get unified self-profile containing user data, subscribed events, attending events,
     * activity feed, and incoming friend requests.
     *
     * Returns MyProfile with all data needed for the profile screen in a single call.
     * This replaces 4 separate API calls with a single efficient request.
     *
     * @param subscribedEventsFirst Number of subscribed events to fetch (default 10)
     * @param attendingEventsFirst Number of attending events to fetch (default 10)
     * @param activityFeedFirst Number of activity feed items to fetch (default 10)
     * @param friendRequestsFirst Number of friend requests to fetch (default 10)
     */
    suspend fun getMyProfile(
        subscribedEventsFirst: Int = 10,
        attendingEventsFirst: Int = 10,
        activityFeedFirst: Int = 10,
        friendRequestsFirst: Int = 10
    ): Resource<MyProfile> {
        return executeQuery(
            queryName = "getMyProfile",
            query = {
                apolloClient.query(
                    GetMyProfileQuery(
                        subscribedEventsFirst = Optional.present(subscribedEventsFirst),
                        attendingEventsFirst = Optional.present(attendingEventsFirst),
                        activityFeedFirst = Optional.present(activityFeedFirst),
                        friendRequestsFirst = Optional.present(friendRequestsFirst)
                    )
                ).execute()
            },
            transform = { data -> data.getMyProfile?.toMyProfile() }
        )
    }

    /**
     * Get unified user profile containing user, relationship status, attending events, and activity feed.
     * The server handles all permission checking and conditionally populates attending events and activity feed
     * based on friendship status.
     *
     * Returns UserProfile with:
     * - user: Always populated with the requested user's profile
     * - relationshipStatus: Always populated (NotFriends, PendingIncoming, PendingOutgoing, or Friends)
     * - attendingEvents: Populated only if users are friends, null otherwise
     * - activityFeed: Populated only if users are friends, null otherwise
     */
    suspend fun getUserProfile(userId: String): Resource<UserProfile> {
        return executeQuery(
            queryName = "getUserProfile",
            query = { apolloClient.query(GetUserProfileQuery(userId = userId)).execute() },
            transform = { data -> data.getUserProfile?.toUserProfile() }
        )
    }

    /**
     * Update the current authenticated user's profile
     *
     * @param displayName New display name (null to keep unchanged)
     * @param bio New bio (null to keep unchanged)
     * @param profileImageUrl New profile image URL (null to keep unchanged)
     * @param username New username (null to keep unchanged)
     * @param primaryColor New primary/banner color in hex format (null to keep unchanged)
     * @param secondaryColor New secondary/background color in hex format (null to keep unchanged)
     */
    suspend fun updateMyProfile(
        displayName: String? = null,
        bio: String? = null,
        profileImageUrl: String? = null,
        username: String? = null,
        primaryColor: String? = null,
        secondaryColor: String? = null
    ): Resource<User> {
        val input = UpdateUserInput(
            displayName = if (displayName != null) Optional.present(displayName) else Optional.Absent,
            bio = if (bio != null) Optional.present(bio) else Optional.Absent,
            profileImageUrl = if (profileImageUrl != null) Optional.present(profileImageUrl) else Optional.Absent,
            username = if (username != null) Optional.present(username) else Optional.Absent,
            primaryColor = if (primaryColor != null) Optional.present(primaryColor) else Optional.Absent,
            secondaryColor = if (secondaryColor != null) Optional.present(secondaryColor) else Optional.Absent
        )

        return executeMutation(
            mutationName = "updateMyProfile",
            mutation = { apolloClient.mutation(UpdateMyProfileMutation(input = input)).execute() },
            transform = { data -> data.updateMyUser.toUser() }
        )
    }

    /**
     * Check if a username is available
     * @param username The username to check
     * @return Resource<Boolean> - true if available, false if taken or invalid
     */
    suspend fun checkUsernameAvailability(username: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "checkUsernameAvailability",
            mutation = { apolloClient.mutation(CheckUsernameAvailabilityMutation(username = username)).execute() },
            transform = { data -> data.checkUsernameAvailability }
        )
    }

    /**
     * Update the current authenticated user's preferences
     *
     * @param theme Theme preference: "system", "light", or "dark" (null to keep unchanged)
     * @param notificationsEnabled Notifications preference (null to keep unchanged)
     * @param displayDensity Display density preference (null to keep unchanged)
     */
    suspend fun updateMyPreferences(
        theme: String? = null,
        notificationsEnabled: Boolean? = null,
        displayDensity: String? = null
    ): Resource<User> {
        val preferencesInput = UserPreferencesInput(
            theme = if (theme != null) Optional.present(theme) else Optional.Absent,
            notificationsEnabled = if (notificationsEnabled != null) Optional.present(notificationsEnabled) else Optional.Absent,
            displayDensity = if (displayDensity != null) Optional.present(displayDensity) else Optional.Absent
        )

        val input = UpdateUserInput(
            preferences = Optional.present(preferencesInput)
        )

        return executeMutation(
            mutationName = "updateMyPreferences",
            mutation = { apolloClient.mutation(UpdateMyProfileMutation(input = input)).execute() },
            transform = { data -> data.updateMyUser.toUser() }
        )
    }
}
