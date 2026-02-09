package com.district37.toastmasters.components.schedules

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.durationMinutes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Represents an agenda item with its calculated position in the calendar grid.
 *
 * @param item The original agenda item
 * @param column Which column this event is in (0 = left, 1 = right)
 * @param totalColumns Total number of columns in this overlap group (1 or 2)
 * @param topOffsetMinutes Minutes from the grid start time
 * @param heightMinutes Duration in minutes (for height calculation)
 */
data class PositionedEvent(
    val item: AgendaItem,
    val column: Int,
    val totalColumns: Int,
    val topOffsetMinutes: Int,
    val heightMinutes: Int
)

/**
 * Result of calculating the calendar grid layout.
 *
 * @param startHour The hour the grid starts (e.g., 6 for 6 AM)
 * @param endHour The hour the grid ends (e.g., 22 for 10 PM)
 * @param positionedEvents List of events with their calculated positions
 */
data class CalendarGridLayout(
    val startHour: Int,
    val endHour: Int,
    val positionedEvents: List<PositionedEvent>
) {
    val totalHours: Int get() = endHour - startHour
    val totalMinutes: Int get() = totalHours * 60
}

/**
 * Calculates the layout for a calendar day view, including:
 * - Dynamic time range (1 hour before first event to 1 hour after last)
 * - Overlap detection and column assignment (max 4 columns)
 */
object OverlapCalculator {

    private const val MAX_COLUMNS = 4

    /**
     * Calculate the grid layout for a list of agenda items.
     *
     * @param items List of agenda items for the day
     * @return CalendarGridLayout with positioned events
     */
    fun calculateLayout(items: List<AgendaItem>): CalendarGridLayout {
        if (items.isEmpty()) {
            // Default to business hours if no events
            return CalendarGridLayout(
                startHour = 8,
                endHour = 18,
                positionedEvents = emptyList()
            )
        }

        // Filter items with valid times and sort by start time
        val validItems = items.filter { it.startTime != null && it.endTime != null }
            .sortedBy { it.startTime?.instant }

        if (validItems.isEmpty()) {
            return CalendarGridLayout(
                startHour = 8,
                endHour = 18,
                positionedEvents = emptyList()
            )
        }

        // Calculate dynamic time range
        val (startHour, endHour) = calculateTimeRange(validItems)

        // Calculate positions with overlap handling
        val positionedEvents = calculateEventPositions(validItems, startHour)

        return CalendarGridLayout(
            startHour = startHour,
            endHour = endHour,
            positionedEvents = positionedEvents
        )
    }

    /**
     * Calculate the start and end hours for the grid.
     * Returns 1 hour before the first event and 1 hour after the last event.
     */
    private fun calculateTimeRange(items: List<AgendaItem>): Pair<Int, Int> {
        val firstStartHour = items.minOfOrNull { item ->
            val localizedTime = item.startTime ?: return@minOfOrNull 8
            val tz = localizedTime.timezone?.let {
                try { TimeZone.of(it) } catch (_: Exception) { TimeZone.currentSystemDefault() }
            } ?: TimeZone.currentSystemDefault()
            localizedTime.instant.toLocalDateTime(tz).hour
        } ?: 8

        val lastEndHour = items.maxOfOrNull { item ->
            val localizedTime = item.endTime ?: return@maxOfOrNull 18
            val tz = localizedTime.timezone?.let {
                try { TimeZone.of(it) } catch (_: Exception) { TimeZone.currentSystemDefault() }
            } ?: TimeZone.currentSystemDefault()
            val endTime = localizedTime.instant.toLocalDateTime(tz)
            // If event ends at exactly an hour, use that hour; otherwise round up
            if (endTime.minute == 0) endTime.hour else endTime.hour + 1
        } ?: 18

        // Add 1 hour padding on each side, clamped to 0-24
        val startHour = (firstStartHour - 1).coerceIn(0, 23)
        val endHour = (lastEndHour + 1).coerceIn(1, 24)

        return startHour to endHour
    }

