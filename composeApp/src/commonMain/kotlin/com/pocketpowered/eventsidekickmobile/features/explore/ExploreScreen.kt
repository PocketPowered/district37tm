package com.district37.toastmasters.features.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.components.activity.activityFeedSection
import com.district37.toastmasters.components.common.RefreshableContent
import com.district37.toastmasters.components.common.ViewAllCard
import com.district37.toastmasters.components.events.FeaturedEventCard
import com.district37.toastmasters.components.events.MediumEventCard
import com.district37.toastmasters.components.notifications.NotificationBellIcon
import com.district37.toastmasters.features.notifications.NotificationViewModel
import com.district37.toastmasters.models.CarouselDisplayFormat
import com.district37.toastmasters.models.EventCarousel
import com.district37.toastmasters.navigation.AllEventsRoute
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.navigation.NotificationCenterRoute
import com.district37.toastmasters.navigation.SearchRoute
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Explore screen showing event carousels
 */
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    notificationViewModel: NotificationViewModel = koinInject()
) {
    val navController = LocalNavController.current
    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated = authState is AuthState.Authenticated

    // Refresh notifications when user becomes authenticated
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            notificationViewModel.refresh()
        }
    }

    // Configure the root TopAppBar with split bubbles:
    // Primary (left): Avatar + Gather logo (handled by MainScaffold)
    // Secondary (right): Notifications + Search
    ConfigureTopAppBar(
        config = AppBarConfigs.rootScreen(
            actions = {
                // Only show notification bell when authenticated
                if (isAuthenticated) {
                    NotificationBellIcon(
                        onClick = { navController.navigate(NotificationCenterRoute) }
                    )
                }
                IconButton(onClick = { navController.navigate(SearchRoute) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            }
        )
    )

    val carouselsState by viewModel.carousels.collectAsState()
    val activityFeedState by viewModel.activityFeed.collectAsState()
    val isLoadingMoreActivity by viewModel.isLoadingMoreActivity.collectAsState()
    val topBarInsets = LocalTopAppBarInsets.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RefreshableContent(
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
        when (val state = carouselsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error loading explore page",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = state.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is Resource.Success -> {
                if (state.data.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Text(
                                text = "No carousels found",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = topBarInsets.recommendedContentPadding, bottom = 100.dp)
                    ) {
                        // Render all carousels in server-provided order
                        // Server controls order and displayFormat for each carousel
                        state.data.forEachIndexed { index, carousel ->
                            item(key = "carousel_$index") {
                                EventCarouselSection(
                                    carousel = carousel,
                                    onViewAllClick = {
                                        navController.navigate(
                                            AllEventsRoute(eventTypeName = carousel.eventType?.rawValue)
                                        )
                                    }
                                )
                                if (index < state.data.size - 1) {
                                    Spacer(modifier = Modifier.size(32.dp))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.size(32.dp))
                        }

                        // Activity Feed Section
                        activityFeedSection(
                            activityFeedState = activityFeedState,
                            isLoadingMore = isLoadingMoreActivity,
                            onLoadMore = { viewModel.loadMoreActivity() },
                            sectionHeaderText = "Friend Activity",
                            emptyStateTitle = "No friend activity yet",
                            emptyStateSubtitle = "When your friends RSVP or save events, you'll see their activity here"
                        )
                    }
                }
            }

            is Resource.NotLoading -> {
                // Initial state - show empty box for pull to refresh to work
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        }
    }
}

/**
 * A carousel section with a title and horizontally scrolling event cards.
 * Uses server-provided displayFormat to determine card styling:
 * - HERO: Large featured cards (400dp)
 * - MEDIUM: Standard cards (200dp)
 */
@Composable
private fun EventCarouselSection(
    carousel: EventCarousel,
    onViewAllClick: () -> Unit = {}
) {
    val navController = LocalNavController.current
    val isHero = carousel.displayFormat == CarouselDisplayFormat.HERO

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Text(
            text = carousel.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Horizontal scrolling carousel with individual cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(carousel.events) { event ->
                if (isHero) {
                    // Featured cards for HERO format
                    FeaturedEventCard(
                        event = event,
                        onClick = { navController.navigate(com.district37.toastmasters.navigation.EventDetailNavigationArgs(event.id)) },
                        modifier = Modifier.width(350.dp)
                    )
                } else {
                    // Medium cards for MEDIUM format
                    MediumEventCard(
                        event = event,
                        onClick = { navController.navigate(com.district37.toastmasters.navigation.EventDetailNavigationArgs(event.id)) },
                        modifier = Modifier.width(300.dp)
                    )
                }
            }

            // Add "View All" card if there are more events
            if (carousel.hasMore) {
                item {
                    ViewAllCard(
                        onClick = onViewAllClick,
                        text = carousel.title,
                        modifier = Modifier
                            .width(300.dp)
                            .height(if (isHero) 400.dp else 200.dp)
                    )
                }
            }
        }
    }
}
