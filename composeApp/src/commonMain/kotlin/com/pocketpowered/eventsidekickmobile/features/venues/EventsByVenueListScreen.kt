package com.district37.toastmasters.features.venues

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
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject

/**
 * Screen that displays all events for a specific venue using the generic paginated list
 *
 * @param venueId ID of the venue to display events for
 * @param onBackClick Callback when back button is clicked
 */
@Composable
fun EventsByVenue(
    venueId: Int,
    onBackClick: () -> Unit = {}
) {
    val venueRepository: VenueRepository = koinInject()
    val navController = LocalNavController.current

    // Load venue to get initial data
    var venueState by remember { mutableStateOf<Resource<com.district37.toastmasters.models.Venue>>(Resource.Loading) }

    LaunchedEffect(venueId) {
        venueState = venueRepository.getVenue(venueId)
    }

    when (val state = venueState) {
        is Resource.Loading -> {
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val venue = state.data

            // Create the pagination config
            val config = PaginatedListConfig(
                title = venue.name,
                subtitle = "${venue.events.totalCount} events",
                initialItems = venue.events.items,
                totalCount = venue.events.totalCount,
                initialCursor = venue.events.cursor,
                emptyMessage = "No events found for this venue"
            )

            // Create the data source
            val dataSource = remember(venueId) {
                EventsByVenuePaginationDataSource(venueRepository, venueId)
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
                    message = state.message ?: "Failed to load venue events",
                    onRetry = { venueState = Resource.Loading }
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
