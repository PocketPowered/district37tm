package com.district37.toastmasters.components.search

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.OmnisearchResultItem
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.models.User
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.DateTimeFormatter
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Unified search result card that displays different content based on result type
 */
@Composable
fun SearchResultCard(
    result: OmnisearchResultItem,
    onEventClick: (Int) -> Unit,
    onVenueClick: (Int) -> Unit,
    onPerformerClick: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    onOrganizationClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (result) {
        is OmnisearchResultItem.EventResult -> EventSearchResultCard(
            event = result.event,
            onClick = { onEventClick(result.event.id) },
            modifier = modifier
        )
        is OmnisearchResultItem.VenueResult -> VenueSearchResultCard(
            venue = result.venue,
            onClick = { onVenueClick(result.venue.id) },
            modifier = modifier
        )
        is OmnisearchResultItem.PerformerResult -> PerformerSearchResultCard(
            performer = result.performer,
            onClick = { onPerformerClick(result.performer.id) },
            modifier = modifier
        )
        is OmnisearchResultItem.UserResult -> UserSearchResultCard(
            user = result.user,
            onClick = { onUserClick(result.user.id) },
            modifier = modifier
        )
        is OmnisearchResultItem.OrganizationResult -> OrganizationSearchResultCard(
            organization = result.organization,
            onClick = { onOrganizationClick(result.organization.id) },
            modifier = modifier
        )
    }
}

@Composable
private fun EventSearchResultCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryImage = event.images.firstOrNull()

    SearchResultCardBase(
        onClick = onClick,
        imageUrl = primaryImage?.url,
        placeholderIcon = Icons.Default.DateRange,
        badge = SearchResultBadge.Event,
        title = event.name,
        subtitle = event.startDate?.let { DateTimeFormatter.formatDateWithTime(it) },
        description = event.description,
        modifier = modifier
    )
}

@Composable
private fun VenueSearchResultCard(
    venue: Venue,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryImage = venue.images.firstOrNull()
    val locationText = listOfNotNull(venue.city, venue.state).joinToString(", ")

    SearchResultCardBase(
        onClick = onClick,
        imageUrl = primaryImage?.url,
        placeholderIcon = Icons.Default.LocationOn,
        badge = SearchResultBadge.Venue,
        title = venue.name,
        subtitle = locationText.ifBlank { null },
        description = venue.address,
        modifier = modifier
    )
}

@Composable
private fun PerformerSearchResultCard(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryImage = performer.images.firstOrNull()

    SearchResultCardBase(
        onClick = onClick,
        imageUrl = primaryImage?.url,
        placeholderIcon = Icons.Default.Person,
        badge = SearchResultBadge.Performer,
        title = performer.name,
        subtitle = performer.performerType,
        description = performer.bio,
        modifier = modifier
    )
}

@Composable
private fun UserSearchResultCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchResultCardBase(
        onClick = onClick,
        imageUrl = user.profileImageUrl,
        placeholderIcon = Icons.Default.Person,
        badge = SearchResultBadge.User,
        title = user.displayName ?: user.username ?: "Unknown User",
        subtitle = user.username?.let { "@$it" },
        description = user.bio,
        modifier = modifier
    )
}

@Composable
private fun OrganizationSearchResultCard(
    organization: Organization,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = organization.logoUrl ?: organization.images.firstOrNull()?.url

    SearchResultCardBase(
        onClick = onClick,
        imageUrl = imageUrl,
        placeholderIcon = Icons.Default.Business,
        badge = SearchResultBadge.Organization,
        title = organization.name,
        subtitle = organization.tag?.let { "@$it" },
        description = organization.description,
        modifier = modifier
    )
}

private enum class SearchResultBadge(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    Event("Event", Color(0xFF2196F3), Color.White),
    Venue("Venue", Color(0xFF4CAF50), Color.White),
    Performer("Performer", Color(0xFF9C27B0), Color.White),
    User("User", Color(0xFF607D8B), Color.White),
    Organization("Organization", Color(0xFFFF9800), Color.White)
}

@Composable
private fun SearchResultCardBase(
    onClick: (() -> Unit)?,
    imageUrl: String?,
    placeholderIcon: ImageVector,
    badge: SearchResultBadge,
    title: String,
    subtitle: String?,
    description: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (imageUrl != null) {
                    CoilImage(
                        imageModel = { imageUrl },
                        modifier = Modifier.fillMaxSize(),
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            contentDescription = title
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
                        androidx.compose.material3.Icon(
                            imageVector = placeholderIcon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge
                Surface(
                    color = badge.backgroundColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = badge.textColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Subtitle
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Description
                description?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
