package com.district37.toastmasters.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

expect class NotificationPermissions {
    val permissionState: StateFlow<NotificationPermissionState>

    suspend fun requestPermission()

    fun openNotificationSettings()
}

@Composable
fun EnableNotificationsBanner(onEnableNotifsClicked: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Enable push-notifications to stay up to date on the latest event changes!",
                style = MaterialTheme.typography.body1
            )
            TextButton(
                onClick = onEnableNotifsClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Enable Notifications")
            }
        }
    }
}

@Composable
fun NotificationPermissionPrompt(
    onDismiss: () -> Unit
) {
    val appViewModel = LocalAppViewModel.current
    val permissionState by appViewModel.notificationsSlice.notificationPermissionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    when (permissionState) {
        NotificationPermissionState.NotDetermined -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Enable Notifications") },
                text = { Text("Stay up to date with the latest District 37 Toastmasters news and events by enabling notifications.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                appViewModel.notificationsSlice.requestNotificationPermission()
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Not Now")
                    }
                }
            )
        }

        NotificationPermissionState.Denied -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Notifications Disabled") },
                text = { Text("To receive notifications, please enable them in your device settings.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            appViewModel.notificationsSlice.openNotificationSettings()
                            onDismiss()
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        NotificationPermissionState.Granted -> {
            // No prompt needed
        }
    }
} 