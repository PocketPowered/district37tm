package com.district37.toastmasters.components.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.engagement.CompactEngagementInfo
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.util.DateTimeFormatter
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Featured event card (400dp tall) for top featured events in first carousel.
 * Full-bleed image with gradient overlay and rich content display.
 *
 * Design specs:
 * - Total height: 400dp
 * - Full-bleed image with gradient overlay
 * - Large typography for event name
 * - Type badge, date, location, engagement preview
 * - Corner radius: 16dp
 */
@Composable
fun FeaturedEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryImage = event.images.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed image
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
                // Placeholder background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.4f to Color.Black.copy(alpha = 0.2f),
                            0.7f to Color.Black.copy(alpha = 0.6f),
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            )

            // Content area - bottom section with rich details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Event name - prominent
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Date range
                Text(
                    text = DateTimeFormatter.formatDateRange(
                        startTime = event.startDate,
                        endTime = event.endDate,
                        showYear = true
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Location (venue) if available
                event.venue?.let { venue ->
                    Text(
                        text = venue.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Compact engagement info (if available)
                event.userEngagement?.let { engagement ->
                    CompactEngagementInfo(
                        engagement = engagement,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
