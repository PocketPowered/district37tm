package com.district37.toastmasters.components.schedules

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.LocalizedTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OverlapCalculatorTest {

    private val testTimezone = "America/New_York"

    private fun createLocalizedTime(hour: Int, minute: Int = 0): LocalizedTime {
        val localDateTime = LocalDateTime(2025, 1, 15, hour, minute)
        val tz = TimeZone.of(testTimezone)
        val instant = localDateTime.toInstant(tz)
        return LocalizedTime(instant, testTimezone)
    }

    private fun createAgendaItem(
        id: Int,
        startHour: Int,
        startMinute: Int = 0,
        endHour: Int,
        endMinute: Int = 0,
        title: String = "Event $id"
    ): AgendaItem {
        return AgendaItem(
            id = id,
            eventId = 1,
            title = title,
            description = null,
            startTime = createLocalizedTime(startHour, startMinute),
            endTime = createLocalizedTime(endHour, endMinute),
            performerIds = emptyList(),
            locationId = null,
            tag = null
        )
    }

    @Test
    fun `empty items returns default business hours layout`() {
        val result = OverlapCalculator.calculateLayout(emptyList())

        assertEquals(8, result.startHour)
        assertEquals(18, result.endHour)
        assertTrue(result.positionedEvents.isEmpty())
    }

    @Test
    fun `single event returns single column layout`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 11)
        )

        val result = OverlapCalculator.calculateLayout(items)

        assertEquals(1, result.positionedEvents.size)
        val event = result.positionedEvents.first()
        assertEquals(0, event.column)
        assertEquals(1, event.totalColumns)
    }

    @Test
    fun `non-overlapping events use same column`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 11),
            createAgendaItem(id = 2, startHour = 12, endHour = 13)
        )

        val result = OverlapCalculator.calculateLayout(items)

        assertEquals(2, result.positionedEvents.size)
        // Both should be in column 0 since they don't overlap
        assertTrue(result.positionedEvents.all { it.column == 0 })
    }

    @Test
    fun `overlapping events get different columns`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 12),
            createAgendaItem(id = 2, startHour = 11, endHour = 13)
        )

        val result = OverlapCalculator.calculateLayout(items)

        assertEquals(2, result.positionedEvents.size)
        val columns = result.positionedEvents.map { it.column }.toSet()
        // They should be in different columns
        assertEquals(2, columns.size)
    }

    @Test
    fun `time range includes 1 hour padding before first event`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 11)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // First event starts at 10, so grid should start at 9
        assertEquals(9, result.startHour)
    }

    @Test
    fun `time range includes 1 hour padding after last event`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 14, endHour = 16)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // Last event ends at 16, so grid should end at 17
        assertEquals(17, result.endHour)
    }

    @Test
    fun `calculates correct top offset in minutes`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, startMinute = 30, endHour = 11, endMinute = 30)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // Grid starts at 9 (hour 10 - 1 padding)
        // Event starts at 10:30, which is 90 minutes from 9:00
        val event = result.positionedEvents.first()
        assertEquals(90, event.topOffsetMinutes)
    }

    @Test
    fun `calculates correct height in minutes`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 11, endMinute = 30)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // Event is 1.5 hours = 90 minutes
        val event = result.positionedEvents.first()
        assertEquals(90, event.heightMinutes)
    }

    @Test
    fun `three overlapping events get different columns`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 13),
            createAgendaItem(id = 2, startHour = 11, endHour = 14),
            createAgendaItem(id = 3, startHour = 12, endHour = 15)
        )

        val result = OverlapCalculator.calculateLayout(items)

        assertEquals(3, result.positionedEvents.size)
        val columns = result.positionedEvents.map { it.column }.toSet()
        // All three should be in different columns
        assertEquals(3, columns.size)
    }

    @Test
    fun `max 4 columns for many overlapping events`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 14),
            createAgendaItem(id = 2, startHour = 10, endHour = 14),
            createAgendaItem(id = 3, startHour = 10, endHour = 14),
            createAgendaItem(id = 4, startHour = 10, endHour = 14),
            createAgendaItem(id = 5, startHour = 10, endHour = 14)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // All events should have column index <= 3 (max 4 columns)
        assertTrue(result.positionedEvents.all { it.column <= 3 })
    }

    @Test
    fun `items without valid times are filtered out`() {
        val validItem = createAgendaItem(id = 1, startHour = 10, endHour = 11)
        val invalidItem = AgendaItem(
            id = 2,
            eventId = 1,
            title = "Invalid",
            description = null,
            startTime = null,
            endTime = null,
            performerIds = emptyList(),
            locationId = null,
            tag = null
        )

        val result = OverlapCalculator.calculateLayout(listOf(validItem, invalidItem))

        assertEquals(1, result.positionedEvents.size)
        assertEquals(1, result.positionedEvents.first().item.id)
    }

    @Test
    fun `totalHours calculated correctly from start and end hour`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 14)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // Start hour is 9 (10-1), end hour is 15 (14+1)
        assertEquals(6, result.totalHours)
    }

    @Test
    fun `totalMinutes calculated correctly`() {
        val items = listOf(
            createAgendaItem(id = 1, startHour = 10, endHour = 14)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // 6 hours = 360 minutes
        assertEquals(360, result.totalMinutes)
    }

    @Test
    fun `events are sorted by start time`() {
        val items = listOf(
            createAgendaItem(id = 2, startHour = 14, endHour = 15),
            createAgendaItem(id = 1, startHour = 10, endHour = 11),
            createAgendaItem(id = 3, startHour = 12, endHour = 13)
        )

        val result = OverlapCalculator.calculateLayout(items)

        // Events should be processed in order of start time
        val ids = result.positionedEvents.map { it.item.id }
        assertEquals(listOf(1, 3, 2), ids)
    }
}
