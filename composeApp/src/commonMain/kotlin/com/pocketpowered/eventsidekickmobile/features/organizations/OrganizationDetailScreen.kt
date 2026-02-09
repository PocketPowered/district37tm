package com.district37.toastmasters.features.organizations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.district37.toastmasters.navigation.EditOrganizationRoute
import com.district37.toastmasters.navigation.OrganizationEventsNavigationArgs
import com.district37.toastmasters.components.common.DetailHeroCard
import com.district37.toastmasters.components.common.DetailScaffold
import com.district37.toastmasters.components.engagement.AdaptiveEngagementBar
import com.district37.toastmasters.components.engagement.LoginPromptDialog
import com.district37.toastmasters.components.events.eventCardList
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.models.Organization
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Organization detail screen that displays comprehensive information about an organization
 * including its details, events, and members
 */
@Composable
fun OrganizationDetailScreen(
    organizationId: Int,
    onBackClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val viewModel: OrganizationDetailViewModel = koinViewModel(key = organizationId.toString()) { parametersOf(organizationId) }
    val authViewModel: AuthViewModel = koinViewModel()
    val organizationState by viewModel.item.collectAsState()
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
        resourceState = organizationState,
        onBackClick = onBackClick,
        onRetry = { viewModel.refresh() },
        errorMessage = "Failed to load organization",
        actions = {
            // Share button
            val shareManager: ShareManager = koinInject()
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generateOrganizationUrl(organizationId, organizationState.getOrNull()?.slug),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Organization"
                )
            }

            // Edit button - only show if user can edit
            if (permissions?.canEdit == true) {
                IconButton(onClick = { navController.navigate(EditOrganizationRoute(organizationId)) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Organization"
                    )
                }
            }
        },
        primaryContent = { organization ->
            OrganizationInfoCard(organization = organization)

            // Engagement bar - only show when feature is initialized
            viewModel.engagementFeature?.let { feature ->
                val engagement by feature.engagement.collectAsState()

                AdaptiveEngagementBar(
                    entityType = EntityType.ORGANIZATION,
                    engagement = engagement,
                    isAuthenticated = viewModel.authFeature.isAuthenticated,
                    onEngagementClick = { feature.toggleSubscription() },
                    onStatusSelected = { status -> feature.setStatus(status) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        additionalContent = { organization ->
            // Description section
            organization.description?.let { description ->
                if (description.isNotBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Contact info section
            val hasContactInfo = listOfNotNull(
                organization.website,
                organization.email,
                organization.phone
            ).isNotEmpty()

            if (hasContactInfo) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Contact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        organization.website?.let { website ->
                            ContactInfoRow(label = "Website", value = website)
                        }
                        organization.email?.let { email ->
                            ContactInfoRow(label = "Email", value = email)
                        }
                        organization.phone?.let { phone ->
                            ContactInfoRow(label = "Phone", value = phone)
                        }
                    }
                }
            }

            // Events by this Organization Section
            eventCardList(
                title = "Events by ${organization.name}",
                events = organization.events.items,
                totalCount = organization.events.totalCount,
                hasMore = organization.events.hasMore,
                onViewAllClick = { navController.navigate(OrganizationEventsNavigationArgs(organization.id)) },
                emptyMessage = "No events from this organization"
            )
        }
    )
}

/**
 * Organization information display using reusable DetailHeroCard
 */
@Composable
fun OrganizationInfoCard(organization: Organization) {
    DetailHeroCard(
        images = organization.images,
        title = organization.name,
        subtitle = null,
        additionalContent = {
            // Member count if available
            if (organization.members.items.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Members:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = organization.members.totalCount?.toString() ?: organization.members.items.size.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    )
}

@Composable
private fun ContactInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
