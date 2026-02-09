package com.district37.toastmasters.components.schedules

import androidx.compose.ui.graphics.Color
import com.district37.toastmasters.graphql.type.UserEngagementStatus

/**
 * Color definitions for calendar block backgrounds based on engagement status.
 * These colors indicate the user's RSVP status for agenda items.
 */
object CalendarBlockColors {
    // Background colors - muted pastels for better readability
    val going = Color(0xFFC8E6C9)           // Muted light green
    val notGoing = Color(0xFFFFCDD2)        // Muted light red/pink
    val undecided = Color(0xFFFFE0B2)       // Soft peach/light orange (less glare than yellow)
    val neutral = Color(0xFFE8E8E8)         // Light gray (no status set)

    // Content colors (text/icons on top of backgrounds) - dark colors for contrast
    val onGoing = Color(0xFF1B5E20)         // Dark green
    val onNotGoing = Color(0xFFB71C1C)      // Dark red
    val onUndecided = Color(0xFFE65100)     // Dark orange
    val onNeutral = Color(0xFF424242)       // Dark gray text

    /**
     * Get background and content colors for a given engagement status.
     * @param status The user's engagement status, or null if not set
     * @return Pair of (backgroundColor, contentColor)
     */
    fun getColors(status: UserEngagementStatus?): Pair<Color, Color> {
        return when (status) {
            UserEngagementStatus.GOING -> going to onGoing
            UserEngagementStatus.NOT_GOING -> notGoing to onNotGoing
            UserEngagementStatus.UNDECIDED -> undecided to onUndecided
            else -> neutral to onNeutral
        }
    }
}
