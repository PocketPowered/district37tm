package com.district37.toastmasters.components.engagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.graphql.type.UserEngagementStatus
import com.district37.toastmasters.models.FriendRsvp
import com.district37.toastmasters.navigation.UserProfileNavigationArgs
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Friend avatar with RSVP status badge overlay
 */
@Composable
fun FriendRsvpAvatar(
    rsvp: FriendRsvp,
    size: Int = 40,
    showBadge: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Avatar (clipped to circle)
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            val profileImageUrl = rsvp.profileImageUrl
            if (profileImageUrl != null) {
                CoilImage(
                    imageModel = { profileImageUrl },
                    modifier = Modifier.fillMaxSize(),
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        contentDescription = rsvp.displayName
                    )
                )
            } else {
                // Fallback gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size((size * 0.5).dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // RSVP status badge overlay (matching activity feed style - overflows the avatar circle)
        if (showBadge) {
            val (badgeIcon, badgeColor) = getRsvpBadge(rsvp.status)
            val badgeSize = (size * 0.4).dp
            val iconSize = (size * 0.25).dp

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Horizontal row showing first 3 friend RSVPs with "+X more" indicator
 * Shows clean avatars without badges (status is shown in the adjacent RSVP button)
 */
@Composable
fun FriendRsvpPreviewRow(
    rsvps: List<FriendRsvp>,
    totalCount: Int,
    onAvatarClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (rsvps.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-8).dp), // Overlap avatars
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show first 3 friends (clean avatars - status shown in RSVP button)
        rsvps.take(3).forEach { rsvp ->
            FriendRsvpAvatar(
                rsvp = rsvp,
                size = 32,
                onClick = onAvatarClick
            )
        }

        // Show "+X" indicator if more than 3
        if (totalCount > 3) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${totalCount - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Modal bottom sheet showing all friends who RSVPd
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRsvpBottomSheet(
    rsvps: List<FriendRsvp>,
    totalCount: Int,
    hasNextPage: Boolean,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Friend's RSVP ($totalCount)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // List of friends
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(rsvps) { rsvp ->
                    FriendRsvpListItem(
                        rsvp = rsvp,
                        onClick = { userId ->
                            navController.navigate(UserProfileNavigationArgs(userId))
                            onDismiss()
                        }
                    )
                }

                // Load more indicator
                if (hasNextPage) {
                    item {
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            TextButton(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Load More")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * List item showing friend RSVP details
 */
@Composable
fun FriendRsvpListItem(
    rsvp: FriendRsvp,
    onClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(rsvp.userId) },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with badge
        FriendRsvpAvatar(
            rsvp = rsvp,
            size = 48
        )

        // Friend info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rsvp.displayName ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicator(status = rsvp.status, size = 8)
                Text(
                    text = getRsvpStatusText(rsvp.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper functions
private fun getRsvpStatusText(status: UserEngagementStatus): String {
    return when (status) {
        UserEngagementStatus.GOING -> "Going"
        UserEngagementStatus.NOT_GOING -> "Not Going"
        UserEngagementStatus.UNDECIDED -> "Undecided"
        else -> "Unknown"
    }
}

/**
 * Get badge icon and color for RSVP status
 * Matches the activity feed badge styling
 */
private fun getRsvpBadge(status: UserEngagementStatus): Pair<ImageVector, Color> {
    return when (status) {
        UserEngagementStatus.GOING -> Icons.Default.Check to Color(0xFF4CAF50)           // Green checkmark
        UserEngagementStatus.NOT_GOING -> Icons.Default.Close to Color(0xFFF44336)      // Red X
        UserEngagementStatus.UNDECIDED -> {
            Icons.Default.DateRange to Color(0xFFFF9800)  // Orange calendar (undecided)
        }

        else -> Icons.Default.DateRange to Color(0xFF9E9E9E)  // Gray calendar (default)
    }
}
