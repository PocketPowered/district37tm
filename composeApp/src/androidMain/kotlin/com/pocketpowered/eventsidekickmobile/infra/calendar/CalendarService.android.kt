package com.district37.toastmasters.infra.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

/**
 * Android implementation of CalendarService using ContentResolver and CalendarContract.
 */
actual class CalendarService(private val context: Context) {
    private val TAG = "CalendarService"

    /**
     * Get available calendars on the device.
     */
    actual suspend fun getAvailableCalendars(): CalendarResult<List<CalendarInfo>> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext CalendarResult.PermissionDenied
        }

        try {
            val calendars = mutableListOf<CalendarInfo>()
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR
            )

            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars.VISIBLE} = 1",
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val nameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val primaryIndex = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                val accountIndex = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                val colorIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)

                while (cursor.moveToNext()) {
                    calendars.add(
                        CalendarInfo(
                            id = cursor.getLong(idIndex).toString(),
                            name = cursor.getString(nameIndex) ?: "Unknown Calendar",
                            isPrimary = cursor.getInt(primaryIndex) == 1,
                            accountName = cursor.getString(accountIndex),
                            color = if (colorIndex >= 0) cursor.getInt(colorIndex) else null
                        )
                    )
                }
            }

            Logger.d(TAG, "Found ${calendars.size} calendars")
            CalendarResult.Success(calendars)
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied when accessing calendars: ${e.message}")
            CalendarResult.PermissionDenied
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get calendars: ${e.message}")
            CalendarResult.Error("Failed to get calendars: ${e.message}", e)
        }
    }

    /**
     * Check if the app has calendar permissions.
     */
    actual suspend fun hasCalendarPermission(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        return readPermission && writePermission
    }

    /**
     * Add an event to a specific calendar.
     */
    actual suspend fun addEvent(calendarId: String, event: CalendarEventData): CalendarResult<String> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext CalendarResult.PermissionDenied
        }

        try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId.toLong())
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description ?: "")
                put(CalendarContract.Events.DTSTART, event.startTime.toEpochMilliseconds())

                // If no end time, default to 1 hour after start
                val endTimeMillis = event.endTime?.toEpochMilliseconds()
                    ?: (event.startTime.toEpochMilliseconds() + 3600000)
                put(CalendarContract.Events.DTEND, endTimeMillis)

                // Use the event's timezone or fall back to device timezone
                val tz = try {
                    TimeZone.getTimeZone(event.timezone)
                } catch (e: Exception) {
                    TimeZone.getDefault()
                }
                put(CalendarContract.Events.EVENT_TIMEZONE, tz.id)

                // Add location if available
                event.location?.let { location ->
                    put(CalendarContract.Events.EVENT_LOCATION, location)
                }

                // Add GPS coordinates if available (Android supports this)
                if (event.latitude != null && event.longitude != null) {
                    // Some calendar apps support geo: URI in description
                    val existingDesc = event.description ?: ""
                    val geoInfo = "\n\nLocation: geo:${event.latitude},${event.longitude}"
                    put(CalendarContract.Events.DESCRIPTION, existingDesc + geoInfo)
                }
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.let { ContentUris.parseId(it) }

            if (eventId != null && eventId > 0) {
                Logger.d(TAG, "Created calendar event with ID: $eventId for event ${event.eventId}")
                CalendarResult.Success(eventId.toString())
            } else {
                Logger.e(TAG, "Failed to create calendar event - no ID returned")
                CalendarResult.Error("Failed to create calendar event")
            }
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied when creating event: ${e.message}")
            CalendarResult.PermissionDenied
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create calendar event: ${e.message}")
            CalendarResult.Error("Failed to create calendar event: ${e.message}", e)
        }
    }

    /**
     * Update an existing calendar event.
     */
    actual suspend fun updateEvent(calendarEventId: String, event: CalendarEventData): CalendarResult<Unit> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext CalendarResult.PermissionDenied
        }

        try {
            val eventUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                calendarEventId.toLong()
            )

            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description ?: "")
                put(CalendarContract.Events.DTSTART, event.startTime.toEpochMilliseconds())

                val endTimeMillis = event.endTime?.toEpochMilliseconds()
                    ?: (event.startTime.toEpochMilliseconds() + 3600000)
                put(CalendarContract.Events.DTEND, endTimeMillis)

                val tz = try {
                    TimeZone.getTimeZone(event.timezone)
                } catch (e: Exception) {
                    TimeZone.getDefault()
                }
                put(CalendarContract.Events.EVENT_TIMEZONE, tz.id)

                event.location?.let { location ->
                    put(CalendarContract.Events.EVENT_LOCATION, location)
                }
            }

            val rowsUpdated = context.contentResolver.update(eventUri, values, null, null)

            if (rowsUpdated > 0) {
                Logger.d(TAG, "Updated calendar event: $calendarEventId")
                CalendarResult.Success(Unit)
            } else {
                Logger.e(TAG, "No rows updated for calendar event: $calendarEventId")
                CalendarResult.Error("Calendar event not found")
            }
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied when updating event: ${e.message}")
            CalendarResult.PermissionDenied
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to update calendar event: ${e.message}")
            CalendarResult.Error("Failed to update calendar event: ${e.message}", e)
        }
    }

    /**
     * Delete a calendar event.
     */
    actual suspend fun deleteEvent(calendarEventId: String): CalendarResult<Unit> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext CalendarResult.PermissionDenied
        }

        try {
            val eventUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                calendarEventId.toLong()
            )

            val rowsDeleted = context.contentResolver.delete(eventUri, null, null)

            if (rowsDeleted > 0) {
                Logger.d(TAG, "Deleted calendar event: $calendarEventId")
                CalendarResult.Success(Unit)
            } else {
                Logger.d(TAG, "No rows deleted for calendar event: $calendarEventId (may already be deleted)")
                CalendarResult.Success(Unit) // Treat as success if event doesn't exist
            }
        } catch (e: SecurityException) {
            Logger.e(TAG, "Permission denied when deleting event: ${e.message}")
            CalendarResult.PermissionDenied
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete calendar event: ${e.message}")
            CalendarResult.Error("Failed to delete calendar event: ${e.message}", e)
        }
    }

    /**
     * Get the start date of a calendar event.
     * Used by CalendarLauncher to navigate to the correct date.
     */
    actual suspend fun getEventStartDate(calendarEventId: String): kotlinx.datetime.Instant? = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            Logger.d(TAG, "No calendar permission when getting event start date")
            return@withContext null
        }

        try {
            val eventId = calendarEventId.toLongOrNull()
            if (eventId == null) {
                Logger.d(TAG, "Invalid calendar event ID format: $calendarEventId")
                return@withContext null
            }

            val projection = arrayOf(CalendarContract.Events.DTSTART)
            val eventUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId
            )

            context.contentResolver.query(
                eventUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val startIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                    if (startIndex >= 0) {
                        val startTimeMillis = cursor.getLong(startIndex)
                        if (startTimeMillis > 0) {
                            return@withContext kotlinx.datetime.Instant.fromEpochMilliseconds(startTimeMillis)
                        }
                    }
                }
            }

            Logger.d(TAG, "Event not found: $calendarEventId")
            null
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get event start date: ${e.message}")
            null
        }
    }
}
