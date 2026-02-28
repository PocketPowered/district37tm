package com.district37.toastmasters.eventlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.EventTag
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private data class PositionedEventPreview(
    val event: EventPreview,
    val column: Int,
    val totalColumns: Int,
    val topOffsetMinutes: Int,
    val heightMinutes: Int
)

private data class CalendarAgendaLayout(
    val startHour: Int,
    val endHour: Int,
    val positionedEvents: List<PositionedEventPreview>
) {
    val totalHours: Int get() = endHour - startHour
    val totalMinutes: Int get() = totalHours * 60
}

private object CalendarAgendaLayoutCalculator {
    private const val MAX_COLUMNS = 4

    fun calculateLayout(events: List<EventPreview>): CalendarAgendaLayout {
        if (events.isEmpty()) {
            return CalendarAgendaLayout(
                startHour = 8,
                endHour = 18,
                positionedEvents = emptyList()
            )
        }

        val sortedEvents = events.sortedBy { it.time.startTime }
        val (startHour, endHour) = calculateTimeRange(sortedEvents)
        val positioned = calculatePositions(sortedEvents, startHour)

        return CalendarAgendaLayout(
            startHour = startHour,
            endHour = endHour,
            positionedEvents = positioned
        )
    }

    private fun calculateTimeRange(events: List<EventPreview>): Pair<Int, Int> {
        val firstStart = events.minOfOrNull { toHourMinute(it.time.startTime).first } ?: 8
        val lastEnd = events.maxOfOrNull {
            val (hour, minute) = toHourMinute(it.time.endTime)
            if (minute == 0) hour else hour + 1
        } ?: 18

        val startHour = (firstStart - 1).coerceIn(0, 23)
        val endHour = (lastEnd + 1).coerceIn(1, 24)
        return startHour to endHour
    }

    private fun calculatePositions(
        events: List<EventPreview>,
        gridStartHour: Int
    ): List<PositionedEventPreview> {
        val gridStartMinutes = gridStartHour * 60
        val activeEvents = mutableListOf<Pair<Int, Int>>()

        val prelim = events.map { event ->
            val startMinutes = toMinutesOfDay(event.time.startTime)
            val endMinutes = toMinutesOfDay(event.time.endTime)
            val duration = ((event.time.endTime - event.time.startTime) / 60_000L).toInt().coerceAtLeast(30)

            activeEvents.removeAll { (activeEnd, _) -> activeEnd <= startMinutes }

            val occupiedColumns = activeEvents.map { it.second }.toSet()
            val column = (0 until MAX_COLUMNS).firstOrNull { it !in occupiedColumns } ?: (MAX_COLUMNS - 1)

            activeEvents.add(endMinutes to column)

            PositionedEventPreview(
                event = event,
                column = column,
                totalColumns = 1,
                topOffsetMinutes = startMinutes - gridStartMinutes,
                heightMinutes = duration
            )
        }

        return updateOverlapGroups(prelim)
    }

    private fun updateOverlapGroups(events: List<PositionedEventPreview>): List<PositionedEventPreview> {
        if (events.isEmpty()) return events

        val result = events.toMutableList()
        for (i in result.indices) {
            val current = result[i]
            val currentStart = current.topOffsetMinutes
            val currentEnd = currentStart + current.heightMinutes

            var maxColumn = current.column
            for (j in result.indices) {
                if (i == j) continue
                val other = result[j]
                val otherStart = other.topOffsetMinutes
                val otherEnd = otherStart + other.heightMinutes
                if (currentStart < otherEnd && currentEnd > otherStart) {
                    maxColumn = maxOf(maxColumn, other.column)
                }
            }

            result[i] = current.copy(totalColumns = (maxColumn + 1).coerceIn(1, MAX_COLUMNS))
        }

        return result
    }

    private fun toHourMinute(epochMillis: Long): Pair<Int, Int> {
        val dateTime = Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return dateTime.hour to dateTime.minute
    }

    private fun toMinutesOfDay(epochMillis: Long): Int {
        val (hour, minute) = toHourMinute(epochMillis)
        return hour * 60 + minute
    }
}

