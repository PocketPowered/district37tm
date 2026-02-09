package com.district37.toastmasters.features.schedules.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import androidx.compose.foundation.layout.PaddingValues
import com.district37.toastmasters.util.DateTimeFormatter
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ManageAgendaItemsScreen(
    eventId: Int,
    onBackClick: () -> Unit,
    onCreateAgendaItem: () -> Unit,
    onEditAgendaItem: (agendaItemId: Int) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {
    val viewModel: ManageAgendaItemsViewModel = koinViewModel(
        key = "manage_agenda_items_$eventId"
    ) { parametersOf(eventId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val event by viewModel.event.collectAsState()
    val agendaItems by viewModel.agendaItems.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()

    var itemToDelete by remember { mutableStateOf<AgendaItem?>(null) }
    val topBarInsets = LocalTopAppBarInsets.current

    // Handle refresh signal from navigation
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadData()
            onRefreshHandled()
        }
    }

    // Configure the top app bar
    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(
            title = "Manage Agenda Items",
            subtitle = event?.name
        ),
        onBackClick = onBackClick
    )

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Agenda Item") },
            text = { Text("Are you sure you want to delete \"${item.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAgendaItem(item.id)
                        itemToDelete = null
                    },
                    enabled = !isDeleting
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete error dialog
    if (deleteError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteError() },
            title = { Text("Error") },
            text = { Text(deleteError ?: "Failed to delete agenda item") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!isLoading && loadError == null) {
                FloatingActionButton(onClick = onCreateAgendaItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add Agenda Item")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                loadError != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = loadError ?: "Failed to load agenda items",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                agendaItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No agenda items yet",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tap the + button to add an item",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topBarInsets.recommendedContentPadding, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(agendaItems, key = { it.id }) { item ->
                            AgendaItemManagementCard(
                                item = item,
                                onEdit = { onEditAgendaItem(item.id) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }

            // Deleting overlay
            if (isDeleting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AgendaItemManagementCard(
    item: AgendaItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (item.startTime != null || item.endTime != null) {
                        Text(
                            text = buildString {
                                item.startTime?.let { start ->
                                    append(DateTimeFormatter.formatSimplifiedDate(start))
                                    append(" ")
                                    append(DateTimeFormatter.formatTime(start))
                                }
                                if (item.startTime != null && item.endTime != null) {
                                    append(" - ")
                                }
                                item.endTime?.let { append(DateTimeFormatter.formatTime(it)) }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (item.description != null) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (item.location != null) {
                Text(
                    text = "Location: ${item.location.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.performers.isNotEmpty()) {
                Text(
                    text = "Performers: ${item.performers.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
