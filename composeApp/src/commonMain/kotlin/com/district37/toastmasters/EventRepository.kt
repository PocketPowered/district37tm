package com.district37.toastmasters

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.EventDetailsQuery
import com.district37.toastmasters.graphql.EventPreviewsByDateQuery
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class EventRepository(
    private val apolloClient: ApolloClient,
    private val resolver: ActiveConferenceResolver
) {
    private val logger = Logger.withTag("EventRepository")

    private fun parseDateKey(dateIso: String): Long? {
        return try {
            LocalDate.parse(dateIso)
                .atStartOfDayIn(TimeZone.UTC)
                .toEpochMilliseconds()
        } catch (_: Exception) {
            null
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
        val conference = resolver.resolve()
        if (conference !is Resource.Success) {
            return if (conference is Resource.Error) conference else Resource.Error(ErrorType.CLIENT_ERROR)
        }

        return try {
            val response = apolloClient.query(
                EventDetailsQuery(
                    id = id.toLong(),
                    conferenceId = conference.data.id
                )
            ).execute()
            if (response.hasErrors()) {
                logger.e {
                    "EventDetailsQuery failed for eventId=$id conferenceId=${conference.data.id}: " +
                        "${response.errors?.joinToString { it.message }}"
                }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val node = response.data?.eventsCollection?.edges?.firstOrNull()?.node
                ?: run {
                    logger.e { "EventDetailsQuery returned no event for eventId=$id conferenceId=${conference.data.id}." }
                    return Resource.Error(ErrorType.NOT_FOUND)
                }

            Resource.Success(node)
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch event details for eventId=$id." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getEventsByKey(dateKey: Long): Resource<List<EventPreviewsByDateQuery.Node>> {
        val conference = resolver.resolve()
        if (conference !is Resource.Success) {
            return if (conference is Resource.Error) conference else Resource.Error(ErrorType.CLIENT_ERROR)
        }

        return try {
            val response = apolloClient.query(
                EventPreviewsByDateQuery(
                    conferenceId = conference.data.id,
                    dateKey = dateKey
                )
            ).execute()
            if (response.hasErrors()) {
                logger.e {
                    "EventPreviewsByDateQuery failed for conferenceId=${conference.data.id} dateKey=$dateKey: " +
                        "${response.errors?.joinToString { it.message }}"
                }
                return Resource.Error(ErrorType.CLIENT_ERROR)
            }

            val previews = response.data?.eventsCollection?.edges?.map { it.node } ?: emptyList()
            Resource.Success(previews)
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch event previews for conferenceId=${conference.data.id} dateKey=$dateKey." }
            Resource.Error(ErrorType.UNKNOWN, e)
        }
    }

    suspend fun getAvailableDates(): Resource<List<Long>> {
        return resolver.resolve().map { conference ->
            generateDateKeys(
                startDateIso = conference.startDate,
                endDateIso = conference.endDate
            )
        }
    }
}
