package com.district37.toastmasters.infra

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of PinnedEventManager using SharedPreferences.
 * Stores the user's pinned event ID for quick access on app launch.
 */
private class PinnedEventManagerImpl(private val context: Context) : PinnedEventManager {

    private companion object {
        const val PREFS_NAME = "eventsidekick_pinned_event"
        const val KEY_PINNED_EVENT_ID = "pinned_event_id"
        const val NO_PINNED_EVENT = -1
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun getPinnedEventId(): Int? {
        return withContext(Dispatchers.IO) {
            val id = sharedPreferences.getInt(KEY_PINNED_EVENT_ID, NO_PINNED_EVENT)
            if (id == NO_PINNED_EVENT) null else id
        }
    }

    override suspend fun pinEvent(eventId: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putInt(KEY_PINNED_EVENT_ID, eventId)
                .apply()
        }
    }

    override suspend fun unpinEvent() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(KEY_PINNED_EVENT_ID)
                .apply()
        }
    }

    override suspend fun isEventPinned(eventId: Int): Boolean {
        return getPinnedEventId() == eventId
    }
}

actual fun createPinnedEventManager(): PinnedEventManager {
    return object : KoinComponent {
        val context: Context by inject()
    }.let { PinnedEventManagerImpl(it.context) }
}
