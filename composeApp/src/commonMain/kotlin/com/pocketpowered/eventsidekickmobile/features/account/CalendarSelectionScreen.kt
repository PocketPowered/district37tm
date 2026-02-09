package com.district37.toastmasters.features.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.infra.calendar.CalendarInfo
import com.district37.toastmasters.infra.calendar.rememberCalendarPermissionLauncher
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalBottomNavInsets
import com.district37.toastmasters.navigation.LocalTopAppBarInsets

/**
 * Screen for selecting which calendar to sync events to.
 * Shows available device calendars and allows the user to select one.
 */
@Composable
fun CalendarSelectionScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Calendar permission launcher
    val requestCalendarPermission = rememberCalendarPermissionLauncher { granted ->
        if (granted) {
            viewModel.refreshCalendars()
        }
    }

    // Configure the TopAppBar for this screen
    ConfigureTopAppBar(
        config = AppBarConfigs.detailScreen(),
        onBackClick = onNavigateBack
    )

    val topBarInsets = LocalTopAppBarInsets.current
    val bottomNavInsets = LocalBottomNavInsets.current

    // Auto-request permission when needed
    LaunchedEffect(state.needsCalendarPermission) {
        if (state.needsCalendarPermission) {
            requestCalendarPermission()
            viewModel.dismissPermissionRequest()
        }
    }

    // Load calendars when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshCalendars()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(topBarInsets.recommendedContentPadding))

            // Header
            Text(
                text = "Select Calendar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

            // Description
            Text(
                text = "Choose which calendar to add events to when using 'Add to Calendar'.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // Calendar list content
            CalendarListContent(
                hasPermission = state.hasCalendarPermission,
                availableCalendars = state.availableCalendars,
                selectedCalendarId = state.selectedCalendarId,
                isLoading = state.isLoadingCalendars,
                onCalendarSelected = { calendarId ->
                    viewModel.onCalendarSelected(calendarId)
                    onNavigateBack()
                },
                onRequestPermission = { requestCalendarPermission() }
            )

            // Bottom padding for nav bar
            Spacer(modifier = Modifier.height(bottomNavInsets.recommendedContentPadding))
        }
    }
}

@Composable
private fun CalendarListContent(
    hasPermission: Boolean,
    availableCalendars: List<CalendarInfo>,
    selectedCalendarId: String?,
    isLoading: Boolean,
    onCalendarSelected: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            !hasPermission -> {
                // No permission state
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Calendar access is required to sync events to your device calendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    OutlinedButton(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Calendar Access")
                    }
                }
            }
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
            availableCalendars.isEmpty() -> {
                // No calendars found
                Text(
                    text = "No calendars found on this device. Please create a calendar in your device's calendar app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> {
                // Calendar list
                availableCalendars.forEach { calendar ->
                    CalendarSelectionRow(
                        calendar = calendar,
                        isSelected = selectedCalendarId == calendar.id,
                        onClick = { onCalendarSelected(calendar.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSelectionRow(
    calendar: CalendarInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = calendar.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
            calendar.accountName?.let { accountName ->
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            if (calendar.isPrimary) {
                Text(
                    text = "Primary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
