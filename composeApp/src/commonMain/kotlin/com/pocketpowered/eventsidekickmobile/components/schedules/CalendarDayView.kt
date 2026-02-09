package com.district37.toastmasters.components.schedules

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.shimmerEffect
import com.district37.toastmasters.components.engagement.RsvpQuickActionBottomSheet
import com.district37.toastmasters.components.engagement.rememberRsvpQuickActionState
import com.district37.toastmasters.components.engagement.scaleBounceOnChange
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemTagStyle
import com.district37.toastmasters.util.rememberHapticFeedback
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * A calendar day view that displays agenda items with:
 * - Hourly time markers on the left
 * - Events positioned absolutely based on their start time
 * - Overlapping events shown side-by-side (max 4 columns)
 *
 * @param items List of agenda items for the day
 * @param dpPerMinute Height ratio for proportional sizing
 * @param minEventHeightDp Minimum height for event blocks
 * @param onItemClick Callback when an event is clicked
 */
@Composable
fun CalendarDayView(
    items: List<AgendaItem>,
    dpPerMinute: Float = 1.5f,
    minEventHeightDp: Float = 92f,
    onItemClick: (AgendaItem) -> Unit = {}
) {
    val rsvpSheetState = rememberRsvpQuickActionState()
    val engagementManager: EngagementManager = koinInject()
    val scope = rememberCoroutineScope()

    // Reactive items list that updates when engagement changes
    var reactiveItems by remember { mutableStateOf(items) }

    // Sync with parameter changes
    LaunchedEffect(items) {
        reactiveItems = items
    }

    // Listen to engagement updates and update items reactively
    LaunchedEffect(Unit) {
        engagementManager.engagementUpdates.collect { event ->
            if (event.key.entityType == EntityType.AGENDAITEM) {
                reactiveItems = reactiveItems.map { item ->
                    if (item.id == event.key.entityId) {
                        item.copy(userEngagement = event.engagement)
                    } else {
                        item
                    }
                }
            }
        }
    }

    // Calculate layout with overlap handling using reactive items
    val layout = remember(reactiveItems) {
        OverlapCalculator.calculateLayout(reactiveItems)
    }

    val totalGridHeight = (layout.totalMinutes * dpPerMinute).dp

    // Note: No verticalScroll here - this component is meant to be placed inside
    // a LazyColumn or other scrollable container. The height is fixed based on
    // the time range of the events.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalGridHeight)
        ) {
            // Time labels column
            TimeLabelsColumn(
                startHour = layout.startHour,
                endHour = layout.endHour,
                dpPerMinute = dpPerMinute,
                modifier = Modifier.width(50.dp)
            )

            // Events container with grid lines
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val containerWidth = maxWidth

                // Grid lines background
                TimeGridLines(
                    startHour = layout.startHour,
                    endHour = layout.endHour,
                    dpPerMinute = dpPerMinute
                )

                // Event blocks
                layout.positionedEvents.forEach { positionedEvent ->
                    EventBlock(
                        positionedEvent = positionedEvent,
                        dpPerMinute = dpPerMinute,
                        minHeightDp = minEventHeightDp,
                        containerWidth = containerWidth,
                        onClick = { onItemClick(positionedEvent.item) },
                        onLongPress = { rsvpSheetState.show(positionedEvent.item) }
                    )
                }
            }
        }
    }

    // RSVP Quick Action Bottom Sheet
    RsvpQuickActionBottomSheet(
        state = rsvpSheetState,
        onStatusSelected = { status ->
            rsvpSheetState.targetAgendaItem?.let { item ->
                scope.launch {
                    engagementManager.setStatus(
                        entityType = EntityType.AGENDAITEM,
                        entityId = item.id,
                        status = status
                    )
                }
            }
        }
    )
}

/**
 * Column showing hourly time labels.
 */
