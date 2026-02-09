package com.district37.toastmasters.features.events

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.components.common.ArchivedBanner
import com.district37.toastmasters.components.common.ErrorCard
import com.district37.toastmasters.components.common.EventInfoCard
import com.district37.toastmasters.components.common.GetDirectionsButton
import com.district37.toastmasters.components.common.MoreOption
import com.district37.toastmasters.components.common.MoreOptionsBottomSheet
import com.district37.toastmasters.components.common.OptionBadge
import com.district37.toastmasters.components.common.ParallaxHero
import com.district37.toastmasters.components.common.RefreshableContent
import com.district37.toastmasters.components.common.rememberMoreOptionsState
import com.district37.toastmasters.components.common.SectionCard
import com.district37.toastmasters.components.engagement.EngagementBar
import com.district37.toastmasters.components.engagement.EngagementBarWithFriends
import com.district37.toastmasters.components.engagement.FriendRsvpBottomSheet
import com.district37.toastmasters.components.engagement.LoginPromptDialog
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.EditEventRoute
import com.district37.toastmasters.navigation.ManageAgendaItemsRoute
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalBottomNavInsets
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.navigation.VenueDetailNavigationArgs
import com.district37.toastmasters.components.schedules.AgendaTabRow
import com.district37.toastmasters.components.schedules.SwipeableAgendaTabContent
import com.district37.toastmasters.components.venues.VenueCard
import com.district37.toastmasters.features.schedules.rememberAgendaTabsState
import com.district37.toastmasters.infra.PinnedEventManager
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.infra.calendar.rememberCalendarPermissionLauncher
import kotlinx.coroutines.launch
import com.district37.toastmasters.util.DateTimeFormatter
import com.district37.toastmasters.util.Resource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Event detail screen that displays comprehensive information about an event
 * including images, venue details, and schedules.
 *
 * Features a parallax hero, flush section cards, and enhanced venue display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    onBackClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val viewModel: EventDetailViewModel =
        koinViewModel(key = eventId.toString()) { parametersOf(eventId) }
    val authViewModel: AuthViewModel = koinViewModel()
    val eventState by viewModel.item.collectAsState()
    val showLoginPrompt by viewModel.authFeature.showLoginPrompt.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val navController = LocalNavController.current
    val bottomNavInsets = LocalBottomNavInsets.current

    // Pin event functionality
    val pinnedEventManager: PinnedEventManager = koinInject()
    val scope = rememberCoroutineScope()
    var isPinned by remember { mutableStateOf(false) }

    // Check if this event is pinned on load
    LaunchedEffect(eventId) {
        isPinned = pinnedEventManager.isEventPinned(eventId)
    }

    // Calendar sync state
    val isEventSynced by viewModel.isEventSynced.collectAsState()
    val isCalendarSyncing by viewModel.isCalendarSyncing.collectAsState()
    val availableCalendars by viewModel.availableCalendars.collectAsState()
    val showCalendarPicker by viewModel.showCalendarPicker.collectAsState()
    val needsCalendarPermission by viewModel.needsCalendarPermission.collectAsState()
    val calendarSyncError by viewModel.calendarSyncError.collectAsState()
    val calendarPickerSheetState = rememberModalBottomSheetState()

    // Bulk sync state (Sync My Schedule)
    val isBulkSyncing by viewModel.isBulkSyncing.collectAsState()
    val bulkSyncResult by viewModel.bulkSyncResult.collectAsState()

    // Calendar permission launcher
    val requestCalendarPermission = rememberCalendarPermissionLauncher { granted ->
        if (granted) {
            viewModel.dismissPermissionRequest()
            // Permission granted, try the calendar action again
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

    // Handle refresh signal from navigation
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    // Collect agenda tabs feature state using helper
    val agendaState = rememberAgendaTabsState(viewModel.agendaTabsFeature)

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

    // More options bottom sheet state
    val moreOptionsState = rememberMoreOptionsState()

    // Configure the root TopAppBar for detail screens with actions
    val shareManager: ShareManager = koinInject()
    val surfaceColor = MaterialTheme.colorScheme.surface
    ConfigureTopAppBar(
        config = AppBarConfigs.detailScreen(actions = {
            // Share button
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generateEventUrl(eventId, eventState.getOrNull()?.slug),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Event"
                )
            }

            // Edit button - only show if user can edit and event is not archived
            val currentEvent = (eventState as? Resource.Success)?.data
            if (permissions?.canEdit == true && currentEvent?.isArchived != true) {
                IconButton(onClick = { navController.navigate(EditEventRoute(eventId)) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Event"
                    )
                }
            }

            // More options button (replaces pin and calendar)
            IconButton(onClick = { moreOptionsState.show() }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options"
                )
            }
        }).copy(gradientColor = surfaceColor),
        onBackClick = onBackClick
    )

    // More options bottom sheet
    MoreOptionsBottomSheet(
        state = moreOptionsState,
        title = "Options",
        options = listOf(
            MoreOption(
                icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                title = if (isPinned) "Unpin Event" else "Pin Event",
                subtitle = "Open this event when the app starts",
                isActive = isPinned,
                onClick = {
                    scope.launch {
                        if (isPinned) {
                            pinnedEventManager.unpinEvent()
                            isPinned = false
                        } else {
                            pinnedEventManager.pinEvent(eventId)
                            isPinned = true
                        }
                    }
                }
            ),
            MoreOption(
                icon = Icons.Outlined.CalendarMonth,
                title = if (isEventSynced) "End Calendar Sync" else "Sync to Calendar",
                subtitle = "Sync the event to your calendar (does not sync the agenda)",
                isActive = isEventSynced,
                isLoading = isCalendarSyncing,
                badge = if (isEventSynced) OptionBadge.REMOVE else OptionBadge.ADD,
                onClick = { viewModel.onCalendarButtonClick() }
            )
        )
    )

    // Friend sheet state
    var showFriendSheet by remember { mutableStateOf(false) }
    val topBarInsets = LocalTopAppBarInsets.current

    // Lazy list state for parallax effect
    val lazyListState = rememberLazyListState()

    // Detect when agenda tabs sticky header is "stuck" at the top
    val isAgendaTabsStuck by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo
                .find { it.key == "agenda_tabs" }
                ?.let { it.offset <= -topBarInsets.recommendedHeroPadding.value } ?: false
        }
    }

    // Animate the spacer height for smooth sticky transition
    val animatedSpacerHeight by animateDpAsState(
        targetValue = if (isAgendaTabsStuck) topBarInsets.recommendedContentPadding else 0.dp,
        animationSpec = spring()
    )

    // Friend RSVP bottom sheet (rendered outside the scrollable content)
    viewModel.friendRsvpsFeature?.let { friendFeature ->
        val friendRsvpsFull by friendFeature.fullList.collectAsState()

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
                    showFriendSheet = false
                }
                is Resource.NotLoading -> {
                    showFriendSheet = false
                }
            }
        }
    }

    // Calendar picker bottom sheet
    val isLoadingCalendars by viewModel.isLoadingCalendars.collectAsState()

    if (showCalendarPicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCalendarPicker() },
            sheetState = calendarPickerSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    isLoadingCalendars -> {
                        // Loading state
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
                        // Empty state
                        Text(
                            text = "No calendars found on this device. Please create a calendar in your device's calendar app first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                        )
                    }
                    else -> {
                        // Calendar list with scrolling
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(availableCalendars) { calendar ->
                                ListItem(
                                    headlineContent = { Text(calendar.name) },
                                    supportingContent = calendar.accountName?.let { { Text(it) } },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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

    // Bulk sync result dialog
    bulkSyncResult?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearBulkSyncResult() },
            title = { Text("Schedule Sync") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearBulkSyncResult() }) {
                    Text("OK")
                }
            }
        )
    }

    RefreshableContent(
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(
                top = topBarInsets.recommendedContentPadding,
                bottom = bottomNavInsets.recommendedContentPadding
            )
        ) {
            when (val state = eventState) {
                is Resource.Loading -> {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is Resource.Error -> {
                    item(key = "error") {
                        Box(modifier = Modifier.padding(top = 80.dp, start = 16.dp, end = 16.dp)) {
                            ErrorCard(
                                message = state.message ?: "Failed to load event",
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val event = state.data

                    // Parallax hero image
                    item(key = "hero") {
                        ParallaxHero(
                            images = event.images,
                            lazyListState = lazyListState
                        )
                    }

                    // Archived banner (if event is archived)
                    if (event.isArchived && event.archivedAt != null) {
                        item(key = "archived_banner") {
                            ArchivedBanner(
                                entityType = "event",
                                archivedAt = event.archivedAt!!
                            )
                        }
                    }

                    // Event info card (flush below hero)
                    item(key = "event_info") {
                        EventInfoCard(
                            title = event.name,
                            subtitle = DateTimeFormatter.formatDateRange(
                                startTime = event.startDate,
                                endTime = event.endDate,
                                showYear = true
                            ),
                            description = event.description,
                            typeBadge = event.eventType?.toString()?.lowercase(),
                            engagementContent = {
                                // Engagement bar - only show when feature is initialized
                                viewModel.engagementFeature?.let { feature ->
                                    val engagement by feature.engagement.collectAsState()

                                    // Friend RSVPs feature state
                                    viewModel.friendRsvpsFeature?.let { friendFeature ->
                                        val friendRsvpsPreview by friendFeature.preview.collectAsState()

                                        EngagementBarWithFriends(
                                            engagement = engagement,
                                            friendRsvps = (friendRsvpsPreview as? Resource.Success)?.data?.rsvps ?: emptyList(),
                                            totalFriendCount = (friendRsvpsPreview as? Resource.Success)?.data?.totalCount ?: 0,
                                            isAuthenticated = viewModel.authFeature.isAuthenticated,
                                            onSubscribeClick = { feature.toggleSubscription() },
                                            onStatusSelected = { status -> feature.setStatus(status) },
                                            onFriendAvatarsClick = {
                                                friendFeature.loadFullList()
                                                showFriendSheet = true
                                            },
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    } ?: run {
                                        // Fallback to original EngagementBar if friend feature not initialized
                                        EngagementBar(
                                            engagement = engagement,
                                            isAuthenticated = viewModel.authFeature.isAuthenticated,
                                            onSubscribeClick = { feature.toggleSubscription() },
                                            onStatusSelected = { status -> feature.setStatus(status) },
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // Venue Section
                    event.venue?.let { venue ->
                        item(key = "venue_section") {
                            SectionCard(modifier = Modifier.padding(vertical = 16.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Venue",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    VenueCard(
                                        venue = venue,
                                        onClick = { navController.navigate(VenueDetailNavigationArgs(venue.id)) }
                                    )
                                    // Get Directions button (only if coordinates available)
                                    if (venue.latitude != null && venue.longitude != null) {
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
                    }

                    // Agenda Section
                    if (agendaState.agendaDates.isNotEmpty()) {
                        // Sticky tab row with settings menu - stays at top while scrolling through content
                        stickyHeader(key = "agenda_tabs") {
                            var showScheduleSettings by remember { mutableStateOf(false) }
                            val scheduleSettingsSheetState = rememberModalBottomSheetState()

                            // Schedule settings bottom sheet
                            if (showScheduleSettings) {
                                ModalBottomSheet(
                                    onDismissRequest = { showScheduleSettings = false },
                                    sheetState = scheduleSettingsSheetState
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                    ) {
                                        Text(
                                            text = "Schedule Settings",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                        // Manage Schedule option - only show if user can edit and event is not archived
                                        val currentEventForSheet = (eventState as? Resource.Success)?.data
                                        if (permissions?.canEdit == true && currentEventForSheet?.isArchived != true) {
                                            ListItem(
                                                headlineContent = { Text("Manage Schedule") },
                                                supportingContent = { Text("Add, edit, or remove agenda items") },
                                                leadingContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    showScheduleSettings = false
                                                    navController.navigate(ManageAgendaItemsRoute(eventId))
                                                },
                                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                            )
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                        ListItem(
                                            headlineContent = { Text("My RSVPs only") },
                                            supportingContent = { Text("Only show items you've RSVP'd to") },
                                            trailingContent = {
                                                Switch(
                                                    checked = agendaState.myScheduleOnly,
                                                    onCheckedChange = { viewModel.agendaTabsFeature.toggleMySchedule() }
                                                )
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                        )
                                        // Calendar sync section - only show if authenticated
                                        if (viewModel.authFeature.isAuthenticated) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            ListItem(
                                                headlineContent = { Text("Sync now") },
                                                supportingContent = { Text("Sync all your RSVP'd items to calendar") },
                                                trailingContent = {
                                                    if (isBulkSyncing) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.padding(8.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                    } else {
                                                        IconButton(onClick = {
                                                            viewModel.syncMyScheduleToCalendar()
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.CalendarMonth,
                                                                contentDescription = "Sync My Schedule"
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.clickable(enabled = !isBulkSyncing) {
                                                    viewModel.syncMyScheduleToCalendar()
                                                },
                                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Animated spacer for smooth sticky transition
                                Spacer(modifier = Modifier.height(animatedSpacerHeight))
                                AgendaTabRow(
                                    agendaDates = agendaState.agendaDates,
                                    selectedTabIndex = agendaState.selectedTabIndex,
                                    onTabSelected = { index -> viewModel.agendaTabsFeature.selectTab(index) },
                                    trailingContent = {
                                        // Settings button with active filter badge
                                        Box {
                                            IconButton(onClick = { showScheduleSettings = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Tune,
                                                    contentDescription = "Schedule Settings"
                                                )
                                            }
                                            if (agendaState.activeFilterCount > 0) {
                                                Badge(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .offset(x = (-4).dp, y = 4.dp)
                                                ) {
                                                    Text(
                                                        text = agendaState.activeFilterCount.toString(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // Agenda content - scrolls normally, swipeable to navigate dates
                        item(key = "agenda_content") {
                            SwipeableAgendaTabContent(
                                agendaDates = agendaState.agendaDates,
                                agendaItemsMap = agendaState.agendaItemsMap,
                                selectedTabIndex = agendaState.selectedTabIndex,
                                onTabSelected = { index -> viewModel.agendaTabsFeature.selectTab(index) },
                                isLoadingMoreItems = agendaState.isLoadingMoreItems,
                                onLoadMoreItems = { date -> viewModel.agendaTabsFeature.loadMoreAgendaItemsForDate(date) },
                                emptyMessage = if (agendaState.myScheduleOnly) {
                                    "No items on your schedule for this date"
                                } else {
                                    "No items for this date"
                                }
                            )
                        }
                    }
                }

                is Resource.NotLoading -> {
                    // Initial state - show empty for pull to refresh
                    item(key = "empty") {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
