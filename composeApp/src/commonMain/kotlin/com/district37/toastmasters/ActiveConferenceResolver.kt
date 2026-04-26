package com.district37.toastmasters

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.database.DevSettingsRepository
import com.district37.toastmasters.graphql.ActiveConferenceQuery
import com.district37.toastmasters.graphql.ConferenceByIdQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource

data class ResolvedConference(
    val id: Long,
    val name: String,
    val scheduleTitle: String?,
    val appHeaderTitle: String?,
    val startDate: String?,
    val endDate: String?
)

class ActiveConferenceResolver(
    private val apolloClient: ApolloClient,
    private val devSettingsRepository: DevSettingsRepository
) {
    private val logger = Logger.withTag("ActiveConferenceResolver")

    private fun normalizeDateScalar(value: Any?): String? = when (value) {
        null -> null
        is String -> value
        else -> value.toString().takeIf { it.isNotBlank() }
    }

    suspend fun resolve(): Resource<ResolvedConference> {
        val overrideId = devSettingsRepository.getConferenceOverrideId()
        return if (overrideId != null) {
            logger.d { "Resolving conference via dev override id=$overrideId" }
            fetchById(overrideId)
        } else {
            fetchActive()
        }
    }

    private suspend fun fetchActive(): Resource<ResolvedConference> {
        return try {
            val response = apolloClient.query(ActiveConferenceQuery()).execute()
            if (response.hasErrors()) {
                logger.e { "ActiveConferenceQuery errors: ${response.errors?.joinToString { it.message }}" }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }
            val node = response.data?.conferencesCollection?.edges?.firstOrNull()?.node
                ?: run {
                    logger.e { "ActiveConferenceQuery returned no active conference." }
                    return Resource.Error(ErrorType.NOT_FOUND)
                }
            Resource.Success(
                ResolvedConference(
                    id = node.id,
                    name = node.name,
                    scheduleTitle = node.schedule_title,
                    appHeaderTitle = node.app_header_title,
                    startDate = normalizeDateScalar(node.start_date),
                    endDate = normalizeDateScalar(node.end_date)
                )
            )
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch active conference." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    private suspend fun fetchById(id: Long): Resource<ResolvedConference> {
        return try {
            val response = apolloClient.query(ConferenceByIdQuery(id)).execute()
            if (response.hasErrors()) {
                logger.e { "ConferenceByIdQuery errors: ${response.errors?.joinToString { it.message }}" }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }
            val node = response.data?.conferencesCollection?.edges?.firstOrNull()?.node
                ?: run {
                    logger.e { "ConferenceByIdQuery returned no conference for id=$id." }
                    return Resource.Error(ErrorType.NOT_FOUND)
                }
            Resource.Success(
                ResolvedConference(
                    id = node.id,
                    name = node.name,
                    scheduleTitle = node.schedule_title,
                    appHeaderTitle = node.app_header_title,
                    startDate = normalizeDateScalar(node.start_date),
                    endDate = normalizeDateScalar(node.end_date)
                )
            )
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch conference by id=$id." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }
}