@Composable
private fun TimeLabelsColumn(
    startHour: Int,
    endHour: Int,
    dpPerMinute: Float,
    modifier: Modifier = Modifier
) {
    val hourHeight = (60 * dpPerMinute).dp

    Box(modifier = modifier.fillMaxHeight()) {
        for (hour in startHour until endHour) {
            val topOffset = ((hour - startHour) * 60 * dpPerMinute).dp

            Text(
                text = formatHour(hour),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .offset(y = topOffset)
                    .padding(end = 8.dp, top = 0.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

/**
 * Horizontal grid lines at each hour.
 */
@Composable
private fun TimeGridLines(
    startHour: Int,
    endHour: Int,
    dpPerMinute: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (hour in startHour until endHour) {
            val topOffset = ((hour - startHour) * 60 * dpPerMinute).dp

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = topOffset),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
        }
    }
}

/**
 * A single event block positioned absolutely in the calendar.
 */
@Composable
private fun EventBlock(
    positionedEvent: PositionedEvent,
    dpPerMinute: Float,
    minHeightDp: Float,
    containerWidth: Dp,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    val haptic = rememberHapticFeedback()
    val item = positionedEvent.item
    val topOffset = (positionedEvent.topOffsetMinutes * dpPerMinute).dp
    val calculatedHeight = (positionedEvent.heightMinutes * dpPerMinute)
    val height = calculatedHeight.coerceAtLeast(minHeightDp).dp
    val cardShape = RoundedCornerShape(6.dp)
    // Calculate width based on overlap
    val gapBetweenColumns = 2.dp
    val totalGaps = if (positionedEvent.totalColumns > 1) gapBetweenColumns else 0.dp
    val availableWidth = containerWidth - totalGaps - 8.dp // 8dp right padding
    val columnWidth = availableWidth / positionedEvent.totalColumns

    // Calculate horizontal offset
    val xOffset = (columnWidth + gapBetweenColumns) * positionedEvent.column

    // Determine content density based on column count
    val columnCount = positionedEvent.totalColumns
    val isCondensed = columnCount > 1      // 2 columns
    val isVeryCondensed = columnCount >= 3 // 3 columns
    val isMinimal = columnCount >= 4       // 4 columns

    // Get colors based on engagement status
    val (backgroundColor, contentColor) = CalendarBlockColors.getColors(item.userEngagement?.status)
    val tagStyle = AgendaItemTagStyle.forTag(item.tag)

    // Padding based on density
    val horizontalPadding = when {
        isMinimal || isVeryCondensed -> 4.dp
        isCondensed -> 6.dp
        else -> 10.dp
    }
    val verticalPadding = when {
        isMinimal || isVeryCondensed -> 2.dp
        isCondensed -> 4.dp
        else -> 6.dp
    }

    Surface(
        modifier = Modifier
            .offset(x = xOffset, y = topOffset)
            .width(columnWidth)
            .height(height)
            .scaleBounceOnChange(key = item.userEngagement?.status)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performLongPress()
                    onLongPress()
                }
            ),
        shape = cardShape,
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                // Duration - always show
                Text(
                    text = formatDuration(positionedEvent.heightMinutes),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )

                // Title - always show, adjust lines based on density
                val titleMaxLines = when {
                    isMinimal -> 2
                    isVeryCondensed -> 2
                    isCondensed -> 2
                    else -> 3
                }
                Text(
                    text = item.title,
                    style = when {
                        isMinimal || isVeryCondensed -> MaterialTheme.typography.bodySmall
                        isCondensed -> MaterialTheme.typography.bodySmall
                        else -> MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )

                // Location - show if enough height and not minimal
                if (!isMinimal && calculatedHeight >= 80f) {
                    item.location?.let { location ->
                        Text(
                            text = "\uD83D\uDCCD ${location.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = if (isVeryCondensed) 2.dp else 4.dp)
                        )
                    }
                }

                // Performers - always show if present
                if (item.performers.isNotEmpty()) {
                    val performerText = item.performers.take(2).joinToString(", ") { it.name }
                    Text(
                        text = "\uD83D\uDC64 $performerText",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (tagStyle is AgendaItemTagStyle.Shimmer) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(cardShape)
                        .shimmerEffect(tagStyle.colors),
                )
            }
        }
    }
}

/**
 * Format an hour (0-23) to a display string like "5 AM" or "2 PM".
 */
private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

/**
 * Format duration in minutes to a display string like "1h" or "30m" or "1h 30m".
 */
private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}
