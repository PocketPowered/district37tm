package com.district37.toastmasters.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController
import kotlinx.coroutines.launch

@Composable
fun NotificationOnboardingScreen(modifier: Modifier = Modifier) {
    val appViewModel = LocalAppViewModel.current
    val navController = LocalNavHostController.current
    val coroutineScope = rememberCoroutineScope()

    fun continueToAgenda() {
        appViewModel.navigate(
            navigationController = navController,
            navigationKey = NavigationItemKey.EVENT_LIST,
            removeSelfFromStack = true
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Enable notifications to get real-time updates and announcements",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        appViewModel.completeNotificationOnboarding()
                        appViewModel.notificationsSlice.requestNotificationPermission()
                        continueToAgenda()
                    }
                }
            ) {
                Text("Enable now")
            }

            TextButton(
                onClick = {
                    appViewModel.completeNotificationOnboarding()
                    continueToAgenda()
                }
            ) {
                Text(
                    text = "I'll do it later",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
