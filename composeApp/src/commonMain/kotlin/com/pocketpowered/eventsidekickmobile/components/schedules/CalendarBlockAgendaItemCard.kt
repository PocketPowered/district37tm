package com.district37.toastmasters.components.schedules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.durationMinutes
import com.district37.toastmasters.util.DateTimeFormatter
import com.district37.toastmasters.util.rememberHapticFeedback

/**
 * Calendar-style block display for agenda items.
 *
 * Height is proportional to duration, with time displayed on the left
 * and a colored block on the right. Block color indicates engagement status
 * (Going=green, Not Going=red, Undecided=yellow, None=neutral).
 *
 * @param item The agenda item to display
 * @param dpPerMinute Height ratio - dp per minute of duration
 * @param minHeightDp Minimum block height to ensure content fits
 * @param maxHeightDp Maximum block height cap
 * @param showLocation Whether to show location chip
 * @param showPerformers Whether to show performer chips
 * @param maxNamedPerformers Maximum number of performer names before showing "+N others"
 * @param onClick Callback when the block is clicked
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarBlockAgendaItemCard(
    item: AgendaItem,
    dpPerMinute: Float = 1.5f,
    minHeightDp: Float = 120f,
    maxHeightDp: Float = 300f,
    showLocation: Boolean = true,
    showPerformers: Boolean = true,
    maxNamedPerformers: Int = 2,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val haptic = rememberHapticFeedback()

    // Calculate block height based on duration
    val blockHeight = calculateBlockHeight(
        durationMinutes = item.durationMinutes,
        dpPerMinute = dpPerMinute,
        minHeightDp = minHeightDp,
        maxHeightDp = maxHeightDp
    )

    // Get colors based on engagement status
    val (backgroundColor, contentColor) = CalendarBlockColors.getColors(item.userEngagement?.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(blockHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Time Column - aligned to top
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.End
        ) {
            item.startTime?.let { startTime ->
                Text(
                    text = DateTimeFormatter.formatTime(startTime),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Block Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performLongPress()
                        onLongPress()
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            color = backgroundColor,
            shadowElevation = 1.dp
        ) {
            BlockContent(
                item = item,
                contentColor = contentColor,
                showLocation = showLocation,
                showPerformers = showPerformers,
                maxNamedPerformers = maxNamedPerformers
            )
        }
    }
}

/**
 * Calculate the block height based on duration.
 *
 * @param durationMinutes Duration in minutes, or null for default
 * @param dpPerMinute Height ratio
 * @param minHeightDp Minimum height
 * @param maxHeightDp Maximum height
 * @return Calculated height in Dp
 */
private fun calculateBlockHeight(
    durationMinutes: Int?,
    dpPerMinute: Float,
    minHeightDp: Float,
    maxHeightDp: Float
): Dp {
    val duration = durationMinutes ?: 30  // Default 30 min for null duration
    val calculatedHeight = (duration * dpPerMinute).coerceIn(minHeightDp, maxHeightDp)
    return calculatedHeight.dp
}

/**
 * Content inside the calendar block.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlockContent(
    item: AgendaItem,
    contentColor: Color,
    showLocation: Boolean,
    showPerformers: Boolean,
    maxNamedPerformers: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Duration badge
        DateTimeFormatter.formatDuration(item.startTime, item.endTime)?.let { duration ->
            Text(
                text = duration,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }

        // Title
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Location and Performer chips
        if ((showLocation && item.location != null) || (showPerformers && item.performers.isNotEmpty())) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Location chip
                if (showLocation) {
                    item.location?.let { location ->
                        CompactChip(
                            text = location.name,
                            icon = "\uD83D\uDCCD", // Pin emoji
                            contentColor = contentColor
                        )
                    }
                }

                // Performer chips
                if (showPerformers && item.performers.isNotEmpty()) {
                    item.performers.take(maxNamedPerformers).forEach { performer ->
                        CompactChip(
                            text = performer.name,
                            icon = "\uD83D\uDC64", // Person emoji
                            contentColor = contentColor
                        )
                    }

                    // "+N others" chip
                    if (item.performers.size > maxNamedPerformers) {
                        val remaining = item.performers.size - maxNamedPerformers
                        CompactChip(
                            text = "+$remaining others",
                            contentColor = contentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact chip for use inside calendar blocks.
 * Uses a semi-transparent background to work on colored surfaces.
 */
@Composable
private fun CompactChip(
    text: String,
    icon: String? = null,
    contentColor: Color
) {
    Surface(
        color = contentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = if (icon != null) "$icon $text" else text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
