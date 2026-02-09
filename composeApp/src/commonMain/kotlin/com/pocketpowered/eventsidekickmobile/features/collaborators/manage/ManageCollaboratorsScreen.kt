package com.district37.toastmasters.features.collaborators.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.CollaborationRequest
import com.district37.toastmasters.models.Collaborator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.district37.toastmasters.models.PermissionLevel
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCollaboratorsScreen(
    entityType: String,
    entityId: Int,
    entityName: String,
    onAddCollaborator: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ManageCollaboratorsViewModel = koinViewModel(parameters = { parametersOf(entityType, entityId) })
) {
    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(title = "Collaborators"),
        onBackClick = onDismiss
    )

    val collaborators by viewModel.collaborators.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val myPermission by viewModel.myPermission.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val showTransferOwnershipDialog by viewModel.showTransferOwnershipDialog.collectAsState()
    val showRemoveDialog by viewModel.showRemoveDialog.collectAsState()
    val showCancelRequestDialog by viewModel.showCancelRequestDialog.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val pendingRequestsLoading by viewModel.pendingRequestsLoading.collectAsState()

    // Determine if we're refreshing (initial load or pull-to-refresh)
    val isRefreshing = isLoading && collaborators.isEmpty() && pendingRequests.isEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val topBarInsets = LocalTopAppBarInsets.current

    // Load more when reaching end of list
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= collaborators.size - 3 && hasMore && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    // Handle action results
    LaunchedEffect(actionResult) {
        when (val result = actionResult) {
            is ManageCollaboratorsViewModel.ActionResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearActionResult()
            }
            is ManageCollaboratorsViewModel.ActionResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearActionResult()
            }
            null -> {}
        }
    }

    // Dialogs
    showPermissionDialog?.let { collaborator ->
        ChangePermissionDialog(
            collaborator = collaborator,
            onDismiss = { viewModel.dismissPermissionDialog() },
            onConfirm = { newLevel ->
                viewModel.updatePermission(collaborator.id, newLevel)
            }
        )
    }

    showTransferOwnershipDialog?.let { collaborator ->
        TransferOwnershipDialog(
            collaborator = collaborator,
            entityName = entityName,
            onDismiss = { viewModel.dismissTransferOwnershipDialog() },
            onConfirm = {
                viewModel.transferOwnership(collaborator.userId)
            }
        )
    }

    showRemoveDialog?.let { collaborator ->
        RemoveCollaboratorDialog(
            collaborator = collaborator,
            onDismiss = { viewModel.dismissRemoveDialog() },
            onConfirm = {
                viewModel.removeCollaborator(collaborator.id)
            }
        )
    }

    showCancelRequestDialog?.let { request ->
        CancelRequestDialog(
            request = request,
            onDismiss = { viewModel.dismissCancelRequestDialog() },
            onConfirm = {
                viewModel.cancelPendingRequest(request.id)
            }
        )
    }

    val canManage = myPermission?.canManageCollaborators == true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = onAddCollaborator,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add") }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading || pendingRequestsLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Entity info
                Text(
                    text = "Managing: $entityName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                actionInProgress != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (actionInProgress) {
                                    ManageCollaboratorsViewModel.ActionState.UpdatingPermission -> "Updating permission..."
                                    ManageCollaboratorsViewModel.ActionState.Removing -> "Removing collaborator..."
                                    ManageCollaboratorsViewModel.ActionState.TransferringOwnership -> "Transferring ownership..."
                                    ManageCollaboratorsViewModel.ActionState.CancellingRequest -> "Cancelling request..."
                                    null -> ""
                                }
                            )
                        }
                    }
                }
                error != null && collaborators.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error ?: "An error occurred",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.loadCollaborators() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                collaborators.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No collaborators yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (canManage) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to invite someone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topBarInsets.recommendedContentPadding, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pending requests section
                        if (pendingRequests.isNotEmpty()) {
                            item(key = "pending_header") {
                                Text(
                                    text = "Pending Requests",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            items(pendingRequests, key = { "pending_${it.id}" }) { request ->
                                PendingRequestItem(
                                    request = request,
                                    onCancel = { viewModel.showCancelRequestDialog(request) }
                                )
                            }

                            // Divider between sections
                            item(key = "divider") {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Collaborators section header
                        if (collaborators.isNotEmpty()) {
                            item(key = "collaborators_header") {
                                Text(
                                    text = "Collaborators",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        items(collaborators, key = { it.id }) { collaborator ->
                            CollaboratorItem(
                                collaborator = collaborator,
                                canManage = canManage,
                                isOwner = myPermission?.isOwner == true,
                                onChangePermission = { viewModel.showChangePermissionDialog(collaborator) },
                                onTransferOwnership = { viewModel.showTransferOwnershipDialog(collaborator) },
                                onRemove = { viewModel.showRemoveDialog(collaborator) }
                            )
                        }

                        if (isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun CollaboratorItem(
    collaborator: Collaborator,
    canManage: Boolean,
    isOwner: Boolean,
    onChangePermission: () -> Unit,
    onTransferOwnership: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            if (collaborator.profileImageUrl != null) {
                CoilImage(
                    imageModel = { collaborator.profileImageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        contentDescription = "Profile picture"
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = collaborator.displayName ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (collaborator.isOwner) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Owner",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = collaborator.permissionLevelDisplay,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )
            }

            // Actions menu (only show if user can manage and this isn't the owner)
            if (canManage && !collaborator.isOwner) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Permission") },
                            onClick = {
                                showMenu = false
                                onChangePermission()
                            }
                        )
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("Transfer Ownership") },
                                onClick = {
                                    showMenu = false
                                    onTransferOwnership()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onRemove()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangePermissionDialog(
    collaborator: Collaborator,
    onDismiss: () -> Unit,
    onConfirm: (PermissionLevel) -> Unit
) {
    val currentLevel = PermissionLevel.fromValue(collaborator.permissionLevel)
    var selectedLevel by remember { mutableStateOf(currentLevel ?: PermissionLevel.EDITOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Permission") },
        text = {
            Column {
                Text(
                    text = "Select new permission level for ${collaborator.displayName ?: "this user"}:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PermissionLevel.entries.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLevel = level }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = level.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (level) {
                                    PermissionLevel.EDITOR -> "Can edit the content"
                                    PermissionLevel.ADMIN -> "Can edit and manage collaborators"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedLevel) },
                enabled = selectedLevel != currentLevel
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TransferOwnershipDialog(
    collaborator: Collaborator,
    entityName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Ownership") },
        text = {
            Text(
                "Are you sure you want to transfer ownership of \"$entityName\" to ${collaborator.displayName ?: "this user"}?\n\n" +
                        "You will become an Admin after the transfer."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RemoveCollaboratorDialog(
    collaborator: Collaborator,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Collaborator") },
        text = {
            Text("Are you sure you want to remove ${collaborator.displayName ?: "this user"} as a collaborator?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PendingRequestItem(
    request: CollaborationRequest,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            if (request.receiverProfileImageUrl != null) {
                CoilImage(
                    imageModel = { request.receiverProfileImageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        contentDescription = "Profile picture"
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.receiverDisplayName ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = "Pending",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = request.permissionLevelDisplay,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Cancel button
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel request",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CancelRequestDialog(
    request: CollaborationRequest,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Request") },
        text = {
            Text("Cancel the collaboration request to ${request.receiverDisplayName ?: "this user"}?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cancel Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep")
            }
        }
    )
}
