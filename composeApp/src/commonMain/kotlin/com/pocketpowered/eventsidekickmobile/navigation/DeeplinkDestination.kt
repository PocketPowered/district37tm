package com.district37.toastmasters.navigation

/**
 * Sealed class representing different types of deeplink destinations in the app.
 * Each destination type encapsulates the data needed to navigate to that screen.
 *
 * Entity destinations accept a string identifier which can be either:
 * - A numeric ID (e.g., "123")
 * - A human-readable slug (e.g., "tech-conference-2026")
 */
sealed class DeeplinkDestination {
    /**
     * Navigate to event detail screen
     * @param identifier The event identifier (ID or slug)
     */
    data class Event(val identifier: String) : DeeplinkDestination() {
        val id: Int? get() = identifier.toIntOrNull()
        val slug: String? get() = if (id == null) identifier else null
    }

    /**
     * Navigate to venue detail screen
     * @param identifier The venue identifier (ID or slug)
     */
    data class Venue(val identifier: String) : DeeplinkDestination() {
        val id: Int? get() = identifier.toIntOrNull()
        val slug: String? get() = if (id == null) identifier else null
    }

    /**
     * Navigate to performer detail screen
     * @param identifier The performer identifier (ID or slug)
     */
    data class Performer(val identifier: String) : DeeplinkDestination() {
        val id: Int? get() = identifier.toIntOrNull()
        val slug: String? get() = if (id == null) identifier else null
    }

    /**
     * Navigate to agenda item detail screen
     * @param id The agenda item ID
     */
    data class AgendaItem(val id: Int) : DeeplinkDestination()

    /**
     * Navigate to location detail screen
     * @param identifier The location identifier (ID or slug)
     */
    data class Location(val identifier: String) : DeeplinkDestination() {
        val id: Int? get() = identifier.toIntOrNull()
        val slug: String? get() = if (id == null) identifier else null
    }

    /**
     * Navigate to organization detail screen
     * @param identifier The organization identifier (ID or slug)
     */
    data class Organization(val identifier: String) : DeeplinkDestination() {
        val id: Int? get() = identifier.toIntOrNull()
        val slug: String? get() = if (id == null) identifier else null
    }

    /**
     * Navigate to user profile screen
     * @param username The user's username
     */
    data class Profile(val username: String) : DeeplinkDestination()

    /**
     * OAuth callback - delegated to existing OAuth handler
     * @param uri The full OAuth callback URI
     */
    data class OAuthCallback(val uri: String) : DeeplinkDestination()

    /**
     * Navigate to chat screen (cross-tab navigation from Explore to Account)
     * @param conversationId The conversation ID
     * @param displayName Optional display name for the chat partner
     * @param avatarUrl Optional avatar URL for the chat partner
     */
    data class Chat(
        val conversationId: Int,
        val displayName: String? = null,
        val avatarUrl: String? = null
    ) : DeeplinkDestination()

    /**
     * Navigate to current user's profile with Requests tab selected.
     * Used when user taps a friend/collaboration request notification.
     */
    data object MyRequests : DeeplinkDestination()

    /**
     * Unknown or malformed deeplink
     */
    data object Unknown : DeeplinkDestination()
}
