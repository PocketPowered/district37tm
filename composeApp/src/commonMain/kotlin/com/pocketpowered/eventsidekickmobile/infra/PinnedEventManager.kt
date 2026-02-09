package com.district37.toastmasters.infra

/**
 * Manager for storing and retrieving the user's pinned event.
 * Users can only pin ONE event at a time - pinning a new event replaces the previous.
 * When an event is pinned, the app will navigate directly to it on cold start (after auth).
 */
interface PinnedEventManager {
    /**
     * Get the currently pinned event ID, or null if none.
     */
    suspend fun getPinnedEventId(): Int?

    /**
     * Pin an event. Replaces any previously pinned event.
     */
    suspend fun pinEvent(eventId: Int)

    /**
     * Unpin the current event (clears the pinned event).
     */
    suspend fun unpinEvent()

    /**
     * Check if a specific event is currently pinned.
     */
    suspend fun isEventPinned(eventId: Int): Boolean
}

/**
 * Platform-specific factory for creating PinnedEventManager.
 * Android uses SharedPreferences, iOS uses UserDefaults.
 */
expect fun createPinnedEventManager(): PinnedEventManager
