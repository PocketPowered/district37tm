package com.district37.toastmasters.features.performers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.components.common.DetailHeroCard
import com.district37.toastmasters.components.common.DetailScaffold
import com.district37.toastmasters.components.common.SectionHeader
import com.district37.toastmasters.components.engagement.AdaptiveEngagementBar
import com.district37.toastmasters.components.engagement.LoginPromptDialog
import com.district37.toastmasters.components.schedules.CompactAgendaItemCard
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.navigation.AgendaItemDetailNavigationArgs
import com.district37.toastmasters.navigation.EditPerformerRoute
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Performer detail screen displaying comprehensive information about a performer
 * including images, bio, and type
 *
 * @param performerId The ID of the performer to display
 * @param onBackClick Callback when back button is clicked
 * @param shouldRefresh Whether to refresh data when screen is shown
 * @param onRefreshHandled Callback when refresh has been handled
 */
@Composable
fun PerformerDetailScreen(
    performerId: Int,
    onBackClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val viewModel: PerformerDetailViewModel = koinViewModel(key = performerId.toString()) { parametersOf(performerId) }
    val authViewModel: AuthViewModel = koinViewModel()
    val performerState by viewModel.item.collectAsState()
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
        resourceState = performerState,
        onBackClick = onBackClick,
        onRetry = { viewModel.refresh() },
        errorMessage = "Failed to load performer",
        actions = {
            // Share button
            val shareManager: ShareManager = koinInject()
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generatePerformerUrl(performerId, performerState.getOrNull()?.slug),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Performer"
                )
            }

            // Edit button - only show if user can edit
            if (permissions?.canEdit == true) {
                IconButton(onClick = { navController.navigate(EditPerformerRoute(performerId)) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Performer"
                    )
                }
            }
        },
        primaryContent = { performer ->
            PerformerInfoCard(performer = performer)

            // Engagement bar - only show when feature is initialized
            viewModel.engagementFeature?.let { feature ->
                val engagement by feature.engagement.collectAsState()

                AdaptiveEngagementBar(
                    entityType = EntityType.PERFORMER,
                    engagement = engagement,
                    isAuthenticated = viewModel.authFeature.isAuthenticated,
                    onEngagementClick = { feature.toggleSubscription() },
                    onStatusSelected = { status -> feature.setStatus(status) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        additionalContent = { _ ->
            // Schedule section
            performerScheduleSection(
                viewModel = viewModel,
                navController = navController
            )
        }
    )
}

/**
 * Performer information display using reusable DetailHeroCard
 */
@Composable
fun PerformerInfoCard(performer: Performer) {
    DetailHeroCard(
        images = performer.images,
        title = performer.name,
        typeBadge = performer.performerType,
        description = performer.bio,
        additionalContent = {
            // Created At
            performer.createdAt?.let { createdAt ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Added: ${createdAt.toString().substringBefore('T')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}

/**
 * LazyListScope extension for displaying performer's schedule section
 */
fun LazyListScope.performerScheduleSection(
    viewModel: PerformerDetailViewModel,
    navController: NavController
) {
    // Section header
    item {
        SectionHeader(title = "Schedule")
    }

    // Content based on state
    item {
        val agendaItemsState by viewModel.agendaItems.collectAsState()

        when (val state = agendaItemsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Text(
                    text = "Failed to load schedule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is Resource.Success -> {
                val connection = state.data
                if (connection.agendaItems.isEmpty()) {
                    Text(
                        text = "No scheduled appearances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Column {
                        connection.agendaItems.forEach { agendaItem ->
                            CompactAgendaItemCard(
                                item = agendaItem,
                                onClick = {
                                    navController.navigate(
                                        AgendaItemDetailNavigationArgs(agendaItem.id)
                                    )
                                }
                            )
                            HorizontalDivider()
                        }

                        // Load more button if there are more items
                        if (connection.hasNextPage) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { viewModel.loadMoreAgendaItems() },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Load more")
                                }
                            }
                        }
                    }
                }
            }
            is Resource.NotLoading -> {
                // Do nothing - initial state before loading starts
            }
        }
    }
}
