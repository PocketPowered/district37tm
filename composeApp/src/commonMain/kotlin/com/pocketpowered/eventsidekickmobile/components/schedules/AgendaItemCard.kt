package com.district37.toastmasters.components.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import com.district37.toastmasters.components.common.Chip
import com.district37.toastmasters.components.common.shimmerEffect
import com.district37.toastmasters.components.engagement.CompactEngagementInfo
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemTagStyle
import com.district37.toastmasters.util.DateTimeFormatter
import com.district37.toastmasters.util.rememberHapticFeedback

/**
 * Reusable agenda item view component
 *
 * @param item The agenda item to display
 * @param showDate Whether to show the date above the time (useful for multi-day events)
 * @param showLocation Whether to show the location chip
 * @param showPerformers Whether to show performer chips
 * @param maxNamedPerformers Maximum number of performer names to show before collapsing
 * @param onClick Callback when the agenda item is clicked
 */
/**
 * Applies visual styling based on the AgendaItemTagStyle (non-animated styles only)
 */
@Composable
private fun Modifier.applyTagStyle(style: AgendaItemTagStyle): Modifier {
    return when (style) {
        is AgendaItemTagStyle.None -> this
        is AgendaItemTagStyle.GradientBackground -> this.background(style.brush)
        is AgendaItemTagStyle.GradientBorder -> this.border(
            width = style.borderWidth.dp,
            brush = style.brush,
            shape = MaterialTheme.shapes.small
        )
        is AgendaItemTagStyle.AccentIndicator -> this.drawBehind {
            drawRect(
                color = style.color,
                topLeft = Offset.Zero,
                size = size.copy(width = style.width)
            )
        }
        is AgendaItemTagStyle.Shimmer -> this // Shimmer handled separately as overlay
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgendaItemCard(
    item: AgendaItem,
    showDate: Boolean = true,
    showLocation: Boolean = true,
    showPerformers: Boolean = true,
    maxNamedPerformers: Int = 2,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val tagStyle = AgendaItemTagStyle.forTag(item.tag)
    val haptic = rememberHapticFeedback()

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .applyTagStyle(tagStyle)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performLongPress()
                        onLongPress()
                    }
                )
                .padding(
                    // Add padding for content spacing and to prevent clipping with border styles
                    all = if (tagStyle is AgendaItemTagStyle.GradientBorder) 24.dp else 16.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Date, Time and duration on the left - more prominent
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.width(75.dp)
        ) {
            // Date (when showDate is true) - styled similar to time
            if (showDate && item.startTime != null) {
                Text(
                    text = DateTimeFormatter.formatSimplifiedDate(item.startTime),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Time
            if (item.startTime != null) {
                Text(
                    text = DateTimeFormatter.formatTime(item.startTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Duration badge
            DateTimeFormatter.formatDuration(item.startTime, item.endTime)?.let { duration ->
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Content on the right
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title row with engagement info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                // Engagement info
                item.userEngagement?.let { engagement ->
                    CompactEngagementInfo(engagement = engagement)
                }
            }

            // Description
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Performers and Location chips (conditionally shown)
            if (showLocation || showPerformers) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (showLocation) {
                        item.location?.let { location ->
                            Chip(text = "📍 ${location.name}")
                        }
                    }

                    if (showPerformers) {
                        item.performers.take(maxNamedPerformers).forEach { performer ->
                            Chip(text = "👤 ${performer.name}")
                        }

                        if (item.performers.size > maxNamedPerformers) {
                            val remaining = item.performers.size - maxNamedPerformers
                            Chip(text = "+$remaining others")
                        }
                    }
                }
            }
        }
        }
        // Shimmer overlay for highlighted items
        if (tagStyle is AgendaItemTagStyle.Shimmer) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect(tagStyle.colors, durationMillis = 1500)
            )
        }
    }
}
