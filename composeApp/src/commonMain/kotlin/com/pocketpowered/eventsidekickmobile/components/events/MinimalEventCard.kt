package com.district37.toastmasters.components.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.UserEngagement
import com.district37.toastmasters.util.DateTimeFormatter
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Minimal event card with horizontal layout for explore page carousels.
 * Shows square thumbnail, event name, and date/time with optional engagement band.
 *
 * @param event The event data to display
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier for the card
 */
@Composable
fun MinimalEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryImage = event.images.firstOrNull()
    val engagementColor = event.userEngagement?.let { getEngagementBandColor(it) }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = if (engagementColor != null) 6.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square thumbnail
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                ) {
                    if (primaryImage != null) {
                        CoilImage(
                            imageModel = { primaryImage.url },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = primaryImage.getCropAlignment(),
                                contentDescription = primaryImage.altText ?: event.name
                            )
                        )
                    } else {
                        // Placeholder when no image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Text content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Event name
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Date/time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatEventDateTime(event),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Engagement band on right edge
            if (engagementColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .background(engagementColor)
                )
            }
        }
    }
}

/**
 * Get the color for the engagement band indicator.
 * Returns null if no engagement should be shown.
 */
private fun getEngagementBandColor(engagement: UserEngagement): Color? {
    return when {
        engagement.status == UserEngagementStatus.GOING -> Color(0xFF4CAF50)      // Green
        engagement.status == UserEngagementStatus.NOT_GOING -> Color(0xFFF44336)  // Red
        engagement.status == UserEngagementStatus.UNDECIDED -> Color(0xFFFFC107)  // Yellow
        engagement.isSubscribed -> Color(0xFF2196F3)                               // Blue for subscribed only
        else -> null
    }
}

/**
 * Format date/time for compact display
 */
private fun formatEventDateTime(event: Event): String {
    return event.startDate?.let {
        DateTimeFormatter.formatDateWithTime(it)
    } ?: "Date TBA"
}
