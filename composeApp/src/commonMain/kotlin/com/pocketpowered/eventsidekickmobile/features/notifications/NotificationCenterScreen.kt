package com.district37.toastmasters.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.infra.rememberNotificationPermissionLauncher
import com.district37.toastmasters.models.Notification
import com.district37.toastmasters.models.NotificationType
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNotificationClick: (Notification) -> Unit,
    viewModel: NotificationViewModel = koinInject()
) {
    val navController = LocalNavController.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val topBarInsets = LocalTopAppBarInsets.current

    // Permission launcher for notification permission
    val requestNotificationPermission = rememberNotificationPermissionLauncher { granted ->
        viewModel.onPermissionResult(granted)
    }

    // Load notifications when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }

    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(
            title = "Notifications",
            actions = if (uiState.notifications.any { !it.isRead }) {
                {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Mark all read")
                    }
                }
            } else null
        ),
        onBackClick = { navController.popBackStack() }
    )

    // Load more when reaching end of list
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= uiState.notifications.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMore && !uiState.isLoadingMore) {
            viewModel.loadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.notifications.isEmpty() -> {
                EmptyNotificationsState(
                    onEnablePush = requestNotificationPermission,
                    pushEnabled = uiState.pushPermissionGranted
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topBarInsets.recommendedContentPadding)
                ) {
                    // Show enable push notifications banner if not enabled
                    if (!uiState.pushPermissionGranted) {
                        item {
                            EnablePushNotificationsBanner(
                                onClick = requestNotificationPermission
                            )
                        }
                    }

                    items(
                        items = uiState.notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id)
                                }
                                onNotificationClick(notification)
                            }
                        )
                        HorizontalDivider()
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.isRead) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Notification image or fallback icon
        if (notification.imageUrl != null) {
            CoilImage(
                imageModel = { notification.imageUrl },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop
                ),
                failure = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (!notification.isRead) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.notificationType.toIcon(),
                    contentDescription = null,
                    tint = if (!notification.isRead) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                color = if (!notification.isRead) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = notification.createdAt.toRelativeTimeString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Unread indicator
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@Composable
private fun EmptyNotificationsState(
    onEnablePush: () -> Unit,
    pushEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (pushEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Follow events, venues, and performers to get notified about updates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (!pushEnabled) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onEnablePush) {
                Text("Enable Push Notifications")
            }
        }
    }
}

@Composable
private fun EnablePushNotificationsBanner(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable push notifications",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Tap to get notified about events you follow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun NotificationType.toIcon() = when (this) {
    NotificationType.EVENT_RESCHEDULED,
    NotificationType.EVENT_VENUE_CHANGED,
    NotificationType.EVENT_CANCELLED -> Icons.Default.Notifications
    NotificationType.VENUE_ADDRESS_CHANGED -> Icons.Default.Notifications
    NotificationType.PERFORMER_ADDED_TO_EVENT,
    NotificationType.PERFORMER_REMOVED_FROM_EVENT -> Icons.Default.Notifications
    NotificationType.ORGANIZATION_UPDATED -> Icons.Default.Notifications
    NotificationType.AGENDA_ITEM_UPDATED -> Icons.Default.Notifications
    NotificationType.GENERAL,
    NotificationType.UNKNOWN -> Icons.Default.Notifications
}

@OptIn(ExperimentalTime::class)
private fun Instant.toRelativeTimeString(): String {
    val now = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
    val period = this.periodUntil(now, TimeZone.currentSystemDefault())

    return when {
        period.years > 0 -> "${period.years}y ago"
        period.months > 0 -> "${period.months}mo ago"
        period.days > 0 -> "${period.days}d ago"
        period.hours > 0 -> "${period.hours}h ago"
        period.minutes > 0 -> "${period.minutes}m ago"
        else -> "Just now"
    }
}
