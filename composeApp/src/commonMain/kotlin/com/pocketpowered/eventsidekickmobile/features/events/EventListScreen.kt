package com.district37.toastmasters.features.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.components.common.ViewAllCard
import com.district37.toastmasters.components.events.EventPreviewCard
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.navigation.AllEventsRoute
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.util.Resource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Event list screen showing event carousels
 */
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = koinViewModel()
) {
    // Configure the root TopAppBar for this screen
    ConfigureTopAppBar(config = AppBarConfigs.rootScreen())

    val eventsState by viewModel.events.collectAsState()
    val hasMoreEvents by viewModel.hasMoreEvents.collectAsState()
    val navController = LocalNavController.current

    when (val state = eventsState) {
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
                        text = "Error loading events",
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events found",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    EventCarouselSection(
                        title = "All Events",
                        events = state.data,
                        hasMoreEvents = hasMoreEvents,
                        onViewAllClick = {
                            navController.navigate(AllEventsRoute)
                        }
                    )
                }
            }
        }

        is Resource.NotLoading -> {
            // Initial state
        }
    }
}

/**
 * A carousel section with a title and horizontally scrolling event cards
 */
@Composable
private fun EventCarouselSection(
    title: String,
    events: List<Event>,
    hasMoreEvents: Boolean = false,
    onViewAllClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Horizontal scrolling carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                EventPreviewCard(
                    event = event,
                    modifier = Modifier
                        .width(320.dp)
                        .height(400.dp)
                )
            }

            // Add "View All" card if there are more events
            if (hasMoreEvents) {
                item {
                    ViewAllCard(
                        onClick = onViewAllClick,
                        modifier = Modifier
                            .width(320.dp)
                            .height(400.dp)
                    )
                }
            }
        }
    }
}
