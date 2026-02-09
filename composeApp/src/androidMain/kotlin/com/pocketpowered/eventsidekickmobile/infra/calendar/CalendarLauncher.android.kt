package com.district37.toastmasters.infra.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.district37.toastmasters.util.Logger

/**
 * Android implementation of CalendarLauncher.
 * Opens the native calendar app to view a specific event.
 */
actual class CalendarLauncher(private val context: Context) {
    private val TAG = "CalendarLauncher"

    /**
     * Open the native calendar app to view the specified event.
     * Uses the Calendar content URI to open the event directly.
     */
    actual fun openCalendarEvent(nativeEventId: String) {
        try {
            val eventId = nativeEventId.toLongOrNull()
            if (eventId == null) {
                Logger.e(TAG, "Invalid event ID: $nativeEventId")
                return
            }

            val eventUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId
            )

            val intent = Intent(Intent.ACTION_VIEW, eventUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
                Logger.d(TAG, "Opened calendar event: $nativeEventId")
            } catch (e: Exception) {
                // Fallback: open calendar app to today's date
                Logger.d(TAG, "Failed to open specific event, opening calendar app")
                val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(calendarIntent)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open calendar: ${e.message}")
        }
    }
}
