package com.district37.toastmasters.components.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.navigation.DeeplinkDestination
import com.district37.toastmasters.navigation.DeeplinkHandler
import com.district37.toastmasters.util.DateTimeFormatter
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.koin.compose.koinInject

/**
 * Compact event card designed for carousel display.
 * Shows only the image, event name, and date - no description.
 * Default size is 160dp x 120dp for a smaller, more compact appearance.
 * Uses DeeplinkHandler for cross-tab navigation to event detail screen.
 *
 * @param event The event to display
 * @param modifier Optional modifier for the card
 * @param deeplinkHandler Handler for cross-tab navigation
 */
@Composable
fun CompactEventCard(
    event: Event,
    modifier: Modifier = Modifier,
    deeplinkHandler: DeeplinkHandler = koinInject()
) {
    val primaryImage = event.images.firstOrNull()

    Card(
        modifier = modifier,
        onClick = { deeplinkHandler.setDestination(DeeplinkDestination.Event(event.id.toString())) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        shape = RoundedCornerShape(16.dp)
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
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                }
            }

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 30f
                        )
                    )
            )

            // Content overlay at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Event Name
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                // Date
                Text(
                    text = formatCompactDateRange(event),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}


/**
 * Format date range for compact display
 */
private fun formatCompactDateRange(event: Event): String {
    val start = event.startDate
    return when {
        start != null -> DateTimeFormatter.formatDateWithTime(start)
        else -> "Date TBA"
    }
}
