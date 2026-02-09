package com.district37.toastmasters.components.engagement

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.engagement.EngagementBehavior
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.FriendRsvp
import com.district37.toastmasters.models.UserEngagement

/**
 * Creates a modifier that applies a scale bounce animation when the trigger state changes.
 * The animation scales up to 1.15x then springs back to 1.0x.
 */
@Composable
fun Modifier.scaleBounceOnChange(
    key: Any?,
    scaleAmount: Float = 1.15f,
    tweenDuration: Int = 100,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessMedium
): Modifier {
    val scale = remember { Animatable(1f) }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        if (hasInitialized) {
            scale.animateTo(
                targetValue = scaleAmount,
                animationSpec = tween(durationMillis = tweenDuration)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness
                )
            )
        } else {
            hasInitialized = true
        }
    }

    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Subscribe button for engagement actions on any entity type.
 * Uses Bell notification icon as subscribing means receiving notifications for changes.
 */
@Composable
fun SubscribeButton(
    isSubscribed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedModifier = modifier.scaleBounceOnChange(key = isSubscribed)

    val buttonColors = if (isSubscribed) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    if (isSubscribed) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = animatedModifier,
            colors = buttonColors
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Subscribed",
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = animatedModifier,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Subscribe",
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Status dropdown button for setting engagement status (RSVP for events/agenda items)
 */
@Composable
fun StatusDropdownButton(
    currentStatus: UserEngagementStatus?,
    enabled: Boolean,
    onStatusSelected: (UserEngagementStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when (currentStatus) {
        UserEngagementStatus.GOING -> "Going"
        UserEngagementStatus.NOT_GOING -> "Not Going"
        UserEngagementStatus.UNDECIDED -> "Undecided"
        else -> "RSVP"
    }

    // Use same colors as StatusIndicator for consistency
    val containerColor = when (currentStatus) {
        UserEngagementStatus.GOING -> Color(0xFF4CAF50)      // Green
        UserEngagementStatus.NOT_GOING -> Color(0xFFF44336)  // Red
        UserEngagementStatus.UNDECIDED -> Color(0xFFFFC107)  // Yellow/Amber
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (currentStatus) {
        UserEngagementStatus.GOING -> Color.White
        UserEngagementStatus.NOT_GOING -> Color.White
        UserEngagementStatus.UNDECIDED -> Color.Black        // Dark text on yellow for readability
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Add border when no status is selected to make it look more like a button
    val borderStroke = if (currentStatus == null) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    } else {
        null
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            color = if (enabled) containerColor else containerColor.copy(alpha = 0.38f),
            contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.38f),
            shape = RoundedCornerShape(24.dp),
            border = borderStroke,
            modifier = Modifier.scaleBounceOnChange(
                key = currentStatus,
                scaleAmount = 1.12f,
                tweenDuration = 90,
                dampingRatio = Spring.DampingRatioMediumBouncy * 0.8f
            )
        ) {
            Text(
                text = displayText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Going") },
                onClick = {
                    expanded = false
                    onStatusSelected(UserEngagementStatus.GOING)
                },
                leadingIcon = {
                    StatusIndicator(status = UserEngagementStatus.GOING, size = 12)
                }
            )
            DropdownMenuItem(
                text = { Text("Not Going") },
                onClick = {
                    expanded = false
                    onStatusSelected(UserEngagementStatus.NOT_GOING)
                },
                leadingIcon = {
                    StatusIndicator(status = UserEngagementStatus.NOT_GOING, size = 12)
                }
            )
            DropdownMenuItem(
                text = { Text("Undecided") },
                onClick = {
                    expanded = false
                    onStatusSelected(UserEngagementStatus.UNDECIDED)
                },
                leadingIcon = {
                    StatusIndicator(status = UserEngagementStatus.UNDECIDED, size = 12)
                }
            )
            if (currentStatus != null) {
                DropdownMenuItem(
                    text = { Text("Clear Status") },
                    onClick = {
                        expanded = false
                        onStatusSelected(null)
                    }
                )
            }
        }
    }
}

/**
 * Status indicator dot
 */
@Composable
fun StatusIndicator(
    status: UserEngagementStatus?,
    size: Int = 8,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        UserEngagementStatus.GOING -> Color(0xFF4CAF50)
        UserEngagementStatus.NOT_GOING -> Color(0xFFF44336)
        UserEngagementStatus.UNDECIDED -> Color(0xFFFFC107)
        else -> Color.Gray
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color, CircleShape)
    )
}

/**
 * Full engagement bar for detail screens
 * Shows subscribe and status controls
 */
@Composable
fun EngagementBar(
    engagement: UserEngagement,
    isAuthenticated: Boolean,
    onSubscribeClick: () -> Unit,
    onStatusSelected: (UserEngagementStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subscribe button
        SubscribeButton(
            isSubscribed = engagement.isSubscribed,
            enabled = isAuthenticated,
            onClick = onSubscribeClick
        )

        // Status dropdown
        StatusDropdownButton(
            currentStatus = engagement.status,
            enabled = isAuthenticated,
            onStatusSelected = onStatusSelected
        )
    }
}

/**
 * Login prompt dialog shown when unauthenticated user tries to interact
 */
