package com.district37.toastmasters.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController

@Composable
fun NotificationsEntry(modifier: Modifier = Modifier) {
    val appViewModel = LocalAppViewModel.current
    val navController = LocalNavHostController.current
    val unseenNotificationCount by
        appViewModel.notificationsSlice.unseenNotificationCount.collectAsState(0)
    Box(modifier = modifier) {
        IconButton(
            {
                appViewModel.navigate(
                    navController,
                    NavigationItemKey.NOTIFICATIONS
                )
            },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            if (unseenNotificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Red, shape = CircleShape)
                    )
                    Text(
                        text = unseenNotificationCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}