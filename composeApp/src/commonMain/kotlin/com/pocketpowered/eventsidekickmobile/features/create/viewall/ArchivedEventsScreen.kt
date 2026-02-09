package com.district37.toastmasters.features.create.viewall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Event
import org.koin.compose.koinInject

/**
 * Screen that displays all archived events for the current user
 */
@Composable
fun ArchivedEventsScreen(
    onBackClick: () -> Unit = {}
) {
    val createHubRepository: CreateHubRepository = koinInject()
    val navController = LocalNavController.current

    // Create the pagination config - start with empty initial items
    val config = remember {
        PaginatedListConfig<Event>(
            title = "Archived Events",
            subtitle = "Events you have archived",
            initialItems = emptyList(),
            totalCount = 0,
            initialCursor = null,
            emptyMessage = "No archived events yet."
        )
    }

    // Create the data source
    val dataSource = remember {
        ArchivedEventsPaginationDataSource(createHubRepository)
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