@Composable
fun LoginPromptDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign In Required") },
        text = {
            Text("Please sign in to subscribe to events and set your attendance status.")
        },
        confirmButton = {
            Button(onClick = onLoginClick) {
                Text("Sign In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

/**
 * Contextual login prompt dialog that adapts its message based on entity type
 */
@Composable
fun ContextualLoginPromptDialog(
    behavior: EngagementBehavior,
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    val message = when (behavior) {
        is EngagementBehavior.SubscribeWithStatus ->
            "Please sign in to subscribe and set your attendance status."
        is EngagementBehavior.SubscribeOnly ->
            "Please sign in to subscribe and get updates."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign In Required") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onLoginClick) {
                Text("Sign In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

/**
 * Unified engagement button that uses Bell notification icon for all entity types.
 * Shows "Subscribe/Subscribed" with notification icon.
 */
@Composable
fun EngagementButton(
    entityType: EntityType,
    isEngaged: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedModifier = modifier.scaleBounceOnChange(key = isEngaged)

    val text = if (isEngaged) "Subscribed" else "Subscribe"

    val buttonColors = if (isEngaged) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    if (isEngaged) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = animatedModifier,
            colors = buttonColors
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = animatedModifier,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Adaptive engagement bar that shows appropriate controls based on entity type.
 * For entities with status (Event, ScheduleItem): Shows subscribe button + status picker
 * For entities without status (Performer, Venue, Location): Shows only subscribe button
 */
@Composable
fun AdaptiveEngagementBar(
    entityType: EntityType,
    engagement: UserEngagement,
    isAuthenticated: Boolean,
    onEngagementClick: () -> Unit,
    onStatusSelected: (UserEngagementStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val behavior = EngagementBehavior.forEntityType(entityType)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subscribe button
        EngagementButton(
            entityType = entityType,
            isEngaged = engagement.isSubscribed,
            enabled = isAuthenticated,
            onClick = onEngagementClick
        )

        // Status dropdown (only for entities that support it)
        if (behavior.supportsStatus) {
            StatusDropdownButton(
                currentStatus = engagement.status,
                enabled = isAuthenticated,
                onStatusSelected = onStatusSelected
            )
        }
    }
}

/**
 * Compact engagement info for cards showing status indicator
 */
@Composable
fun CompactEngagementInfo(
    engagement: UserEngagement,
    modifier: Modifier = Modifier
) {
    if (engagement.status != null || engagement.isSubscribed) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            engagement.status?.let {
                StatusIndicator(status = it)
            }
        }
    }
}

/**
 * Enhanced engagement bar with friend RSVP preview
 * Shows subscribe and status controls with friend avatars to the left of RSVP button
 */
@Composable
fun EngagementBarWithFriends(
    engagement: UserEngagement,
    friendRsvps: List<FriendRsvp>,
    totalFriendCount: Int,
    isAuthenticated: Boolean,
    onSubscribeClick: () -> Unit,
    onStatusSelected: (UserEngagementStatus?) -> Unit,
    onFriendAvatarsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subscribe button
        SubscribeButton(
            isSubscribed = engagement.isSubscribed,
            enabled = isAuthenticated,
            onClick = onSubscribeClick
        )

        // Friend RSVPs + Status dropdown
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend RSVP preview (if any friends have RSVPd)
            if (friendRsvps.isNotEmpty()) {
                FriendRsvpPreviewRow(
                    rsvps = friendRsvps,
                    totalCount = totalFriendCount,
                    onAvatarClick = onFriendAvatarsClick,
                    onMoreClick = onFriendAvatarsClick
                )
            }

            // Status dropdown
            StatusDropdownButton(
                currentStatus = engagement.status,
                enabled = isAuthenticated,
                onStatusSelected = onStatusSelected
            )
        }
    }
}

/**
 * Adaptive engagement bar with friend RSVP preview that works for any entity type.
 * Automatically shows status picker based on entity type.
 */
@Composable
fun AdaptiveEngagementBarWithFriends(
    entityType: EntityType,
    engagement: UserEngagement,
    friendRsvps: List<FriendRsvp>,
    totalFriendCount: Int,
    isAuthenticated: Boolean,
    onEngagementClick: () -> Unit,
    onStatusSelected: (UserEngagementStatus?) -> Unit,
    onFriendAvatarsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val behavior = EngagementBehavior.forEntityType(entityType)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subscribe button
        EngagementButton(
            entityType = entityType,
            isEngaged = engagement.isSubscribed,
            enabled = isAuthenticated,
            onClick = onEngagementClick
        )

        // Friend RSVPs + Status dropdown (only if behavior supports status)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend RSVP preview (if any friends have RSVPd)
            if (friendRsvps.isNotEmpty()) {
                FriendRsvpPreviewRow(
                    rsvps = friendRsvps,
                    totalCount = totalFriendCount,
                    onAvatarClick = onFriendAvatarsClick,
                    onMoreClick = onFriendAvatarsClick
                )
            }

            // Status dropdown (only for entities that support it)
            if (behavior.supportsStatus) {
                StatusDropdownButton(
                    currentStatus = engagement.status,
                    enabled = isAuthenticated,
                    onStatusSelected = onStatusSelected
                )
            }
        }
    }
}
