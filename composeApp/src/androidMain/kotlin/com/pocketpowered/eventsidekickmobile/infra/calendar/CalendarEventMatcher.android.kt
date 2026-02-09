package com.district37.toastmasters.infra.calendar

import android.content.Context
import android.provider.CalendarContract
import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

/**
 * Android implementation of CalendarEventMatcher using ContentResolver.
 */
actual class CalendarEventMatcher(private val context: Context) {
    private val TAG = "CalendarEventMatcher"

    actual suspend fun eventExistsById(calendarEventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val eventId = calendarEventId.toLongOrNull()
            if (eventId == null) {
                Logger.d(TAG, "Invalid calendar event ID format: $calendarEventId")
                return@withContext false
            }

            val projection = arrayOf(CalendarContract.Events._ID)
            val selection = "${CalendarContract.Events._ID} = ?"
            val selectionArgs = arrayOf(eventId.toString())

            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val exists = cursor.count > 0
                Logger.d(TAG, "Event $calendarEventId exists: $exists")
                return@withContext exists
            }
            false
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied checking event existence: ${e.message}")
            false
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
    ): String? = withContext(Dispatchers.IO) {
        try {
            val toleranceMs = toleranceMinutes * 60 * 1000L
            val startMs = startTime.toEpochMilliseconds()
            val minStart = startMs - toleranceMs
            val maxStart = startMs + toleranceMs

            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART
            )

            // Query for events with matching title pattern and start time within tolerance
            // Use LIKE for partial matching since we append " - EventName" to titles
            val selection = "${CalendarContract.Events.TITLE} LIKE ? AND " +
                    "${CalendarContract.Events.DTSTART} >= ? AND " +
                    "${CalendarContract.Events.DTSTART} <= ?"
            val selectionArgs = arrayOf(
                "%$title%",
                minStart.toString(),
                maxStart.toString()
            )

            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(CalendarContract.Events._ID)
                    val eventId = cursor.getLong(idIndex).toString()
                    Logger.d(TAG, "Found event by content: $eventId (title contains '$title')")
                    return@withContext eventId
                }
            }

            Logger.d(TAG, "No event found matching title '$title' and start time $startTime")
            null
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied searching for event: ${e.message}")
            null
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
