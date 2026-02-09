package com.district37.toastmasters.features.schedules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.EditAgendaItemRoute
import com.district37.toastmasters.navigation.EventDetailNavigationArgs
import com.district37.toastmasters.navigation.LocationDetailNavigationArgs
import com.district37.toastmasters.navigation.PerformerDetailNavigationArgs
import com.district37.toastmasters.components.common.DetailScaffold
import com.district37.toastmasters.components.common.MoreOption
import com.district37.toastmasters.components.common.MoreOptionsBottomSheet
import com.district37.toastmasters.components.common.OptionBadge
import com.district37.toastmasters.components.common.rememberMoreOptionsState
import com.district37.toastmasters.components.common.SectionHeader
import com.district37.toastmasters.components.engagement.AdaptiveEngagementBar
import com.district37.toastmasters.components.engagement.AdaptiveEngagementBarWithFriends
import com.district37.toastmasters.components.engagement.FriendRsvpBottomSheet
import com.district37.toastmasters.components.engagement.LoginPromptDialog
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.infra.calendar.rememberCalendarPermissionLauncher
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.components.events.EventCard
import com.district37.toastmasters.components.locations.LocationCard
import com.district37.toastmasters.components.performers.PerformerCard
import com.district37.toastmasters.components.schedules.AgendaItemHeroCard
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.util.DateTimeFormatter
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Agenda item detail screen that displays comprehensive information about an agenda item
 * including title, description, timing, tag, performers, and location
 *
 * @param agendaItemId The ID of the agenda item to display
 * @param showEvent Whether to show the associated event section (false when navigating from event context)
 * @param onBackClick Callback when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaItemDetailScreen(
    agendaItemId: Int,
    showEvent: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    val viewModel: AgendaItemDetailViewModel =
        koinViewModel(key = agendaItemId.toString()) { parametersOf(agendaItemId) }
    val authViewModel: AuthViewModel = koinViewModel()
    val agendaItemState by viewModel.item.collectAsState()
    val showLoginPrompt by viewModel.authFeature.showLoginPrompt.collectAsState()
    val navController = LocalNavController.current

    // Calendar sync state
    val isItemSynced by viewModel.isItemSynced.collectAsState()
    val isCalendarSyncing by viewModel.isCalendarSyncing.collectAsState()
    val availableCalendars by viewModel.availableCalendars.collectAsState()
    val showCalendarPicker by viewModel.showCalendarPicker.collectAsState()
    val needsCalendarPermission by viewModel.needsCalendarPermission.collectAsState()
    val calendarSyncError by viewModel.calendarSyncError.collectAsState()
    val isLoadingCalendars by viewModel.isLoadingCalendars.collectAsState()

    // Calendar permission launcher
    val requestCalendarPermission = rememberCalendarPermissionLauncher { granted ->
        if (granted) {
            viewModel.dismissPermissionRequest()
            viewModel.onCalendarButtonClick()
        } else {
            viewModel.dismissPermissionRequest()
        }
    }

    // Auto-request permission when needed
    LaunchedEffect(needsCalendarPermission) {
        if (needsCalendarPermission) {
            requestCalendarPermission()
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

    // Calendar picker bottom sheet
    if (showCalendarPicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCalendarPicker() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Calendar for Syncing",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    isLoadingCalendars -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    availableCalendars.isEmpty() -> {
                        Text(
                            text = "No calendars found on this device. Please create a calendar in your device's calendar app first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                        )
                    }
                    else -> {
                        availableCalendars.forEach { calendar ->
                            ListItem(
                                headlineContent = { Text(calendar.name) },
                                supportingContent = calendar.accountName?.let { { Text(it) } },
                                modifier = Modifier.clickable {
                                    viewModel.onCalendarSelected(calendar.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Calendar sync error dialog
    calendarSyncError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearCalendarError() },
            title = { Text("Calendar Sync Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCalendarError() }) {
                    Text("OK")
                }
            }
        )
    }

    // More options bottom sheet state
    val moreOptionsState = rememberMoreOptionsState()

    // More options bottom sheet
    MoreOptionsBottomSheet(
        state = moreOptionsState,
        title = "Options",
        options = listOf(
            MoreOption(
                icon = Icons.Outlined.CalendarMonth,
                title = if (isItemSynced) "End Calendar Sync" else "Sync to Calendar",
                subtitle = "Sync this session to your device calendar",
                isActive = isItemSynced,
                isLoading = isCalendarSyncing,
                badge = if (isItemSynced) OptionBadge.REMOVE else OptionBadge.ADD,
                onClick = { viewModel.onCalendarButtonClick() }
            )
        )
    )

    DetailScaffold(
        resourceState = agendaItemState,
        onBackClick = onBackClick,
        onRetry = { viewModel.refresh() },
        errorMessage = "Failed to load agenda item",
        actions = {
            // Share button
            val shareManager: ShareManager = koinInject()
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generateAgendaItemUrl(agendaItemId),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Agenda Item"
                )
            }

            // Edit button - only show if user has edit permission on the parent event
            val agendaItemPermissions = (agendaItemState as? Resource.Success)?.data?.permissions
            if (agendaItemPermissions?.canEdit == true) {
                IconButton(onClick = {
                    navController.navigate(EditAgendaItemRoute(agendaItemId))
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Agenda Item"
                    )
                }
            }

            // More options button (replaces calendar)
            IconButton(onClick = { moreOptionsState.show() }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        },
        primaryContent = { agendaItem ->
            AgendaItemHeroCard(agendaItem = agendaItem)

            // Engagement bar - only show when feature is initialized
            viewModel.engagementFeature?.let { feature ->
                val engagement by feature.engagement.collectAsState()

                // Friend RSVPs feature state
                viewModel.friendRsvpsFeature?.let { friendFeature ->
                    val friendRsvpsPreview by friendFeature.preview.collectAsState()
                    val friendRsvpsFull by friendFeature.fullList.collectAsState()
                    var showFriendSheet by remember { mutableStateOf(false) }

                    AdaptiveEngagementBarWithFriends(
                        entityType = EntityType.AGENDAITEM,
                        engagement = engagement,
                        friendRsvps = (friendRsvpsPreview as? Resource.Success)?.data?.rsvps ?: emptyList(),
                        totalFriendCount = (friendRsvpsPreview as? Resource.Success)?.data?.totalCount ?: 0,
                        isAuthenticated = viewModel.authFeature.isAuthenticated,
                        onEngagementClick = { feature.toggleSubscription() },
                        onStatusSelected = { status -> feature.setStatus(status) },
                        onFriendAvatarsClick = {
                            friendFeature.loadFullList()
                            showFriendSheet = true
                        },
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    // Friend RSVP bottom sheet
                    if (showFriendSheet) {
                        when (val state = friendRsvpsFull) {
                            is Resource.Success -> {
                                val isLoadingMore by friendFeature.isLoadingMore.collectAsState()
                                FriendRsvpBottomSheet(
                                    rsvps = state.data,
                                    totalCount = state.data.size,
                                    hasNextPage = isLoadingMore,
                                    isLoading = false,
                                    onLoadMore = { friendFeature.loadMore() },
                                    onDismiss = { showFriendSheet = false }
                                )
                            }
                            is Resource.Loading -> {
                                // Show loading state in modal
                            }
                            is Resource.Error -> {
                                // Show error state, dismiss sheet
                                showFriendSheet = false
                            }
                            is Resource.NotLoading -> {
                                // Not loading yet, dismiss sheet
                                showFriendSheet = false
                            }
                        }
                    }
                } ?: run {
                    // Fallback to AdaptiveEngagementBar if friend feature not initialized
                    AdaptiveEngagementBar(
                        entityType = EntityType.AGENDAITEM,
                        engagement = engagement,
                        isAuthenticated = viewModel.authFeature.isAuthenticated,
                        onEngagementClick = { feature.toggleSubscription() },
                        onStatusSelected = { status -> feature.setStatus(status) },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        },
        additionalContent = { agendaItem ->
            // Location Section - use nested data
            if (agendaItem.location != null) {
                item(key = "location_section") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Location",
                            icon = Icons.Default.LocationOn,
                            iconContentDescription = "Location"
                        )
                        LocationCard(
                            location = agendaItem.location,
                            onClick = {
                                navController.navigate(
                                    LocationDetailNavigationArgs(
                                        agendaItem.location.id
                                    )
                                )
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            // Performers Section - horizontal carousel
            if (agendaItem.performers.isNotEmpty()) {
                item(key = "performers_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            title = "Performers",
                            icon = Icons.Default.Person,
                            iconContentDescription = "Performers",
                            count = agendaItem.performers.size
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(agendaItem.performers.size) { index ->
                                val performer = agendaItem.performers[index]
                                PerformerCard(
                                    performer = performer,
                                    onClick = {
                                        navController.navigate(
                                            PerformerDetailNavigationArgs(
                                                performer.id
                                            )
                                        )
                                    },
                                    modifier = Modifier.width(260.dp)
                                )
                            }
                        }
                    }
                }
            }
            // Event Section - use nested data (only shown when showEvent is true)
            if (showEvent && agendaItem.event != null) {
                item(key = "event_section") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Part of Event",
                            icon = Icons.Default.Event,
                            iconContentDescription = "Event"
                        )
                        EventCard(
                            event = agendaItem.event,
                            onClick = {
                                navController.navigate(
                                    EventDetailNavigationArgs(
                                        agendaItem.event.id
                                    )
                                )
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    )
}

/**
 * Card component displaying agenda item information
 */
@Composable
fun AgendaItemInfoCard(agendaItem: AgendaItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = agendaItem.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Timing
            if (agendaItem.startTime != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            DateTimeFormatter.formatDate(agendaItem.startTime),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = DateTimeFormatter.formatTimeRange(
                                agendaItem.startTime,
                                agendaItem.endTime
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Tag
            agendaItem.tag?.let { tag ->
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = tag.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            // Description
            agendaItem.description?.let { description ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