@Composable
fun CalendarAgendaView(
    events: List<EventPreview>,
    onEventClick: (EventPreview) -> Unit,
    onFavoriteToggle: (EventPreview) -> Unit,
    modifier: Modifier = Modifier,
    dpPerMinute: Float = 1.35f,
    minEventHeightDp: Float = 76f
) {
    val layout = remember(events) {
        CalendarAgendaLayoutCalculator.calculateLayout(events)
    }
    val totalGridHeight = (layout.totalMinutes * dpPerMinute).dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalGridHeight)
        ) {
            TimeLabelsColumn(
                startHour = layout.startHour,
                endHour = layout.endHour,
                dpPerMinute = dpPerMinute,
                modifier = Modifier.width(52.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val containerWidth = maxWidth

                TimeGridLines(
                    startHour = layout.startHour,
                    endHour = layout.endHour,
                    dpPerMinute = dpPerMinute
                )

                layout.positionedEvents.forEach { event ->
                    CalendarEventBlock(
                        positionedEvent = event,
                        dpPerMinute = dpPerMinute,
                        minHeightDp = minEventHeightDp,
                        containerWidth = containerWidth,
                        onClick = { onEventClick(event.event) },
                        onFavoriteToggle = { onFavoriteToggle(event.event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeLabelsColumn(
    startHour: Int,
    endHour: Int,
    dpPerMinute: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxHeight()) {
        for (hour in startHour until endHour) {
            val topOffset = ((hour - startHour) * 60 * dpPerMinute).dp
            Text(
                text = formatHour(hour),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .offset(y = topOffset)
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun TimeGridLines(
    startHour: Int,
    endHour: Int,
    dpPerMinute: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (hour in startHour until endHour) {
            val topOffset = ((hour - startHour) * 60 * dpPerMinute).dp
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = topOffset),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun CalendarEventBlock(
    positionedEvent: PositionedEventPreview,
    dpPerMinute: Float,
    minHeightDp: Float,
    containerWidth: Dp,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val topOffset = (positionedEvent.topOffsetMinutes * dpPerMinute).dp
    val calculatedHeight = (positionedEvent.heightMinutes * dpPerMinute)
    val height = calculatedHeight.coerceAtLeast(minHeightDp).dp

    val gapBetweenColumns = 3.dp
    val totalGaps = gapBetweenColumns * (positionedEvent.totalColumns - 1)
    val availableWidth = containerWidth - totalGaps - 8.dp
    val columnWidth = availableWidth / positionedEvent.totalColumns
    val xOffset = (columnWidth + gapBetweenColumns) * positionedEvent.column

    val backgroundColor = when (positionedEvent.event.tag) {
        EventTag.HIGHLIGHTED -> MaterialTheme.colors.secondary.copy(alpha = 0.28f)
        EventTag.BREAK -> MaterialTheme.colors.primary.copy(alpha = 0.16f)
        EventTag.NORMAL -> MaterialTheme.colors.surface
    }

    Card(
        modifier = Modifier
            .offset(x = xOffset, y = topOffset)
            .width(columnWidth)
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = 3.dp,
        backgroundColor = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeRange(positionedEvent.event),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (positionedEvent.event.isFavorited) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (positionedEvent.event.isFavorited) {
                        "Unfavorite"
                    } else {
                        "Favorite"
                    },
                    tint = if (positionedEvent.event.isFavorited) {
                        androidx.compose.ui.graphics.Color.Red
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    },
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(16.dp)
                        .clickable { onFavoriteToggle() }
                )
            }

            Text(
                text = positionedEvent.event.title,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface,
                maxLines = if (positionedEvent.totalColumns >= 3) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (calculatedHeight >= 88f && positionedEvent.event.locationInfo.isNotBlank()) {
                Text(
                    text = positionedEvent.event.locationInfo,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val twelveHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "$twelveHour $period"
}

private fun formatTimeRange(event: EventPreview): String {
    val start = Instant.fromEpochMilliseconds(event.time.startTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val end = Instant.fromEpochMilliseconds(event.time.endTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    fun format(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val twelveHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$twelveHour:${minute.toString().padStart(2, '0')}$period"
    }

    return "${format(start.hour, start.minute)} - ${format(end.hour, end.minute)}"
}
