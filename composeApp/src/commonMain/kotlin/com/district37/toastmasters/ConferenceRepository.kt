package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ActiveConferenceQuery

class ConferenceRepository(
    private val apolloClient: ApolloClient
) {
    suspend fun getConferenceScheduleTitle(): String? {
        return try {
            val response = apolloClient.query(ActiveConferenceQuery()).execute()
            if (response.hasErrors()) {
                return null
            }

            response.data?.conferencesCollection?.edges
                ?.firstOrNull()
                ?.node
                ?.let { conference ->
                    conference.schedule_title?.trim()?.takeIf { it.isNotEmpty() }
                        ?: conference.name.trim().takeIf { it.isNotEmpty() }
                }
        } catch (_: Exception) {
            null
        }
    }
}
