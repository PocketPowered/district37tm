package com.district37.toastmasters.features.users

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.activity.ActivityFeedItemCard
import com.district37.toastmasters.components.common.RefreshableContent
import com.district37.toastmasters.components.events.userEventsCarouselSection
import com.district37.toastmasters.features.account.components.ProfileBannerHeader
import com.district37.toastmasters.features.users.components.FriendActionButtons
import com.district37.toastmasters.features.users.components.OtherUserProfileTab
import com.district37.toastmasters.features.users.components.OtherUserProfileTabBar
import com.district37.toastmasters.features.users.components.OtherUserStatsRow
import com.district37.toastmasters.models.ActivityFeedConnection
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.models.UserRelationshipStatus
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.util.DisplayFormatters
import com.district37.toastmasters.util.Resource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Screen for viewing another user's profile
 * Redesigned with Twitter/LinkedIn-style layout matching own profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOtherUserProfileScreen(
    userId: String,
    onBackClick: () -> Unit
) {
    val viewModel: ViewOtherUserProfileViewModel = koinViewModel(key = userId) { parametersOf(userId) }
    val profileState by viewModel.profile.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Get the user's primary color for the gradient (when profile is loaded)
    val userPrimaryColor = (profileState as? Resource.Success)?.data?.user?.primaryColor
    val gradientColor = userPrimaryColor?.let { hexToColor(it) }

    // Configure the root TopAppBar with the viewed user's primary color
    ConfigureTopAppBar(
        config = TopAppBarConfig(
            showBackButton = true,
            gradientColor = gradientColor
        ),
        onBackClick = onBackClick
    )

    when (val state = profileState) {
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Could not load profile",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedButton(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
        }
        is Resource.Success -> {
            val userProfile = state.data

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                RefreshableContent(
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProfileContent(
                        user = userProfile.user,
                        relationshipStatus = userProfile.relationshipStatus,
                        attendingEvents = userProfile.attendingEvents,
                        activityFeed = userProfile.activityFeed,
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        actionInProgress = actionInProgress,
                        onSendRequest = { viewModel.sendFriendRequest() },
                        onCancelRequest = { requestId -> viewModel.cancelFriendRequest(requestId) },
                        onAcceptRequest = { requestId -> viewModel.acceptFriendRequest(requestId) },
                        onRejectRequest = { requestId -> viewModel.rejectFriendRequest(requestId) },
                        onRemoveFriend = { viewModel.removeFriend() }
                    )
                }
            }
        }
        is Resource.NotLoading -> {
            // Initial state
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileContent(
    user: User,
    relationshipStatus: UserRelationshipStatus,
    attendingEvents: UserEventsConnection?,
    activityFeed: ActivityFeedConnection?,
    selectedTab: OtherUserProfileTab,
    onTabSelected: (OtherUserProfileTab) -> Unit,
    actionInProgress: Boolean,
    onSendRequest: () -> Unit,
    onCancelRequest: (Int) -> Unit,
    onAcceptRequest: (Int) -> Unit,
    onRejectRequest: (Int) -> Unit,
    onRemoveFriend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = user.displayName ?: user.username ?: "User"
    val memberSince = user.createdAt?.let { DisplayFormatters.formatMemberSince(it) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Banner header (reusing ProfileBannerHeader from own profile)
        item(key = "banner") {
            ProfileBannerHeader(
                bannerImageUrl = null,
                profileImageUrl = user.profileImageUrl,
                displayName = displayName,
                username = user.username,
                memberSince = memberSince,
                bio = user.bio,
                onLongPressAvatar = { },
                primaryColor = user.primaryColor
            )
        }

        item(key = "spacer_after_banner") {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Stats row (attending count only)
        item(key = "stats") {
            OtherUserStatsRow(
                attendingCount = attendingEvents?.totalCount ?: 0
            )
        }

        item(key = "spacer_after_stats") {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Action buttons (Message + Friend status)
        item(key = "actions") {
            FriendActionButtons(
                userId = user.id,
                displayName = displayName,
                avatarUrl = user.profileImageUrl,
                relationshipStatus = relationshipStatus,
                actionInProgress = actionInProgress,
                onSendRequest = onSendRequest,
                onCancelRequest = onCancelRequest,
                onAcceptRequest = onAcceptRequest,
                onRejectRequest = onRejectRequest,
                onRemoveFriend = onRemoveFriend
            )
        }

        item(key = "spacer_after_actions") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky tab bar
        stickyHeader(key = "tabs") {
            OtherUserProfileTabBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        // Tab content
        when (selectedTab) {
            OtherUserProfileTab.EVENTS -> {
                if (attendingEvents != null) {
                    // Show attending events carousel
                    userEventsCarouselSection(
                        title = "Attending",
                        eventsState = Resource.Success(attendingEvents),
                        onViewAllClick = {
                            // TODO: Navigate to full attending events list
                        },
                        emptyMessage = "No attending events"
                    )
                } else {
                    // Show locked message for non-friends
                    item(key = "events_locked") {
                        LockedContentMessage(
                            message = "Add as friend to see events",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
            OtherUserProfileTab.ACTIVITY -> {
                if (activityFeed != null) {
                    val items = activityFeed.items
                    if (items.isEmpty()) {
                        item(key = "activity_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = items,
                            key = { "activity_${it.id}" }
                        ) { activityItem ->
                            ActivityFeedItemCard(
                                item = activityItem,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // Show locked message for non-friends
                    item(key = "activity_locked") {
                        LockedContentMessage(
                            message = "Add as friend to see activity",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item(key = "bottom_spacing") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Locked content message for non-friends
 */
@Composable
private fun LockedContentMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Converts a hex color string to a Compose Color.
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
