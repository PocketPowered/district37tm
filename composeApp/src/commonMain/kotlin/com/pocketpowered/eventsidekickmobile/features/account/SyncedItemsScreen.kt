package com.district37.toastmasters.features.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.infra.calendar.LocalAgendaItemSyncRecord
import com.district37.toastmasters.infra.calendar.LocalCalendarSyncRecord
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalBottomNavInsets
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.util.DateTimeFormatter

/**
 * Screen showing all items currently synced to the device calendar.
 */
@Composable
fun SyncedItemsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (Int) -> Unit,
    onNavigateToAgendaItem: (Int) -> Unit
) {
    val syncedEvents by viewModel.syncedEvents.collectAsState()
    val syncedAgendaItems by viewModel.syncedAgendaItems.collectAsState()

    // Configure the TopAppBar for this screen
    ConfigureTopAppBar(
        config = AppBarConfigs.detailScreen(),
        onBackClick = onNavigateBack
    )

    val topBarInsets = LocalTopAppBarInsets.current
    val bottomNavInsets = LocalBottomNavInsets.current

    val totalItems = syncedEvents.size + syncedAgendaItems.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(topBarInsets.recommendedContentPadding))
            }

            // Header
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Synced Items",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (totalItems == 0) {
                            "No items synced to your calendar yet."
                        } else {
                            "$totalItems item${if (totalItems != 1) "s" else ""} synced to your device calendar"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Empty state
            if (totalItems == 0) {
                item {
                    EmptyState()
                }
            }

            // Synced Events section
            if (syncedEvents.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Events",
                        count = syncedEvents.size
                    )
                }

                items(syncedEvents.values.toList()) { record ->
                    SyncedEventItem(
                        record = record,
                        onClick = { onNavigateToEvent(record.eventId) }
                    )
                }
            }

            // Synced Agenda Items section
            if (syncedAgendaItems.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Schedule Items",
                        count = syncedAgendaItems.size
                    )
                }

                items(syncedAgendaItems.values.toList()) { record ->
                    SyncedAgendaItemItem(
                        record = record,
                        onClick = { onNavigateToAgendaItem(record.agendaItemId) }
                    )
                }
            }

            // Bottom padding for nav bar
            item {
                Spacer(modifier = Modifier.height(bottomNavInsets.recommendedContentPadding))
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Add events or schedule items to your calendar using the calendar button on detail screens.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Text(
        text = "$title ($count)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SyncedEventItem(
    record: LocalCalendarSyncRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                val dateTimeText = record.startTime?.let { DateTimeFormatter.formatDateWithTime(it) }
                    ?: "Synced to calendar"
                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

@Composable
private fun SyncedAgendaItemItem(
    record: LocalAgendaItemSyncRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                val dateTimeText = record.startTime?.let { DateTimeFormatter.formatDateWithTime(it) }
                    ?: "Synced to calendar"
                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule Item",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        )
    }
}
