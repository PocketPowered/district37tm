package com.district37.toastmasters.features.account

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.components.activity.activityFeedSection
import com.district37.toastmasters.components.common.RefreshableContent
import com.district37.toastmasters.components.events.userEventsCarouselSection
import com.district37.toastmasters.models.CollaborationRequestConnection
import com.district37.toastmasters.models.FriendRequest
import com.district37.toastmasters.components.collaborators.CollaborationRequestCard
import com.district37.toastmasters.features.account.components.ProfileBannerHeader
import com.district37.toastmasters.features.account.components.ProfileStatsRow
import com.district37.toastmasters.features.account.components.ProfileTab
import com.district37.toastmasters.features.account.components.ProfileTabBar
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.util.Resource
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.district37.toastmasters.messaging.UnreadMessagesManager
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * Enhanced user profile screen with modern Twitter/LinkedIn-style layout.
 * Features banner header, overlapping profile photo, stats row, and tab-based navigation.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserProfileScreen(
    user: User,
    profileViewModel: UserProfileViewModel,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onMessages: () -> Unit,
    onViewAllSubscribedEvents: () -> Unit = {},
    onViewAllAttendingEvents: () -> Unit = {},
    onViewAllFriends: () -> Unit = {},
    onLongPressAvatar: () -> Unit = {},
    onViewUserProfile: (String) -> Unit = {}
) {
    val friendRequestsState by profileViewModel.friendRequests.collectAsState()
    val collaborationRequestsState by profileViewModel.collaborationRequests.collectAsState()
    val processingCollaborationRequests by profileViewModel.processingCollaborationRequests.collectAsState()
    val subscribedEventsState by profileViewModel.subscribedEvents.collectAsState()
    val attendingEventsState by profileViewModel.attendingEvents.collectAsState()
    val activityFeedState by profileViewModel.activityFeed.collectAsState()
    val isLoadingMoreActivity by profileViewModel.isLoadingMoreActivity.collectAsState()
    val selectedTab by profileViewModel.selectedTab.collectAsState()
    val requestsBadgeCount by profileViewModel.requestsBadgeCount.collectAsState()
    val profileData by profileViewModel.profileData.collectAsState()
    val friendsCount = (profileData as? Resource.Success)?.data?.friendsCount ?: 0

    // Unread messages indicator
    val unreadMessagesManager: UnreadMessagesManager = koinInject()
    val hasUnreadMessages by unreadMessagesManager.hasAnyUnread.collectAsState()

    // Get the user's primary color for the gradient
    val gradientColor = user.primaryColor?.let { hexToColor(it) }

    // Configure the TopAppBar with messages, edit, and settings icons
    ConfigureTopAppBar(
        config = TopAppBarConfig(
            showUserAvatar = true,
            centerContent = TopAppBarConfig.CenterContent.Logo,
            gradientColor = gradientColor,
            actions = {
                IconButton(onClick = onMessages) {
                    Box {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Messages"
                        )
                        // Unread indicator dot
                        if (hasUnreadMessages) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
                IconButton(onClick = onEditProfile) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RefreshableContent(
            onRefresh = { profileViewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            ProfileContent(
                user = user,
                memberSince = user.createdAt?.let { formatMemberSince(it) },
                onLongPressAvatar = onLongPressAvatar,
                subscribedEventsState = subscribedEventsState,
                attendingEventsState = attendingEventsState,
                friendsCount = friendsCount,
                onViewAllSubscribedEvents = onViewAllSubscribedEvents,
                onViewAllAttendingEvents = onViewAllAttendingEvents,
                onViewAllFriends = onViewAllFriends,
                selectedTab = selectedTab,
                onTabSelected = { profileViewModel.selectTab(it) },
                requestsBadgeCount = requestsBadgeCount,
                friendRequestsState = friendRequestsState,
                collaborationRequestsState = collaborationRequestsState,
                processingCollaborationRequests = processingCollaborationRequests,
                activityFeedState = activityFeedState,
                isLoadingMoreActivity = isLoadingMoreActivity,
                onAcceptFriendRequest = { profileViewModel.acceptFriendRequest(it) },
                onRejectFriendRequest = { profileViewModel.rejectFriendRequest(it) },
                onAcceptCollaborationRequest = { profileViewModel.acceptCollaborationRequest(it) },
                onRejectCollaborationRequest = { profileViewModel.rejectCollaborationRequest(it) },
                onLoadMoreActivity = { profileViewModel.loadMoreActivity() },
                onViewUserProfile = onViewUserProfile
            )
        }
    }
}

/**
 * Profile content with new layout structure
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileContent(
    user: User,
    memberSince: String?,
    onLongPressAvatar: () -> Unit,
    subscribedEventsState: Resource<UserEventsConnection>,
    attendingEventsState: Resource<UserEventsConnection>,
    friendsCount: Int,
    onViewAllSubscribedEvents: () -> Unit,
    onViewAllAttendingEvents: () -> Unit,
    onViewAllFriends: () -> Unit,
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    requestsBadgeCount: Int,
    friendRequestsState: Resource<com.district37.toastmasters.models.FriendRequestConnection>,
    collaborationRequestsState: Resource<CollaborationRequestConnection>,
    processingCollaborationRequests: Set<Int>,
    activityFeedState: Resource<List<com.district37.toastmasters.models.ActivityFeedItem>>,
    isLoadingMoreActivity: Boolean,
    onAcceptFriendRequest: (Int) -> Unit,
    onRejectFriendRequest: (Int) -> Unit,
    onAcceptCollaborationRequest: (Int) -> Unit,
    onRejectCollaborationRequest: (Int) -> Unit,
    onLoadMoreActivity: () -> Unit,
    onViewUserProfile: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // Calculate stats and loading state
    val subscribedCount = (subscribedEventsState as? Resource.Success)?.data?.totalCount ?: 0
    val attendingCount = (attendingEventsState as? Resource.Success)?.data?.totalCount ?: 0
    val isStatsLoading = subscribedEventsState is Resource.Loading || attendingEventsState is Resource.Loading

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner header with profile info
        item(key = "banner_header") {
            ProfileBannerHeader(
                bannerImageUrl = null, // No banner image support yet
                profileImageUrl = user.effectiveAvatarUrl,
                displayName = user.effectiveDisplayName,
                username = user.username,
                memberSince = memberSince,
                bio = user.bio,
                onLongPressAvatar = onLongPressAvatar,
                primaryColor = user.primaryColor
            )
        }

        // Stats row
        item(key = "stats_row") {
            ProfileStatsRow(
                subscribedCount = subscribedCount,
                attendingCount = attendingCount,
                friendsCount = friendsCount,
                isLoading = isStatsLoading,
                onSubscribedClick = onViewAllSubscribedEvents,
                onAttendingClick = onViewAllAttendingEvents,
                onFriendsClick = onViewAllFriends
            )
        }

        // Sticky tab bar
        stickyHeader(key = "tab_bar") {
            ProfileTabBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                requestsBadgeCount = requestsBadgeCount
            )
        }

        // Tab content based on selected tab
        when (selectedTab) {
            ProfileTab.EVENTS -> {
                // Subscribed Events Carousel
                userEventsCarouselSection(
                    title = "Subscribed Events",
                    eventsState = subscribedEventsState,
                    onViewAllClick = onViewAllSubscribedEvents,
                    emptyMessage = "No subscribed events yet"
                )

                // Attending Events Carousel
                userEventsCarouselSection(
                    title = "Attending",
                    eventsState = attendingEventsState,
                    onViewAllClick = onViewAllAttendingEvents,
                    emptyMessage = "Not attending any events yet"
                )

                // Bottom spacing for events tab
                item(key = "events_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            ProfileTab.ACTIVITY -> {
                // Activity Feed Section
                activityFeedSection(
                    activityFeedState = activityFeedState,
                    isLoadingMore = isLoadingMoreActivity,
                    onLoadMore = onLoadMoreActivity,
                    sectionHeaderText = "Your Activity",
                    emptyStateTitle = "No activity yet",
                    emptyStateSubtitle = "Save events, RSVP, and interact to see your activity here",
                    errorMessage = "Failed to load activity"
                )

                // Bottom spacing for activity tab
                item(key = "activity_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            ProfileTab.REQUESTS -> {
                // Friend Requests Section
                val friendRequests =
                    (friendRequestsState as? Resource.Success)?.data?.requests ?: emptyList()
                if (friendRequests.isNotEmpty()) {
                    item(key = "friend_requests_header") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Friend Requests",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${friendRequests.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(friendRequests, key = { it.id }) { request ->
                        FriendRequestCard(
                            request = request,
                            onAccept = { onAcceptFriendRequest(request.id) },
                            onReject = { onRejectFriendRequest(request.id) },
                            onUserClick = { onViewUserProfile(request.senderId) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // Collaboration Requests Section
                val collaborationRequests =
                    (collaborationRequestsState as? Resource.Success)?.data?.requests ?: emptyList()
                if (collaborationRequests.isNotEmpty()) {
                    item(key = "collab_requests_header") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (friendRequests.isNotEmpty()) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Collaboration Requests",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${collaborationRequests.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(collaborationRequests, key = { "collab_${it.id}" }) { request ->
                        CollaborationRequestCard(
                            request = request,
                            onAccept = { onAcceptCollaborationRequest(request.id) },
                            onReject = { onRejectCollaborationRequest(request.id) },
                            isLoading = processingCollaborationRequests.contains(request.id),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // Empty state for requests
                if (friendRequests.isEmpty() && collaborationRequests.isEmpty()) {
                    item(key = "requests_empty") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No pending requests",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Friend and collaboration requests will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // Bottom spacing for requests tab
                item(key = "requests_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Card for displaying a friend request with accept/reject actions
 */
@Composable
private fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clickable user info section (avatar + name)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onUserClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    val profileImageUrl = request.senderProfileImageUrl
                    if (profileImageUrl != null) {
                        CoilImage(
                            imageModel = { profileImageUrl },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                contentDescription = request.senderDisplayName
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // User name
                Column {
                    Text(
                        text = request.senderDisplayName ?: "Unknown User",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "wants to be your friend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Accept button
            IconButton(
                onClick = onAccept,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reject button
            IconButton(
                onClick = onReject,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Format the member since date
 */
private fun formatMemberSince(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${localDateTime.year}"
}

/**
 * Convert a hex color string to a Compose Color.
 * Supports formats: "#RRGGBB" or "RRGGBB"
 */
private fun hexToColor(hex: String): Color? {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF$cleanHex").toLong(16))
    } catch (_: Exception) {
        null
    }
}
