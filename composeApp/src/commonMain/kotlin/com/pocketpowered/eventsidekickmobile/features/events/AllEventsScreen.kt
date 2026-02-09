package com.district37.toastmasters.features.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.components.events.EventsPagedDataSource
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.Event
import org.koin.compose.koinInject

/**
 * Standalone screen showing all events with pagination support
 *
 * @param eventType Optional event type enum to filter by
 * @param onBackClick Callback when back button is clicked
 */
@Composable
fun AllEventsScreen(
    eventType: EventType? = null,
    onBackClick: () -> Unit
) {
    val eventRepository: EventRepository = koinInject()
    val navController = LocalNavController.current

    // Determine title based on event type
    val title = if (eventType != null) {
        "${eventType.rawValue.lowercase().replaceFirstChar { it.uppercase() }} events"
    } else {
        "All events"
    }

    // Create the pagination config
    val config = PaginatedListConfig<Event>(
        title = title,
        initialItems = emptyList(),
        totalCount = 100,
        initialCursor = null,
        emptyMessage = "No events found"
    )

    // Create the data source
    val dataSource = remember(eventType) {
        EventsPagedDataSource(eventRepository, eventType)
    }

    // Create the view model
    val viewModel = remember(config, dataSource) {
        PaginatedListViewModel(config, dataSource)
    }

    // Render the generic paginated list screen
    PaginatedListScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        itemContent = { event, _ ->
            EventCard(
                event = event,
                onClick = { navController.navigate(EventDetailNavigationArgs(event.id)) }
            )
        }
    )
}
