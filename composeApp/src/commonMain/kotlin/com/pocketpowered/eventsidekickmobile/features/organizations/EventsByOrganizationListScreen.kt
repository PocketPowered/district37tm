package com.district37.toastmasters.features.organizations

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.common.ErrorCard
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject

/**
 * Screen that displays all events for a specific organization using the generic paginated list
 *
 * @param organizationId ID of the organization to display events for
 * @param onBackClick Callback when back button is clicked
 */
@Composable
fun EventsByOrganization(
    organizationId: Int,
    onBackClick: () -> Unit = {}
) {
    val organizationRepository: OrganizationRepository = koinInject()
    val navController = LocalNavController.current

    // Load organization to get initial data
    var organizationState by remember { mutableStateOf<Resource<com.district37.toastmasters.models.Organization>>(Resource.Loading) }

    LaunchedEffect(organizationId) {
        organizationState = organizationRepository.getOrganization(organizationId)
    }

    when (val state = organizationState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val organization = state.data

            // Create the pagination config
            val config = PaginatedListConfig(
                title = organization.name,
                subtitle = "${organization.events.totalCount ?: organization.events.items.size} events",
                initialItems = organization.events.items,
                totalCount = organization.events.totalCount,
                initialCursor = organization.events.cursor,
                emptyMessage = "No events found for this organization"
            )

            // Create the data source
            val dataSource = remember(organizationId) {
                EventsByOrganizationPaginationDataSource(organizationRepository, organizationId)
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
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ErrorCard(
                    message = state.message ?: "Failed to load organization events",
                    onRetry = { organizationState = Resource.Loading }
                )
            }
        }
        else -> {
            // Handle any other state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
