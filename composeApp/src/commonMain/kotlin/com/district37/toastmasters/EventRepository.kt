package com.district37.toastmasters

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.AvailableDatesQuery
import com.district37.toastmasters.graphql.EventDetailsQuery
import com.district37.toastmasters.graphql.EventPreviewsByDateQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class EventRepository(private val apolloClient: ApolloClient) {
    private val logger = Logger.withTag("EventRepository")

    private data class ActiveConferenceDateRange(
        val conferenceId: Long,
        val startDateIso: String?,
        val endDateIso: String?
    )

    private suspend fun getActiveConferenceDateRange(): Resource<ActiveConferenceDateRange> {
        return try {
            val response = apolloClient.query(AvailableDatesQuery()).execute()
            if (response.hasErrors()) {
                logger.e {
                    "AvailableDatesQuery returned errors: ${response.errors?.joinToString { it.message }}"
                }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val activeConference = response.data?.conferencesCollection?.edges?.firstOrNull()?.node
                ?: run {
                    logger.e { "AvailableDatesQuery returned no active conference." }
                    return Resource.Error(ErrorType.NOT_FOUND)
                }

            Resource.Success(
                ActiveConferenceDateRange(
                    conferenceId = activeConference.id,
                    startDateIso = normalizeDateScalar(activeConference.start_date),
                    endDateIso = normalizeDateScalar(activeConference.end_date)
                )
            )
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch active conference date range." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    private fun parseDateKey(dateIso: String): Long? {
        return try {
            LocalDate.parse(dateIso)
                .atStartOfDayIn(TimeZone.UTC)
                .toEpochMilliseconds()
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeDateScalar(value: Any?): String? {
        return when (value) {
            null -> null
            is String -> value
            else -> value.toString().takeIf { it.isNotBlank() }
        }
    }

    private fun generateDateKeys(startDateIso: String?, endDateIso: String?): List<Long> {
        val startDateKey = startDateIso?.let(::parseDateKey) ?: return emptyList()
        val endDateKey = endDateIso?.let(::parseDateKey) ?: return emptyList()
        if (endDateKey < startDateKey) {
            return emptyList()
        }

        val oneDayMs = 86_400_000L
        return buildList {
            var current = startDateKey
            while (current <= endDateKey) {
                add(current)
                current += oneDayMs
            }
        }
    }

    suspend fun getEventDetails(id: Int): Resource<EventDetailsQuery.Node> {
        val activeConference = getActiveConferenceDateRange()
        if (activeConference !is Resource.Success) {
            return if (activeConference is Resource.Error) activeConference else Resource.Error(ErrorType.CLIENT_ERROR)
        }

        return try {
            val response = apolloClient.query(
                EventDetailsQuery(
                    id = id.toLong(),
                    conferenceId = activeConference.data.conferenceId
                )
            ).execute()
            if (response.hasErrors()) {
                logger.e {
                    "EventDetailsQuery failed for eventId=$id conferenceId=${activeConference.data.conferenceId}: " +
                        "${response.errors?.joinToString { it.message }}"
                }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val node = response.data?.eventsCollection?.edges?.firstOrNull()?.node
                ?: run {
                    logger.e {
                        "EventDetailsQuery returned no event for eventId=$id conferenceId=${activeConference.data.conferenceId}."
                    }
                    return Resource.Error(ErrorType.NOT_FOUND)
                }

            Resource.Success(node)
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch event details for eventId=$id." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getEventsByKey(dateKey: Long): Resource<List<EventPreviewsByDateQuery.Node>> {
        val activeConference = getActiveConferenceDateRange()
        if (activeConference !is Resource.Success) {
            return if (activeConference is Resource.Error) activeConference else Resource.Error(ErrorType.CLIENT_ERROR)
        }

        return try {
            val response = apolloClient.query(
                EventPreviewsByDateQuery(
                    conferenceId = activeConference.data.conferenceId,
                    dateKey = dateKey
                )
            ).execute()
            if (response.hasErrors()) {
                logger.e {
                    "EventPreviewsByDateQuery failed for conferenceId=${activeConference.data.conferenceId} dateKey=$dateKey: " +
                        "${response.errors?.joinToString { it.message }}"
                }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val previews = response.data?.eventsCollection?.edges?.map { it.node } ?: emptyList()
            Resource.Success(previews)
        } catch (e: Exception) {
            logger.e(e) {
                "Failed to fetch event previews for conferenceId=${activeConference.data.conferenceId} dateKey=$dateKey."
            }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getAvailableDates(): Resource<List<Long>> {
        return getActiveConferenceDateRange().map { activeConference ->
            generateDateKeys(
                startDateIso = activeConference.startDateIso,
                endDateIso = activeConference.endDateIso
            )
        }
    }
}
