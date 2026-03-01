package com.district37.toastmasters

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ActiveConferenceQuery

class ConferenceRepository(
    private val apolloClient: ApolloClient
) {
    data class ConferenceTitles(
        val scheduleTitle: String?,
        val appHeaderTitle: String?
    )

    suspend fun getConferenceTitles(): ConferenceTitles? {
        return try {
            val response = apolloClient.query(ActiveConferenceQuery()).execute()
            if (response.hasErrors()) {
                return null
            }

            response.data?.conferencesCollection?.edges
                ?.firstOrNull()
                ?.node
                ?.let { conference ->
                    ConferenceTitles(
                        scheduleTitle = conference.schedule_title?.trim()?.takeIf { it.isNotEmpty() }
                            ?: conference.name.trim().takeIf { it.isNotEmpty() },
                        appHeaderTitle = conference.app_header_title?.trim()?.takeIf { it.isNotEmpty() }
                            ?: conference.schedule_title?.trim()?.takeIf { it.isNotEmpty() }
                            ?: conference.name.trim().takeIf { it.isNotEmpty() }
                    )
                }
        } catch (_: Exception) {
            null
        }
    }
}
