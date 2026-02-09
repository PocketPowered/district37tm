package com.district37.toastmasters.features.create.viewall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.PerformerDetailNavigationArgs
import com.district37.toastmasters.common.pagination.PaginatedListConfig
import com.district37.toastmasters.common.pagination.PaginatedListScreen
import com.district37.toastmasters.common.pagination.PaginatedListViewModel
import com.district37.toastmasters.components.performers.PerformerCard
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.Performer
import org.koin.compose.koinInject

/**
 * Screen that displays all editable performers (owned + collaborated) for the current user
 */
@Composable
fun MyEditablePerformersScreen(
    onBackClick: () -> Unit = {}
) {
    val createHubRepository: CreateHubRepository = koinInject()
    val navController = LocalNavController.current

    // Create the pagination config - start with empty initial items
    val config = remember {
        PaginatedListConfig<Performer>(
            title = "My Performers",
            subtitle = "Performers you own or collaborate on",
            initialItems = emptyList(),
            totalCount = 0,
            initialCursor = null,
            emptyMessage = "No performers yet. Create your first performer or get invited to collaborate!"
        )
    }

    // Create the data source
    val dataSource = remember {
        MyEditablePerformersPaginationDataSource(createHubRepository)
    }

    // Create the view model
    val viewModel = remember(config, dataSource) {
        PaginatedListViewModel(config, dataSource)
    }

    // Render the generic paginated list screen
    PaginatedListScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        itemContent = { performer, _ ->
            PerformerCard(
                performer = performer,
                onClick = { navController.navigate(PerformerDetailNavigationArgs(performer.id)) }
            )
        }
    )
}
