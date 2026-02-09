package com.district37.toastmasters.features.organizations.members

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.district37.toastmasters.models.OrganizationMember
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import androidx.compose.foundation.layout.PaddingValues
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrganizationMembersScreen(
    organizationId: Int,
    organizationName: String,
    onBackClick: () -> Unit,
    onInviteMember: () -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: ManageOrganizationMembersViewModel = koinViewModel(
        key = "manage_org_members_$organizationId"
    ) { parametersOf(organizationId) }
) {
    // Handle external refresh trigger (e.g., after sending invitation)
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    ConfigureTopAppBar(
        config = AppBarConfigs.titleScreen(title = "Members"),
        onBackClick = onBackClick
    )

    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val myRole by viewModel.myRole.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    val showRoleDialog by viewModel.showRoleDialog.collectAsState()
    val showRemoveDialog by viewModel.showRemoveDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val topBarInsets = LocalTopAppBarInsets.current

    // Handle action results
    LaunchedEffect(actionResult) {
        when (val result = actionResult) {
            is ManageOrganizationMembersViewModel.ActionResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearActionResult()
            }
            is ManageOrganizationMembersViewModel.ActionResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.clearActionResult()
            }
            null -> {}
        }
    }

    // Dialogs
    showRoleDialog?.let { member ->
        ChangeRoleDialog(
            member = member,
            currentUserRole = myRole,
            onDismiss = { viewModel.dismissRoleDialog() },
            onConfirm = { newRole ->
                viewModel.updateMemberRole(member.userId, newRole)
            }
        )
    }

    showRemoveDialog?.let { member ->
        RemoveMemberDialog(
            member = member,
            onDismiss = { viewModel.dismissRemoveDialog() },
            onConfirm = {
                viewModel.removeMember(member.userId)
            }
        )
    }

    val canManage = viewModel.canManageMembers()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = onInviteMember,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Invite") }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && members.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null && members.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Unknown error")
                            TextButton(onClick = { viewModel.loadMembers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                members.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No members yet")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topBarInsets.recommendedContentPadding, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(members, key = { it.id }) { member ->
                            MemberCard(
                                member = member,
                                canChangeRole = viewModel.canChangeRole(member),
                                canRemove = viewModel.canRemoveMember(member),
                                onChangeRole = { viewModel.showChangeRoleDialog(member) },
                                onRemove = { viewModel.showRemoveDialog(member) },
                                isActionInProgress = actionInProgress != null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: OrganizationMember,
    canChangeRole: Boolean,
    canRemove: Boolean,
    onChangeRole: () -> Unit,
    onRemove: () -> Unit,
    isActionInProgress: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.userProfile?.profileImageUrl != null) {
                        CoilImage(
                            imageModel = { member.userProfile.profileImageUrl },
                            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = member.userProfile?.displayName ?: "Unknown User",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RoleChip(role = member.role)
                }
            }

            // Action buttons
            Row {
                if (canChangeRole) {
                    IconButton(
                        onClick = onChangeRole,
                        enabled = !isActionInProgress
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change role",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        enabled = !isActionInProgress
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove member",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(role: OrganizationRole) {
    val (icon, label, color) = when (role) {
        OrganizationRole.OWNER -> Triple(Icons.Default.Star, "Owner", MaterialTheme.colorScheme.primary)
        OrganizationRole.ADMIN -> Triple(Icons.Default.Edit, "Admin", MaterialTheme.colorScheme.secondary)
        OrganizationRole.MEMBER -> Triple(Icons.Default.Person, "Member", MaterialTheme.colorScheme.outline)
    }

    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    )
}

@Composable
private fun ChangeRoleDialog(
    member: OrganizationMember,
    currentUserRole: OrganizationRole?,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(member.role) }

    // Available roles depend on current user's role
    val availableRoles = when (currentUserRole) {
        OrganizationRole.OWNER -> listOf(OrganizationRole.ADMIN, OrganizationRole.MEMBER)
        OrganizationRole.ADMIN -> listOf(OrganizationRole.MEMBER)
        else -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Role") },
        text = {
            Column {
                Text("Select a new role for ${member.userProfile?.displayName ?: "this member"}:")
                Spacer(modifier = Modifier.height(16.dp))
                availableRoles.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(role.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedRole) },
                enabled = selectedRole != member.role
            ) {
                Text("Change")
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
private fun RemoveMemberDialog(
    member: OrganizationMember,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Member") },
        text = {
            Text("Are you sure you want to remove ${member.userProfile?.displayName ?: "this member"} from the organization?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
