package com.district37.toastmasters

import co.touchlab.kermit.Logger
import com.wongislandd.nexus.util.Resource

class ConferenceRepository(
    private val resolver: ActiveConferenceResolver
) {
    private val logger = Logger.withTag("ConferenceRepository")

    data class ConferenceTitles(
        val scheduleTitle: String?,
        val appHeaderTitle: String?
    )

    suspend fun getConferenceTitles(): ConferenceTitles? {
        return when (val result = resolver.resolve()) {
            is Resource.Success -> {
                val conference = result.data
                ConferenceTitles(
                    scheduleTitle = conference.scheduleTitle?.trim()?.takeIf { it.isNotEmpty() }
                        ?: conference.name.trim().takeIf { it.isNotEmpty() },
                    appHeaderTitle = conference.appHeaderTitle?.trim()?.takeIf { it.isNotEmpty() }
                        ?: conference.scheduleTitle?.trim()?.takeIf { it.isNotEmpty() }
                        ?: conference.name.trim().takeIf { it.isNotEmpty() }
                )
            }
            else -> {
                logger.e { "Failed to resolve active conference for titles." }
                null
            }
        }
    }
}
