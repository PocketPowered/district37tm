package com.district37.toastmasters.features.performers

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject

/**
 * Screen that displays all events for a specific performer using the generic paginated list
 *
 * @param performerId ID of the performer to display events for
 * @param onBackClick Callback when back button is clicked
 */
@Composable
fun EventsByPerformer(
    performerId: Int,
    onBackClick: () -> Unit = {}
) {
    val performerRepository: PerformerRepository = koinInject()
    val navController = LocalNavController.current

    // Load performer to get initial data
    var performerState by remember { mutableStateOf<Resource<com.district37.toastmasters.models.Performer>>(Resource.Loading) }

    LaunchedEffect(performerId) {
        performerState = performerRepository.getPerformerDetails(performerId)
    }

    when (val state = performerState) {
        is Resource.Loading -> {
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val performer = state.data

            // Create the pagination config
            val config = PaginatedListConfig(
                title = performer.name,
                subtitle = "${performer.events.totalCount} events",
                initialItems = performer.events.items,
                totalCount = performer.events.totalCount,
                initialCursor = performer.events.cursor,
                emptyMessage = "No events found for this performer"
            )

            // Create the data source
            val dataSource = remember(performerId) {
                EventsByPerformerPaginationDataSource(performerRepository, performerId)
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
        is Resource.Error -> {
            // Show error state
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                com.district37.toastmasters.components.common.ErrorCard(
                    message = state.message ?: "Failed to load performer events",
                    onRetry = { performerState = Resource.Loading }
                )
            }
        }
        else -> {
            // Handle any other state
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
