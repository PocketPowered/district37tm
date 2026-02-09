package com.district37.toastmasters.util

import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.AgendaItemTag
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Centralized utility object for formatting data for display
 *
 * Provides consistent formatting functions for:
 * - Enum to string conversions
 * - Date formatting
 * - User name initials
 * - And other display-related transformations
 *
 * This eliminates duplicate formatting logic across Screens and ViewModels
 */
object DisplayFormatters {

    /**
     * Format EventType enum to human-readable string
     *
     * @param eventType The GraphQL EventType enum
     * @return Human-readable event type string
     */
    fun formatEventType(eventType: EventType): String {
        return when (eventType) {
            EventType.CONFERENCE -> "Conference"
            EventType.CONCERT -> "Concert"
            EventType.FESTIVAL -> "Festival"
            EventType.WORKSHOP -> "Workshop"
            EventType.SYMPOSIUM -> "Symposium"
            EventType.FORUM -> "Forum"
            EventType.EXPO -> "Expo"
            EventType.PITCH_EVENT -> "Pitch Event"
            EventType.HACKATHON -> "Hackathon"
            else -> eventType.rawValue
        }
    }

    /**
     * Format AgendaItemTag enum to human-readable string
     *
     * @param tag The AgendaItemTag enum (nullable)
     * @return Human-readable tag name, or "None" if null
     */
    fun formatTagName(tag: AgendaItemTag?): String {
        return tag?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None"
    }

    /**
     * Format Instant to "Month Year" format for member since dates
     *
     * Example: "January 2023"
     *
     * @param instant The timestamp to format
     * @return Formatted string in "Month Year" format
     */
    fun formatMemberSince(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${localDateTime.year}"
    }

    /**
     * Extract initials from a user's display name
     *
     * Takes up to the first 2 words and uses their first characters
     * Returns "?" if no valid initials can be extracted
     *
     * Examples:
     * - "John Doe" -> "JD"
     * - "Jane" -> "J"
     * - "Mary Jane Watson" -> "MJ" (first 2 words only)
     * - "" -> "?"
     *
     * @param displayName The user's full name
     * @return User initials (1-2 characters) or "?"
     */
    fun formatUserInitials(displayName: String): String {
        return displayName
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    /**
     * Format a nullable string, returning a default if null or blank
     *
     * @param value The nullable string
     * @param default The default value to return (default: "—")
     * @return The value or default
     */
    fun formatNullableString(value: String?, default: String = "—"): String {
        return if (value.isNullOrBlank()) default else value
    }

    /**
     * Format a count with singular/plural labels
     *
     * Examples:
     * - formatCount(1, "item") -> "1 item"
     * - formatCount(5, "item") -> "5 items"
     * - formatCount(0, "event") -> "0 events"
     *
     * @param count The count value
     * @param singularLabel The singular form of the label
     * @param pluralLabel Optional plural form (defaults to singularLabel + "s")
     * @return Formatted count string
     */
    fun formatCount(count: Int, singularLabel: String, pluralLabel: String? = null): String {
        val label = if (count == 1) singularLabel else (pluralLabel ?: "${singularLabel}s")
        return "$count $label"
    }
}
