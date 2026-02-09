package com.district37.toastmasters.features.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.auth.data.TokenManager
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.infra.ClipboardManager
import com.district37.toastmasters.infra.DeveloperSettingsManager
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DeveloperScreen(
    tokenManager: TokenManager = koinInject(),
    clipboardManager: ClipboardManager = koinInject(),
    developerSettingsManager: DeveloperSettingsManager = koinInject(),
    notificationRepository: NotificationRepository = koinInject(),
    eventRepository: EventRepository = koinInject()
) {
    var accessToken by remember { mutableStateOf<String?>(null) }
    var refreshToken by remember { mutableStateOf<String?>(null) }
    var useLocalhostServer by remember { mutableStateOf(false) }
    var isSendingTestNotification by remember { mutableStateOf(false) }
    var isCreatingTestEvent by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarInsets = LocalTopAppBarInsets.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    // Load tokens and settings on screen enter
    LaunchedEffect(Unit) {
        val tokens = tokenManager.getTokens()
        accessToken = tokens?.accessToken
        refreshToken = tokens?.refreshToken
        useLocalhostServer = developerSettingsManager.getUseLocalhostServer()
    }

    // Configure the top app bar for this screen
    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(title = "Developer Tools"),
        onBackClick = { navController.popBackStack() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topBarInsets.recommendedContentPadding)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Debug build only - not available in release",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Test Onboarding Button
            Button(
                onClick = {
                    scope.launch {
                        developerSettingsManager.setShouldShowOnboardingOnNextLaunch(true)
                        snackbarHostState.showSnackbar(
                            "Onboarding will show on next app launch"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Onboarding (Restart Required)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Push Notification Button
            Button(
                onClick = {
                    if (!isSendingTestNotification) {
                        isSendingTestNotification = true
                        scope.launch {
                            val result = notificationRepository.sendTestNotification()
                            isSendingTestNotification = false
                            when (result) {
                                is Resource.Success -> {
                                    if (result.data) {
                                        snackbarHostState.showSnackbar(
                                            "Test notification sent! Check your device."
                                        )
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            "Failed to send test notification"
                                        )
                                    }
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(
                                        "Error: ${result.message}"
                                    )
                                }
                                is Resource.Loading, is Resource.NotLoading -> { /* no-op */ }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSendingTestNotification
            ) {
                Text(if (isSendingTestNotification) "Sending..." else "Send Test Push Notification")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Test Upcoming Event Button
            Button(
                onClick = {
                    if (!isCreatingTestEvent) {
                        isCreatingTestEvent = true
                        scope.launch {
                            val result = eventRepository.createTestUpcomingEvent()
                            isCreatingTestEvent = false
                            when (result) {
                                is Resource.Success -> {
                                    snackbarHostState.showSnackbar(
                                        "Test event created: ${result.data.name}"
                                    )
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(
                                        "Error: ${result.message}"
                                    )
                                }
                                is Resource.Loading, is Resource.NotLoading -> { /* no-op */ }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreatingTestEvent
            ) {
                Text(if (isCreatingTestEvent) "Creating..." else "Create Test Upcoming Event (24h)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Localhost Server Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Use Localhost Server",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Restart required after changing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = useLocalhostServer,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                developerSettingsManager.setUseLocalhostServer(enabled)
                                useLocalhostServer = enabled
                                snackbarHostState.showSnackbar(
                                    if (enabled) "Localhost server enabled - restart app to apply"
                                    else "Production server enabled - restart app to apply"
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Access Token Card
            TokenCard(
                title = "Access Token",
                token = accessToken,
                onCopy = { token ->
                    clipboardManager.copyToClipboard(token)
                    scope.launch {
                        snackbarHostState.showSnackbar("Access token copied to clipboard")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Token Card
            TokenCard(
                title = "Refresh Token",
                token = refreshToken,
                onCopy = { token ->
                    clipboardManager.copyToClipboard(token)
                    scope.launch {
                        snackbarHostState.showSnackbar("Refresh token copied to clipboard")
                    }
                }
            )
        }
    }
}

@Composable
private fun TokenCard(
    title: String,
    token: String?,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (token != null) {
                    IconButton(onClick = { onCopy(token) }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy $title",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (token != null) {
                SelectionContainer {
                    Text(
                        text = token,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "No token available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
