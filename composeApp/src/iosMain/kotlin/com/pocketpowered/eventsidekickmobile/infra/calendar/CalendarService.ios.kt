package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.EventKit.EKAuthorizationStatus
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKCalendar
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneWithName
import kotlin.coroutines.resume

/**
 * iOS implementation of CalendarService using EventKit.
 */
@OptIn(ExperimentalForeignApi::class)
actual class CalendarService {
    private val TAG = "CalendarService"
    // Create event store lazily and recreate if needed after permission changes
    private var eventStore = EKEventStore()

    /**
     * Get available calendars on the device.
     */
    actual suspend fun getAvailableCalendars(): CalendarResult<List<CalendarInfo>> {
        if (!hasCalendarPermission()) {
            return CalendarResult.PermissionDenied
        }

        return try {
            val ekCalendars = eventStore.calendarsForEntityType(EKEntityType.EKEntityTypeEvent)
            val calendars = ekCalendars.mapNotNull { calendar ->
                val ekCalendar = calendar as? EKCalendar ?: return@mapNotNull null
                CalendarInfo(
                    id = ekCalendar.calendarIdentifier,
                    name = ekCalendar.title,
                    isPrimary = ekCalendar == eventStore.defaultCalendarForNewEvents,
                    accountName = ekCalendar.source?.title,
                    color = ekCalendar.CGColor?.let {
                        // Extract RGB from CGColor - simplified approach
                        null // iOS colors are complex, skip for now
                    }
                )
            }
            Logger.d(TAG, "Found ${calendars.size} calendars")
            CalendarResult.Success(calendars)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get calendars: ${e.message}")
            CalendarResult.Error("Failed to get calendars: ${e.message}", e)
        }
    }

    /**
     * Check if the app has calendar permissions.
     */
    actual suspend fun hasCalendarPermission(): Boolean {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        Logger.d(TAG, "Calendar authorization status: $status (Authorized=${EKAuthorizationStatusAuthorized}, FullAccess=${EKAuthorizationStatusFullAccess})")
        // On iOS 17+, use EKAuthorizationStatusFullAccess
        // For backwards compatibility, also check EKAuthorizationStatusAuthorized
        val hasPermission = status == EKAuthorizationStatusAuthorized ||
               status == EKAuthorizationStatusFullAccess
        Logger.d(TAG, "hasCalendarPermission returning: $hasPermission")
        return hasPermission
    }

    /**
     * Request calendar permission (called from Swift side if needed).
     * Returns true if permission was granted.
     */
    suspend fun requestCalendarPermission(): Boolean {
        Logger.d(TAG, "requestCalendarPermission() called - about to request full access")
        return suspendCancellableCoroutine { continuation ->
            eventStore.requestFullAccessToEventsWithCompletion { granted, error ->
                Logger.d(TAG, "requestFullAccessToEventsWithCompletion callback: granted=$granted, error=${error?.localizedDescription}")
                if (error != null) {
                    Logger.e(TAG, "Error requesting calendar permission: ${error.localizedDescription}")
                }
                if (granted) {
                    // Refresh event store to ensure we can access calendars after permission grant
                    eventStore = EKEventStore()
                    Logger.d(TAG, "Calendar permission granted, event store refreshed")
                }
                continuation.resume(granted)
            }
        }
    }

    /**
     * Add an event to a specific calendar.
     */
    actual suspend fun addEvent(calendarId: String, event: CalendarEventData): CalendarResult<String> {
        if (!hasCalendarPermission()) {
            return CalendarResult.PermissionDenied
        }

        return try {
            // Find the calendar
            val ekCalendar = eventStore.calendarsForEntityType(EKEntityType.EKEntityTypeEvent)
                .mapNotNull { it as? EKCalendar }
                .firstOrNull { it.calendarIdentifier == calendarId }
                ?: return CalendarResult.Error("Calendar not found: $calendarId")

            // Create the event
            val ekEvent = EKEvent.eventWithEventStore(eventStore)
            ekEvent.calendar = ekCalendar
            ekEvent.title = event.title
            ekEvent.notes = event.description

            // Set start date
            ekEvent.startDate = event.startTime.toNSDate()

            // Set end date (default to 1 hour after start if not specified)
            val endInstant = event.endTime ?: kotlinx.datetime.Instant.fromEpochMilliseconds(
                event.startTime.toEpochMilliseconds() + 3600000
            )
            ekEvent.endDate = endInstant.toNSDate()

            // Set timezone
            val nsTimeZone = NSTimeZone.timeZoneWithName(event.timezone)
            if (nsTimeZone != null) {
                ekEvent.timeZone = nsTimeZone
            }

            // Set location
            event.location?.let { location ->
                ekEvent.location = location
            }

            // Note: For structured location with coordinates, we'd need to use
            // EKStructuredLocation, but that requires more complex setup

            // Save the event
            var saveError: NSError? = null
            val success = eventStore.saveEvent(
                event = ekEvent,
                span = EKSpan.EKSpanThisEvent,
                error = null  // We'll handle errors via exception
            )

            if (success) {
                val eventIdentifier = ekEvent.eventIdentifier
                if (eventIdentifier != null) {
                    Logger.d(TAG, "Created calendar event with ID: $eventIdentifier for event ${event.eventId}")
                    CalendarResult.Success(eventIdentifier)
                } else {
                    CalendarResult.Error("Event saved but no identifier returned")
                }
            } else {
                CalendarResult.Error("Failed to save event")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create calendar event: ${e.message}")
            CalendarResult.Error("Failed to create calendar event: ${e.message}", e)
        }
    }

    /**
     * Update an existing calendar event.
     */
    actual suspend fun updateEvent(calendarEventId: String, event: CalendarEventData): CalendarResult<Unit> {
        if (!hasCalendarPermission()) {
            return CalendarResult.PermissionDenied
        }

        return try {
            val ekEvent = eventStore.eventWithIdentifier(calendarEventId)
                ?: return CalendarResult.Error("Calendar event not found: $calendarEventId")

            // Update event properties
            ekEvent.title = event.title
            ekEvent.notes = event.description
            ekEvent.startDate = event.startTime.toNSDate()

            val endInstant = event.endTime ?: kotlinx.datetime.Instant.fromEpochMilliseconds(
                event.startTime.toEpochMilliseconds() + 3600000
            )
            ekEvent.endDate = endInstant.toNSDate()

            val nsTimeZone = NSTimeZone.timeZoneWithName(event.timezone)
            if (nsTimeZone != null) {
                ekEvent.timeZone = nsTimeZone
            }

            event.location?.let { location ->
                ekEvent.location = location
            }

            val success = eventStore.saveEvent(
                event = ekEvent,
                span = EKSpan.EKSpanThisEvent,
                error = null
            )

            if (success) {
                Logger.d(TAG, "Updated calendar event: $calendarEventId")
                CalendarResult.Success(Unit)
            } else {
                CalendarResult.Error("Failed to update event")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to update calendar event: ${e.message}")
            CalendarResult.Error("Failed to update calendar event: ${e.message}", e)
        }
    }

    /**
     * Delete a calendar event.
     */
    actual suspend fun deleteEvent(calendarEventId: String): CalendarResult<Unit> {
        if (!hasCalendarPermission()) {
            return CalendarResult.PermissionDenied
        }

        return try {
            val ekEvent = eventStore.eventWithIdentifier(calendarEventId)

            if (ekEvent == null) {
                // Event doesn't exist, treat as success
                Logger.d(TAG, "Calendar event not found: $calendarEventId (may already be deleted)")
                return CalendarResult.Success(Unit)
            }

            val success = eventStore.removeEvent(
                event = ekEvent,
                span = EKSpan.EKSpanThisEvent,
                error = null
            )

            if (success) {
                Logger.d(TAG, "Deleted calendar event: $calendarEventId")
                CalendarResult.Success(Unit)
            } else {
                CalendarResult.Error("Failed to delete event")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete calendar event: ${e.message}")
            CalendarResult.Error("Failed to delete calendar event: ${e.message}", e)
        }
    }

    /**
     * Get the start date of a calendar event.
     * Used by CalendarLauncher to navigate to the correct date.
     */
    actual suspend fun getEventStartDate(calendarEventId: String): kotlinx.datetime.Instant? {
        if (!hasCalendarPermission()) {
            Logger.d(TAG, "No calendar permission when getting event start date")
            return null
        }

        return try {
            val ekEvent = eventStore.eventWithIdentifier(calendarEventId)
            if (ekEvent != null) {
                val startDate = ekEvent.startDate
                if (startDate != null) {
                    // Convert NSDate to kotlinx.datetime.Instant
                    // NSDate uses reference date Jan 1, 2001 (978307200 seconds after Unix epoch)
                    val epochSeconds = (startDate.timeIntervalSinceReferenceDate + 978307200.0).toLong()
                    kotlinx.datetime.Instant.fromEpochSeconds(epochSeconds)
                } else {
                    Logger.d(TAG, "Event found but has no start date: $calendarEventId")
                    null
                }
            } else {
                Logger.d(TAG, "Event not found: $calendarEventId")
                null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get event start date: ${e.message}")
            null
        }
    }
}

/**
 * Extension to convert kotlinx.datetime.Instant to NSDate
 * NSDate reference date is Jan 1, 2001, while Unix epoch is Jan 1, 1970
 * Difference is 978307200 seconds
 */
private fun kotlinx.datetime.Instant.toNSDate(): NSDate {
    // NSDate uses reference date Jan 1, 2001 (978307200 seconds after Unix epoch)
    val timeIntervalSinceReference = this.epochSeconds.toDouble() - 978307200.0
    return NSDate(timeIntervalSinceReferenceDate = timeIntervalSinceReference)
}
