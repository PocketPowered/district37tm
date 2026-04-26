package com.district37.toastmasters

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ActiveConferenceQuery

class ConferenceRepository(
    private val apolloClient: ApolloClient
) {
    private val logger = Logger.withTag("ConferenceRepository")

    data class ConferenceTitles(
        val scheduleTitle: String?,
        val appHeaderTitle: String?
    )

    suspend fun getConferenceTitles(): ConferenceTitles? {
        return try {
            val response = apolloClient.query(ActiveConferenceQuery()).execute()
            if (response.hasErrors()) {
                logger.e {
                    "ActiveConferenceQuery returned errors: ${response.errors?.joinToString { it.message }}"
                }
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
                } ?: run {
                logger.e { "ActiveConferenceQuery returned no active conference." }
                null
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch conference titles." }
            null
        }
    }
}
