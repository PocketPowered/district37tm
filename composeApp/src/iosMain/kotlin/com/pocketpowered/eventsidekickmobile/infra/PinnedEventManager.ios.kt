package com.district37.toastmasters.infra

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of PinnedEventManager using UserDefaults.
 * Stores the user's pinned event ID for quick access on app launch.
 */
private class PinnedEventManagerImpl : PinnedEventManager {

    private companion object {
        const val KEY_PINNED_EVENT_ID = "pinned_event_id"
        const val NO_PINNED_EVENT = 0L // NSUserDefaults returns 0 for missing integer keys
    }

    override suspend fun getPinnedEventId(): Int? {
        val id = NSUserDefaults.standardUserDefaults.integerForKey(KEY_PINNED_EVENT_ID)
        return if (id == NO_PINNED_EVENT) null else id.toInt()
    }

    override suspend fun pinEvent(eventId: Int) {
        NSUserDefaults.standardUserDefaults.setInteger(eventId.toLong(), forKey = KEY_PINNED_EVENT_ID)
    }

    override suspend fun unpinEvent() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_PINNED_EVENT_ID)
    }

    override suspend fun isEventPinned(eventId: Int): Boolean {
        return getPinnedEventId() == eventId
    }
}

actual fun createPinnedEventManager(): PinnedEventManager {
    return PinnedEventManagerImpl()
}
