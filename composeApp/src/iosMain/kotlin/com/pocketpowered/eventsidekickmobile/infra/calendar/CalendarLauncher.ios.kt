package com.district37.toastmasters.infra.calendar

import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSinceReferenceDate
import platform.UIKit.UIApplication

/**
 * iOS implementation of CalendarLauncher.
 * Opens the Calendar app to the date of the specified event.
 *
 * Note: iOS doesn't have a public URL scheme to open a specific event directly.
 * We look up the event to get its date, then open the Calendar app to that date.
 */
actual class CalendarLauncher(
    private val calendarService: CalendarService
) {
    private val TAG = "CalendarLauncher"
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Open the Calendar app to the date of the specified event.
     * Uses the calshow: URL scheme with Core Data reference timestamp.
     */
    actual fun openCalendarEvent(nativeEventId: String) {
        scope.launch {
            try {
                // Look up the event start date using CalendarService (which has proper permissions)
                val startDate = calendarService.getEventStartDate(nativeEventId)

                val timestamp: Double = if (startDate != null) {
                    // Convert kotlinx.datetime.Instant to Core Data reference timestamp
                    // Core Data reference date is Jan 1, 2001 (978307200 seconds after Unix epoch)
                    startDate.epochSeconds.toDouble() - 978307200.0
                } else {
                    // Event not found, open calendar to today
                    Logger.d(TAG, "Event not found: $nativeEventId, opening to today")
                    NSDate().timeIntervalSinceReferenceDate
                }

                // calshow: URL scheme opens Calendar app
                // The timestamp parameter navigates to that date
                val urlString = "calshow:$timestamp"
                val url = NSURL.URLWithString(urlString)

                if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>()) { success ->
                        if (success) {
                            Logger.d(TAG, "Opened calendar to event date: $nativeEventId")
                        } else {
                            Logger.e(TAG, "Failed to open Calendar app")
                        }
                    }
                } else {
                    Logger.e(TAG, "Cannot open Calendar app URL")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to open calendar: ${e.message}")
            }
        }
    }
}
