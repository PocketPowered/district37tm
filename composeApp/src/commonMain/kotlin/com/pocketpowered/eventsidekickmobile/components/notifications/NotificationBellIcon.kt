package com.district37.toastmasters.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.district37.toastmasters.features.notifications.NotificationViewModel
import org.koin.compose.koinInject

/**
 * Notification bell icon with unread count badge.
 * Uses the shared NotificationViewModel singleton to display the unread count.
 */
@Composable
fun NotificationBellIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val unreadCount = uiState.unreadCount

    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (unreadCount > 0) {
                    Icons.Filled.Notifications
                } else {
                    Icons.Outlined.Notifications
                },
                contentDescription = if (unreadCount > 0) {
                    "$unreadCount unread notifications"
                } else {
                    "Notifications"
                }
            )
        }

        // Badge with unread count
        if (unreadCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Standalone notification badge for use in other contexts
 */
@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                color = MaterialTheme.colorScheme.onError,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
