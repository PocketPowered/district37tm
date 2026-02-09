package com.district37.toastmasters.features.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.components.organizations.OrganizationCarouselCard
import com.district37.toastmasters.components.performers.PerformerCard
import com.district37.toastmasters.components.venues.VenueCarouselCard
import com.district37.toastmasters.models.CreateHub
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.CreateEventRoute
import com.district37.toastmasters.navigation.CreatePerformerRoute
import com.district37.toastmasters.navigation.CreateOrganizationRoute
import com.district37.toastmasters.navigation.CreateVenueRoute
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.navigation.OrganizationDetailNavigationArgs
import com.district37.toastmasters.navigation.PerformerDetailNavigationArgs
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.navigation.VenueDetailNavigationArgs
import com.district37.toastmasters.navigation.MyEditableEventsRoute
import com.district37.toastmasters.navigation.MyEditableVenuesRoute
import com.district37.toastmasters.navigation.MyEditablePerformersRoute
import com.district37.toastmasters.navigation.ArchivedEventsRoute
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.auth.models.AuthState
import org.koin.compose.viewmodel.koinViewModel

/**
 * Hub screen for the Create tab showing quick actions and user's editable entities (owned + collaborated)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHubScreen(
    viewModel: CreateHubViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val navController = LocalNavController.current
    val createHubState by viewModel.createHubData.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn = authState is AuthState.Authenticated
    val createOptionsState = rememberCreateOptionsState()

    // Handle refresh signal from navigation
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    // Configure the top app bar
    ConfigureTopAppBar(
        config = AppBarConfigs.rootScreen(),
        onBackClick = null
    )

    val topBarInsets = LocalTopAppBarInsets.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { createOptionsState.show() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create"
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = topBarInsets.recommendedContentPadding,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // My Creations Section
                if (isLoggedIn) {
                    item {
                        Text(
                            text = "My Content",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        when (val state = createHubState) {
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is Resource.Error -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Failed to load your content",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = state.message ?: "Please try again",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            is Resource.Success -> {
                                ManagedEntitiesContent(
                                    createHub = state.data,
                                    onEventClick = { event ->
                                        navController.navigate(EventDetailNavigationArgs(event.id))
                                    },
                                    onVenueClick = { venue ->
                                        navController.navigate(VenueDetailNavigationArgs(venue.id))
                                    },
                                    onPerformerClick = { performer ->
                                        navController.navigate(
                                            PerformerDetailNavigationArgs(
                                                performer.id
                                            )
                                        )
                                    },
                                    onOrganizationClick = { organization ->
                                        navController.navigate(
                                            OrganizationDetailNavigationArgs(
                                                organization.id
                                            )
                                        )
                                    },
                                    onViewAllEvents = {
                                        navController.navigate(MyEditableEventsRoute)
                                    },
                                    onViewAllVenues = {
                                        navController.navigate(MyEditableVenuesRoute)
                                    },
                                    onViewAllPerformers = {
                                        navController.navigate(MyEditablePerformersRoute)
                                    }
                                )
                            }

                            is Resource.NotLoading -> {
                                // Initial state before data is loaded - show nothing
                            }
                        }
                    }

                    // Utilities Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Text(
                            text = "Utilities",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        UtilityButton(
                            icon = Icons.Filled.Archive,
                            label = "Archived",
                            onClick = { navController.navigate(ArchivedEventsRoute) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    // Login prompt for non-authenticated users
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Sign In to Manage Your Content",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sign in to see and manage events, venues, and performers you've created or collaborate on.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    CreateOptionsBottomSheet(
        state = createOptionsState,
        onCreateEvent = { navController.navigate(CreateEventRoute) },
        onCreateVenue = { navController.navigate(CreateVenueRoute) },
        onCreatePerformer = { navController.navigate(CreatePerformerRoute) },
        onCreateOrganization = { navController.navigate(CreateOrganizationRoute) }
    )
}

@Composable
private fun ManagedEntitiesContent(
    createHub: CreateHub,
    onEventClick: (Event) -> Unit,
    onVenueClick: (Venue) -> Unit,
    onPerformerClick: (Performer) -> Unit,
    onOrganizationClick: (Organization) -> Unit,
    onViewAllEvents: () -> Unit,
    onViewAllVenues: () -> Unit,
    onViewAllPerformers: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Events Section
        if (createHub.myEvents.items.isNotEmpty()) {
            EntitySectionHeader(
                title = "Events",
                count = createHub.myEvents.totalCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(createHub.myEvents.items) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        modifier = Modifier.width(300.dp).height(320.dp)
                    )
                }
                if (createHub.myEvents.hasMore) {
                    item {
                        ViewAllCard(
                            label = "View All Events",
                            onClick = onViewAllEvents,
                            modifier = Modifier.width(150.dp).height(320.dp)
                        )
                    }
                }
            }
        }

        // Venues Section
        if (createHub.myVenues.items.isNotEmpty()) {
            EntitySectionHeader(
                title = "Venues",
                count = createHub.myVenues.totalCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(createHub.myVenues.items) { venue ->
                    VenueCarouselCard(
                        venue = venue,
                        onClick = { onVenueClick(venue) },
                        modifier = Modifier.width(200.dp).height(240.dp)
                    )
                }
                if (createHub.myVenues.hasMore) {
                    item {
                        ViewAllCard(
                            label = "View All Venues",
                            onClick = onViewAllVenues,
                            modifier = Modifier.width(150.dp).height(240.dp)
                        )
                    }
                }
            }
        }

        // Performers Section
        if (createHub.myPerformers.items.isNotEmpty()) {
            EntitySectionHeader(
                title = "Performers",
                count = createHub.myPerformers.totalCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(createHub.myPerformers.items) { performer ->
                    PerformerCard(
                        performer = performer,
                        onClick = { onPerformerClick(performer) },
                        modifier = Modifier.width(180.dp).height(240.dp)
                    )
                }
                if (createHub.myPerformers.hasMore) {
                    item {
                        ViewAllCard(
                            label = "View All Performers",
                            onClick = onViewAllPerformers,
                            modifier = Modifier.width(150.dp).height(240.dp)
                        )
                    }
                }
            }
        }

        // Organizations Section
        if (createHub.myOrganizations.items.isNotEmpty()) {
            EntitySectionHeader(
                title = "Organizations",
                count = createHub.myOrganizations.totalCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(createHub.myOrganizations.items) { organization ->
                    OrganizationCarouselCard(
                        organization = organization,
                        onClick = { onOrganizationClick(organization) },
                        modifier = Modifier.width(200.dp).height(240.dp)
                    )
                }
            }
        }

        // Empty state when user has no content
        if (createHub.myEvents.items.isEmpty() && createHub.myVenues.items.isEmpty() && createHub.myPerformers.items.isEmpty() && createHub.myOrganizations.items.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No content yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first event, venue, or performer using the quick actions above, or get invited to collaborate on existing ones!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EntitySectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ViewAllCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UtilityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
