package com.district37.toastmasters.features.venues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EditVenueRoute
import com.district37.toastmasters.navigation.VenueEventsNavigationArgs
import com.district37.toastmasters.components.common.DetailHeroCard
import com.district37.toastmasters.components.common.DetailScaffold
import com.district37.toastmasters.components.common.GetDirectionsButton
import com.district37.toastmasters.components.engagement.AdaptiveEngagementBar
import com.district37.toastmasters.components.engagement.LoginPromptDialog
import com.district37.toastmasters.components.events.eventCardList
import com.district37.toastmasters.components.maps.MapLocation
import com.district37.toastmasters.components.maps.MapView
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.models.Venue
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Venue detail screen that displays comprehensive information about a venue
 * including its details and upcoming events
 */
@Composable
fun VenueDetailScreen(
    venueId: Int,
    onBackClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val viewModel: VenueDetailViewModel = koinViewModel(key = venueId.toString()) { parametersOf(venueId) }
    val authViewModel: AuthViewModel = koinViewModel()
    val venueState by viewModel.item.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val showLoginPrompt by viewModel.authFeature.showLoginPrompt.collectAsState()
    val navController = LocalNavController.current

    // Handle refresh signal from navigation
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    // Login prompt dialog
    if (showLoginPrompt) {
        LoginPromptDialog(
            onDismiss = { viewModel.authFeature.dismissLoginPrompt() },
            onLoginClick = {
                viewModel.authFeature.dismissLoginPrompt()
                authViewModel.startGoogleLogin()
            }
        )
    }

    DetailScaffold(
        resourceState = venueState,
        onBackClick = onBackClick,
        onRetry = { viewModel.refresh() },
        errorMessage = "Failed to load venue",
        actions = {
            // Share button
            val shareManager: ShareManager = koinInject()
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generateVenueUrl(venueId, venueState.getOrNull()?.slug),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Venue"
                )
            }

            // Edit button - only show if user can edit
            if (permissions?.canEdit == true) {
                IconButton(onClick = { navController.navigate(EditVenueRoute(venueId)) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Venue"
                    )
                }
            }
        },
        primaryContent = { venue ->
            VenueInfoCard(venue = venue)

            // Engagement bar - only show when feature is initialized
            viewModel.engagementFeature?.let { feature ->
                val engagement by feature.engagement.collectAsState()

                AdaptiveEngagementBar(
                    entityType = EntityType.VENUE,
                    engagement = engagement,
                    isAuthenticated = viewModel.authFeature.isAuthenticated,
                    onEngagementClick = { feature.toggleSubscription() },
                    onStatusSelected = { status -> feature.setStatus(status) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        additionalContent = { venue ->
            // Address Section - show if any address info OR coordinates exist
            val hasAddressInfo = listOfNotNull(
                venue.address,
                venue.city,
                venue.state,
                venue.zipCode
            ).isNotEmpty()

            if (hasAddressInfo || (venue.latitude != null && venue.longitude != null)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Map and directions (only if coordinates available)
                        if (venue.latitude != null && venue.longitude != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            MapView(
                                location = MapLocation(
                                    latitude = venue.latitude,
                                    longitude = venue.longitude,
                                    title = venue.name
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GetDirectionsButton(
                                latitude = venue.latitude,
                                longitude = venue.longitude,
                                label = venue.name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Events at this Venue Section using reusable EventCardList
            eventCardList(
                title = "Events at this Venue",
                events = venue.events.items,
                totalCount = venue.events.totalCount,
                hasMore = venue.events.hasMore,
                onViewAllClick = { navController.navigate(VenueEventsNavigationArgs(venue.id)) },
                emptyMessage = "No upcoming events at this venue"
            )
        }
    )
}

/**
 * Venue information display using reusable DetailHeroCard
 */
@Composable
fun VenueInfoCard(venue: Venue) {
    // Build location subtitle
    val locationParts = listOfNotNull(venue.address, venue.city, venue.state, venue.zipCode)
    val subtitle = if (locationParts.isNotEmpty()) {
        "📍 " + locationParts.joinToString(", ")
    } else null

    DetailHeroCard(
        images = venue.images,
        title = venue.name,
        subtitle = subtitle,
        additionalContent = {
            // Capacity
            venue.capacity?.let { capacity ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 Capacity:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = capacity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    )
}