    /**
     * Calculate event positions with overlap handling.
     * Uses a greedy algorithm to assign events to columns.
     */
    private fun calculateEventPositions(
        items: List<AgendaItem>,
        gridStartHour: Int
    ): List<PositionedEvent> {
        val gridStartMinutes = gridStartHour * 60

        // Track which columns are occupied at each time
        // Each entry is (endMinutes, columnIndex) for active events
        val activeEvents = mutableListOf<Pair<Int, Int>>()

        return items.map { item ->
            val startLocalizedTime = item.startTime!!
            val endLocalizedTime = item.endTime!!

            // Use the timezone from the LocalizedTime, falling back to system default
            val tz = startLocalizedTime.timezone?.let {
                try { TimeZone.of(it) } catch (_: Exception) { TimeZone.currentSystemDefault() }
            } ?: TimeZone.currentSystemDefault()

            val startLocal = startLocalizedTime.instant.toLocalDateTime(tz)
            val endLocal = endLocalizedTime.instant.toLocalDateTime(tz)

            val startMinutes = startLocal.hour * 60 + startLocal.minute
            val endMinutes = endLocal.hour * 60 + endLocal.minute
            val durationMinutes = item.durationMinutes ?: 30

            // Remove events that have ended
            activeEvents.removeAll { (eventEndMinutes, _) -> eventEndMinutes <= startMinutes }

            // Find available column
            val occupiedColumns = activeEvents.map { it.second }.toSet()
            val column = (0 until MAX_COLUMNS).firstOrNull { it !in occupiedColumns } ?: (MAX_COLUMNS - 1)

            // Check if there are any overlapping events
            val hasOverlap = activeEvents.isNotEmpty()
            val totalColumns = if (hasOverlap || occupiedColumns.isNotEmpty()) {
                // Count how many columns are used in this overlap group
                (occupiedColumns + column).size.coerceAtMost(MAX_COLUMNS)
            } else {
                1
            }

            // Add this event to active events
            activeEvents.add(endMinutes to column)

            // Update totalColumns for all overlapping events
            val finalTotalColumns = if (hasOverlap) MAX_COLUMNS else 1

            PositionedEvent(
                item = item,
                column = column,
                totalColumns = finalTotalColumns,
                topOffsetMinutes = startMinutes - gridStartMinutes,
                heightMinutes = durationMinutes
            )
        }.let { events ->
            // Second pass: update totalColumns for overlapping groups
            updateOverlapGroups(events)
        }
    }

    /**
     * Second pass to correctly set totalColumns for all events in overlap groups.
     */
    private fun updateOverlapGroups(events: List<PositionedEvent>): List<PositionedEvent> {
        if (events.isEmpty()) return events

        // Find all overlapping groups and update their totalColumns
        val result = events.toMutableList()

        for (i in result.indices) {
            val event = result[i]
            val eventStart = event.topOffsetMinutes
            val eventEnd = eventStart + event.heightMinutes

            // Check if this event overlaps with any other event
            var maxColumnInGroup = event.column
            for (j in result.indices) {
                if (i == j) continue
                val other = result[j]
                val otherStart = other.topOffsetMinutes
                val otherEnd = otherStart + other.heightMinutes

                // Check for overlap
                if (eventStart < otherEnd && eventEnd > otherStart) {
                    maxColumnInGroup = maxOf(maxColumnInGroup, other.column)
                }
            }

            // Update totalColumns if there's overlap
            if (maxColumnInGroup > 0) {
                result[i] = event.copy(totalColumns = maxColumnInGroup + 1)
            }
        }

        return result
    }

    /**
     * Check if two time ranges overlap.
     */
    private fun rangesOverlap(
        start1: Int, end1: Int,
        start2: Int, end2: Int
    ): Boolean {
        return start1 < end2 && end1 > start2
    }
}
