package com.district37.toastmasters.features.create.viewall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.VenueDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.venues.VenueCard
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Venue
import org.koin.compose.koinInject

/**
 * Screen that displays all editable venues (owned + collaborated) for the current user
 */
@Composable
fun MyEditableVenuesScreen(
    onBackClick: () -> Unit = {}
) {
    val createHubRepository: CreateHubRepository = koinInject()
    val navController = LocalNavController.current

    // Create the pagination config - start with empty initial items
    val config = remember {
        PaginatedListConfig<Venue>(
            title = "My Venues",
            subtitle = "Venues you own or collaborate on",
            initialItems = emptyList(),
            totalCount = 0,
            initialCursor = null,
            emptyMessage = "No venues yet. Create your first venue or get invited to collaborate!"
        )
    }

    // Create the data source
    val dataSource = remember {
        MyEditableVenuesPaginationDataSource(createHubRepository)
    }

    // Create the view model
    val viewModel = remember(config, dataSource) {
        PaginatedListViewModel(config, dataSource)
    }

    // Render the generic paginated list screen
    PaginatedListScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        itemContent = { venue, _ ->
            VenueCard(
                venue = venue,
                onClick = { navController.navigate(VenueDetailNavigationArgs(venue.id)) }
            )
        }
    )
}
