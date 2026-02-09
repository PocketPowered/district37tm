package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.Foundation.NSDate

/**
 * iOS implementation of CalendarEventMatcher using EventKit.
 */
@OptIn(ExperimentalForeignApi::class)
actual class CalendarEventMatcher {
    private val TAG = "CalendarEventMatcher"
    private val eventStore = EKEventStore()

    actual suspend fun eventExistsById(calendarEventId: String): Boolean {
        return try {
            val event = eventStore.eventWithIdentifier(calendarEventId)
            val exists = event != null
            Logger.d(TAG, "Event $calendarEventId exists: $exists")
            exists
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking event existence: ${e.message}")
            false
        }
    }

    actual suspend fun findEventByContent(
        title: String,
        startTime: Instant,
        endTime: Instant?,
        toleranceMinutes: Int
    ): String? {
        return try {
            val toleranceSec = toleranceMinutes * 60.0
            val startDate = startTime.toNSDate()

            // Create search window around the expected start time
            val searchStartDate = NSDate(
                timeIntervalSinceReferenceDate = startDate.timeIntervalSinceReferenceDate - toleranceSec
            )
            val searchEndDate = NSDate(
                timeIntervalSinceReferenceDate = startDate.timeIntervalSinceReferenceDate + toleranceSec
            )

            // Get all calendars
            val calendars = eventStore.calendarsForEntityType(EKEntityType.EKEntityTypeEvent)

            // Create predicate for time range
            val predicate = eventStore.predicateForEventsWithStartDate(
                searchStartDate,
                searchEndDate,
                calendars
            )

            // Find matching events
            val events = eventStore.eventsMatchingPredicate(predicate)

            // Find event with matching title (partial match)
            val matchedEvent = events.firstOrNull { event ->
                val ekEvent = event as? EKEvent
                ekEvent?.title?.contains(title, ignoreCase = true) == true
            } as? EKEvent

            if (matchedEvent != null) {
                val eventId = matchedEvent.eventIdentifier
                Logger.d(TAG, "Found event by content: $eventId (title contains '$title')")
                eventId
            } else {
                Logger.d(TAG, "No event found matching title '$title' and start time $startTime")
                null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error searching for event by content: ${e.message}")
            null
        }
    }

    actual suspend fun matchEvent(criteria: CalendarEventMatchCriteria): CalendarEventMatchResult {
        // First, try to find by original ID (fastest)
        if (eventExistsById(criteria.originalCalendarEventId)) {
            Logger.d(TAG, "Matched event by ID: ${criteria.originalCalendarEventId}")
            return CalendarEventMatchResult.FoundById(criteria.originalCalendarEventId)
        }

        // Fallback: try to find by content (title + time)
        val matchedId = findEventByContent(
            title = criteria.title,
            startTime = criteria.startTime,
            endTime = criteria.endTime
        )

        return if (matchedId != null) {
            Logger.d(TAG, "Matched event by content: $matchedId")
            CalendarEventMatchResult.FoundByContent(matchedId)
        } else {
            Logger.d(TAG, "No match found for event with title '${criteria.title}'")
            CalendarEventMatchResult.NotFound
        }
    }
}

/**
 * Extension to convert kotlinx.datetime.Instant to NSDate
 */
private fun Instant.toNSDate(): NSDate {
    // NSDate uses reference date Jan 1, 2001 (978307200 seconds after Unix epoch)
    val timeIntervalSinceReference = this.epochSeconds.toDouble() - 978307200.0
    return NSDate(timeIntervalSinceReferenceDate = timeIntervalSinceReference)
}
