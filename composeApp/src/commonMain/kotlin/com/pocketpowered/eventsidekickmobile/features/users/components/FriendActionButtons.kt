package com.district37.toastmasters.features.users.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.messaging.MessageUserButton
import com.district37.toastmasters.models.UserRelationshipStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Action buttons section for other user profiles
 * Contains Message button + relationship-based friend action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendActionButtons(
    userId: String,
    displayName: String?,
    avatarUrl: String?,
    relationshipStatus: UserRelationshipStatus,
    actionInProgress: Boolean,
    onSendRequest: () -> Unit,
    onCancelRequest: (Int) -> Unit,
    onAcceptRequest: (Int) -> Unit,
    onRejectRequest: (Int) -> Unit,
    onRemoveFriend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelRequestSheet by remember { mutableStateOf(false) }
    var showIncomingRequestSheet by remember { mutableStateOf(false) }
    var showRemoveFriendSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val incomingRequestSheetState = rememberModalBottomSheetState()
    val removeFriendSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Message button - always shown
        MessageUserButton(
            userId = userId,
            displayName = displayName,
            avatarUrl = avatarUrl
        )

        // Friend action button - varies based on relationship status
        when (relationshipStatus) {
            is UserRelationshipStatus.NotFriends -> {
                Button(
                    onClick = onSendRequest,
                    enabled = !actionInProgress
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Friend")
                }
            }
            is UserRelationshipStatus.PendingOutgoing -> {
                RequestPendingButton(
                    onClick = { showCancelRequestSheet = true },
                    enabled = !actionInProgress
                )
            }
            is UserRelationshipStatus.PendingIncoming -> {
                IncomingRequestButton(
                    onClick = { showIncomingRequestSheet = true },
                    enabled = !actionInProgress
                )
            }
            is UserRelationshipStatus.Friends -> {
                FriendsChip(
                    onClick = { showRemoveFriendSheet = true },
                    enabled = !actionInProgress
                )
            }
        }
    }

    // Cancel friend request bottom sheet
    if (showCancelRequestSheet && relationshipStatus is UserRelationshipStatus.PendingOutgoing) {
        CancelFriendRequestBottomSheet(
            sheetState = sheetState,
            onDismiss = { showCancelRequestSheet = false },
            onCancelRequest = {
                scope.launch {
                    sheetState.hide()
                    showCancelRequestSheet = false
                    onCancelRequest(relationshipStatus.request.id)
                }
            }
        )
    }

    // Incoming friend request bottom sheet
    if (showIncomingRequestSheet && relationshipStatus is UserRelationshipStatus.PendingIncoming) {
        IncomingFriendRequestBottomSheet(
            sheetState = incomingRequestSheetState,
            onDismiss = { showIncomingRequestSheet = false },
            onAccept = {
                scope.launch {
                    incomingRequestSheetState.hide()
                    showIncomingRequestSheet = false
                    onAcceptRequest(relationshipStatus.request.id)
                }
            },
            onReject = {
                scope.launch {
                    incomingRequestSheetState.hide()
                    showIncomingRequestSheet = false
                    onRejectRequest(relationshipStatus.request.id)
                }
            }
        )
    }

    // Remove friend bottom sheet
    if (showRemoveFriendSheet && relationshipStatus is UserRelationshipStatus.Friends) {
        RemoveFriendBottomSheet(
            sheetState = removeFriendSheetState,
            onDismiss = { showRemoveFriendSheet = false },
            onRemoveFriend = {
                scope.launch {
                    removeFriendSheetState.hide()
                    showRemoveFriendSheet = false
                    onRemoveFriend()
                }
            }
        )
    }
}

/**
 * Friends chip that opens a bottom sheet when tapped
 */
@Composable
private fun FriendsChip(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Friends",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Button showing "Request Pending" status that opens a bottom sheet when tapped
 */
@Composable
private fun RequestPendingButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Request Pending",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Button showing incoming friend request that opens a bottom sheet when tapped
 */
@Composable
private fun IncomingRequestButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Friend Request",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Bottom sheet for canceling a friend request
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CancelFriendRequestBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCancelRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Friend Request",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel friend request option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCancelRequest)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Cancel friend request",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Bottom sheet for responding to an incoming friend request
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomingFriendRequestBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Friend Request",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Accept friend request option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAccept)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Accept friend request",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Reject friend request option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReject)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Reject friend request",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Bottom sheet for removing a friend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveFriendBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Friendship",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Remove friend option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRemoveFriend)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Remove friend",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
