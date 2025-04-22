package com.district37.toastmasters.notifications

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.navigation.StatefulScaffold
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.Resource
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NotificationsScreen() {
    val appViewModel = LocalAppViewModel.current
    val navController = LocalNavHostController.current
    val notifications by appViewModel.notificationsSlice.notificationsFlow.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    StatefulScaffold(
        title = "Notifications",
        resource = Resource.Success(notifications),
        actions = {
            IconButton(onClick = {
                coroutineScope.launch {
                    appViewModel.notificationsSlice.clearAllNotifications()
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear all notifications")
            }
        }
    ) { notifs ->
        if (notifs.isEmpty()) {
            Text(
                "No notifications available at this time!",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifs.size, key = { notifs[it].id }) { idx ->
                val notification = notifs[idx]
                NotificationItem(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                        .onGloballyPositioned {
                            coroutineScope.launch {
                                appViewModel.notificationsSlice.markNotificationAsSeen(notification.id)
                            }
                        },
                    notification = notification,
                    onDelete = { id ->
                        coroutineScope.launch {
                            appViewModel.notificationsSlice.deleteNotification(id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    modifier: Modifier = Modifier,
    notification: Notification,
    onDelete: (Long) -> Unit
) {
    val isSeen by remember { mutableStateOf(notification.seen) }
    Card(
        elevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.header,
                        style = MaterialTheme.typography.h6
                    )
                    if (!isSeen) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.overline,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colors.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = { onDelete(notification.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete notification")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.description,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Received: ${notification.timeReceived.toLocalDateTime(TimeZone.currentSystemDefault())}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}