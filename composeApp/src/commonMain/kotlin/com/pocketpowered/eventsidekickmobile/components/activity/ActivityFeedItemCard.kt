package com.district37.toastmasters.components.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.graphql.type.ActivityType
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.navigation.DeeplinkDestination
import com.district37.toastmasters.navigation.DeeplinkHandler
import com.district37.toastmasters.util.DateTimeFormatter
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.koin.compose.koinInject

/**
 * Card displaying a single activity feed item
 * Uses DeeplinkHandler for cross-tab navigation to entity detail screens
 */
@Composable
fun ActivityFeedItemCard(
    item: ActivityFeedItem,
    modifier: Modifier = Modifier,
    deeplinkHandler: DeeplinkHandler = koinInject()
) {
    val onClick: () -> Unit = {
        when (item.entityType) {
            EntityType.EVENT -> item.event?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.Event(it.toString()))
            }
            EntityType.VENUE -> item.venue?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.Venue(it.toString()))
            }
            EntityType.PERFORMER -> item.performer?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.Performer(it.toString()))
            }
            EntityType.AGENDAITEM -> item.agendaItem?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.AgendaItem(it))
            }
            EntityType.LOCATION -> item.location?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.Location(it.toString()))
            }
            EntityType.ORGANIZATION -> item.organization?.id?.let {
                deeplinkHandler.setDestination(DeeplinkDestination.Organization(it.toString()))
            }
            else -> {}
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // User avatar with activity type badge
            Box {
                // User profile image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    val profileImageUrl = item.user?.profileImageUrl
                    if (profileImageUrl != null) {
                        CoilImage(
                            imageModel = { profileImageUrl },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                contentDescription = item.user?.displayName
                            )
                        )
                    } else {
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
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Activity type indicator badge
                val (badgeIcon, badgeColor) = getActivityBadge(item.activityType)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Activity text - show "You" (bold) for current user's activity
                val actorName = if (item.isCurrentUser) "You" else (item.user?.displayName ?: "Someone")
                val activityText = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(actorName)
                    }
                    append(" ")
                    append(getActivityVerb(item.activityType, item.isCurrentUser))
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(item.entityName)
                    }
                }
                Text(
                    text = activityText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Timestamp
                Text(
                    text = DateTimeFormatter.formatRelativeTime(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Entity thumbnail (if available)
            val entityImageUrl = item.entityImageUrl
            if (entityImageUrl != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    CoilImage(
                        imageModel = { entityImageUrl },
                        modifier = Modifier.fillMaxSize(),
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            contentDescription = item.entityName
                        )
                    )
                }
            }
        }
    }
}

/**
 * Get the icon and color for activity type badge
 */
private fun getActivityBadge(activityType: ActivityType): Pair<ImageVector, Color> {
    return when (activityType) {
        ActivityType.RSVP_GOING -> Icons.Default.Check to Color(0xFF4CAF50)
        ActivityType.RSVP_NOT_GOING -> Icons.Default.Close to Color(0xFFF44336)
        ActivityType.RSVP_UNDECIDED -> Icons.Default.DateRange to Color(0xFFFF9800)
        ActivityType.SUBSCRIBED -> Icons.Default.Favorite to Color(0xFF4CAF50)
        ActivityType.UNSUBSCRIBED -> Icons.Default.Favorite to Color(0xFF9E9E9E)
        ActivityType.CREATED -> Icons.Default.Add to Color(0xFF2196F3)
        else -> Icons.Default.DateRange to Color(0xFF9E9E9E)
    }
}

/**
 * Get the verb text for activity type
 * @param isCurrentUser If true, uses "are" form for current user (e.g., "are going to" instead of "is going to")
 */
private fun getActivityVerb(activityType: ActivityType, isCurrentUser: Boolean = false): String {
    return when (activityType) {
        ActivityType.RSVP_GOING -> if (isCurrentUser) "are going to" else "is going to"
        ActivityType.RSVP_NOT_GOING -> if (isCurrentUser) "are not going to" else "is not going to"
        ActivityType.RSVP_UNDECIDED -> if (isCurrentUser) "are undecided about" else "is undecided about"
        ActivityType.SUBSCRIBED -> "subscribed to"
        ActivityType.UNSUBSCRIBED -> "unsubscribed from"
        ActivityType.CREATED -> "created"
        else -> "interacted with"
    }
}
