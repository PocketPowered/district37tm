package com.district37.toastmasters.common.pagination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.ErrorCard
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets

/**
 * Generic paginated list screen that can display any type of items
 *
 * @param T The type of items to display
 * @param viewModel The ViewModel managing the list state
 * @param onBackClick Callback when back button is clicked
 * @param itemContent Composable lambda for rendering each item with its click handler
 */
@Composable
fun <T> PaginatedListScreen(
    viewModel: PaginatedListViewModel<T>,
    onBackClick: () -> Unit = {},
    itemContent: @Composable (item: T, onClick: () -> Unit) -> Unit
) {
    // Configure the root TopAppBar with dynamic title from ViewModel
    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(
            title = viewModel.title,
            subtitle = viewModel.subtitle
        ),
        onBackClick = onBackClick
    )

    val items by viewModel.items.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreItems by viewModel.hasMoreItems.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    val topBarInsets = LocalTopAppBarInsets.current

    // Infinite scroll: Load more when user scrolls near the end
    LaunchedEffect(listState, hasMoreItems, isLoadingMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (items.isEmpty() || (lastVisibleItemIndex != null &&
                            lastVisibleItemIndex >= items.size - 3) &&
                    hasMoreItems &&
                    !isLoadingMore
                ) {
                    viewModel.loadMore()
                }
            }
    }

    when {
        items.isEmpty() && isLoadingMore -> {
            // Initial loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        items.isEmpty() -> {
            // Empty state (after loading completed)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = viewModel.emptyMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topBarInsets.recommendedContentPadding, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { item -> item.hashCode() }) { item ->
                    itemContent(item) { /* onClick - passed to itemContent */ }
                }

                // Error display
                error?.let { errorMessage ->
                    item {
                        ErrorCard(
                            message = errorMessage,
                            onRetry = { viewModel.loadMore() }
                        )
                    }
                }

                // Loading indicator at the bottom (seamless infinite scroll)
                if (isLoadingMore && hasMoreItems) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
