package com.district37.toastmasters.features.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.RefreshableContent
import com.district37.toastmasters.infra.calendar.rememberCalendarPermissionLauncher
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalBottomNavInsets
import com.district37.toastmasters.navigation.LocalTopAppBarInsets

/**
 * Calendar settings screen for calendar sync preferences
 */
@Composable
fun CalendarSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCalendarSelection: () -> Unit,
    onNavigateToSyncedItems: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val autoSyncCalendar by viewModel.autoSyncCalendar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Calendar permission launcher
    val requestCalendarPermission = rememberCalendarPermissionLauncher { granted ->
        if (granted) {
            // Use onCalendarPermissionGranted to also trigger reinstall reconciliation if needed
            viewModel.onCalendarPermissionGranted()
        }
    }

    // Configure the TopAppBar for this screen
    ConfigureTopAppBar(
        config = AppBarConfigs.detailScreen(),
        onBackClick = onNavigateBack
    )

    val topBarInsets = LocalTopAppBarInsets.current
    val bottomNavInsets = LocalBottomNavInsets.current

    // Show error snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Load calendars to get the selected calendar name
    LaunchedEffect(Unit) {
        viewModel.refreshCalendars()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RefreshableContent(
            onRefresh = { viewModel.refreshCalendarSettings() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(topBarInsets.recommendedContentPadding))
                }

                // Header
                item {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }

                // Context text
                item {
                    Text(
                        text = "Manage how events are synced to your device calendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Calendar selection row
                item {
                    CalendarSelectionRow(
                        hasPermission = state.hasCalendarPermission,
                        selectedCalendarName = state.availableCalendars.find { it.id == state.selectedCalendarId }?.name,
                        isLoading = state.isLoadingCalendars,
                        onClick = {
                            if (state.hasCalendarPermission) {
                                onNavigateToCalendarSelection()
                            } else {
                                requestCalendarPermission()
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Auto-sync section
                item {
                    AutoSyncSection(
                        autoSyncEnabled = autoSyncCalendar,
                        onToggle = { viewModel.toggleAutoSyncCalendar() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sync Status section
                item {
                    SyncStatusSection(
                        syncedItemCount = state.syncedItemCount,
                        isSyncing = state.isSyncing,
                        lastSyncResult = state.lastSyncResult,
                        onSyncNow = { viewModel.syncNow() },
                        onViewSyncedItems = onNavigateToSyncedItems
                    )
                }

                // Bottom padding for nav bar
                item {
                    Spacer(modifier = Modifier.height(bottomNavInsets.recommendedContentPadding))
                }
            }
        }

        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun CalendarSelectionRow(
    hasPermission: Boolean,
    selectedCalendarName: String?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Selected Calendar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = when {
                    !hasPermission -> "Tap to grant calendar access"
                    isLoading -> "Loading..."
                    selectedCalendarName != null -> selectedCalendarName
                    else -> "No calendar selected"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Select calendar",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SyncStatusSection(
    syncedItemCount: Int,
    isSyncing: Boolean,
    lastSyncResult: String?,
    onSyncNow: () -> Unit,
    onViewSyncedItems: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sync Status",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        // View synced items row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewSyncedItems() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Synced Items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (syncedItemCount == 0) {
                        "No items synced to calendar"
                    } else {
                        "$syncedItemCount item${if (syncedItemCount != 1) "s" else ""} synced"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View synced items",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // Sync Now button
        OutlinedButton(
            onClick = onSyncNow,
            enabled = !isSyncing && syncedItemCount > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Syncing...")
            } else {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Sync Now")
            }
        }

        // Last sync result
        lastSyncResult?.let { result ->
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.contains("failed", ignoreCase = true) || result.contains("error", ignoreCase = true)) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
private fun AutoSyncSection(
    autoSyncEnabled: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Auto-Sync",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto-sync to calendar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Automatically sync RSVP'd items to your device calendar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = autoSyncEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
