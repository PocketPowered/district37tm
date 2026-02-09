package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A banner that displays when an entity (event, image, etc.) has been archived.
 * Shows the date when the entity was archived.
 *
 * @param entityType The type of entity that was archived (e.g., "event", "image")
 * @param archivedAt The timestamp when the entity was archived
 * @param modifier Optional modifier for styling
 */
@Composable
fun ArchivedBanner(
    entityType: String,
    archivedAt: Instant,
    modifier: Modifier = Modifier
) {
    val localDate = archivedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val formattedDate = "${localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${localDate.dayOfMonth}, ${localDate.year}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Archive,
            contentDescription = "Archived",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "This $entityType was archived on $formattedDate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
