package com.district37.toastmasters.components.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.components.common.EmptyStateCard
import com.district37.toastmasters.components.common.SectionHeader
import com.district37.toastmasters.components.common.ViewAllButton
import com.district37.toastmasters.models.Event

/**
 * Reusable component for displaying a list of events with pagination support
 * Shows initial events and a "View All" button when there are more events
 *
 * This is designed to be used within a LazyColumn's item scope
 * Automatically handles navigation to event details when an event is clicked
 *
 * @param title Section title (e.g., "Events at this Venue")
 * @param events Initial events to display
 * @param totalCount Total number of events available
 * @param hasMore Whether there are more events to load
 * @param onViewAllClick Callback when "View All" button is clicked
 * @param emptyMessage Message to show when there are no events
 */
fun LazyListScope.eventCardList(
    title: String,
    events: List<Event>,
    totalCount: Int,
    hasMore: Boolean,
    onViewAllClick: () -> Unit,
    emptyMessage: String = "No events found"
) {
    // Section Header with count badge
    item {
        SectionHeader(
            title = title,
            count = totalCount
        )
    }

    // Event cards in horizontal carousel
    if (events.isNotEmpty()) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events.size) { index ->
                    val navController = LocalNavController.current
                    val event = events[index]
                    EventCard(
                        event = event,
                        onClick = { navController.navigate(EventDetailNavigationArgs(event.id)) },
                        modifier = Modifier
                            .width(320.dp)
                    )
                }
            }
        }

        // View All button
        if (hasMore || totalCount > events.size) {
            item {
                ViewAllButton(
                    text = "View All $totalCount Events",
                    onClick = onViewAllClick
                )
            }
        }
    } else if (totalCount == 0) {
        // Empty state
        item {
            EmptyStateCard(message = emptyMessage)
        }
    }
}
