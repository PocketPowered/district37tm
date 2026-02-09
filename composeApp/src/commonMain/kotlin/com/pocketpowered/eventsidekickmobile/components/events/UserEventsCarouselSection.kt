package com.district37.toastmasters.components.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.SectionHeader
import com.district37.toastmasters.components.common.ViewAllButton
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.util.Resource

/**
 * Reusable component for displaying a carousel of user events (saved or attending)
 * in the account profile screen.
 *
 * This is designed to be used within a LazyColumn's item scope.
 * Uses compact event cards showing only image, name, and date.
 * Navigation is handled internally via DeeplinkHandler.
 *
 * @param title Section title (e.g., "Saved Events", "Attending")
 * @param eventsState The resource state containing the events connection
 * @param onViewAllClick Callback when "View All" button is clicked
 * @param emptyMessage Message to show when there are no events
 */
fun LazyListScope.userEventsCarouselSection(
    title: String,
    eventsState: Resource<UserEventsConnection>,
    onViewAllClick: () -> Unit,
    emptyMessage: String = "No events"
) {
    when (eventsState) {
        is Resource.Loading -> {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionHeader(title = title)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        ShimmerEventCard(
                            modifier = Modifier
                                .width(160.dp)
                                .height(120.dp)
                        )
                    }
                }
            }
        }

        is Resource.Success -> {
            val connection = eventsState.data
            if (connection.events.isEmpty()) {
                // Show empty state
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SectionHeader(title = title)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Show carousel with events
                item {
                    SectionHeader(title = title)
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(connection.events.size) { index ->
                            val event = connection.events[index]
                            CompactEventCard(
                                event = event,
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(120.dp)
                            )
                        }
                    }
                }

                // View All button if there are more events
                if (connection.hasNextPage || connection.totalCount > connection.events.size) {
                    item {
                        ViewAllButton(
                            text = "View All ${connection.totalCount} Events",
                            onClick = onViewAllClick
                        )
                    }
                }
            }
        }

        is Resource.Error -> {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionHeader(title = title)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Failed to load events",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is Resource.NotLoading -> {
            // Initial state - show nothing
        }
    }
}
