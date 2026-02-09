package com.district37.toastmasters.features.locations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.DetailHeroCard
import com.district37.toastmasters.components.common.DetailScaffold
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.ShareUrlGenerator
import com.district37.toastmasters.models.Location
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Location detail screen displaying comprehensive information about a location
 * including images, description, capacity, type, and all other available fields
 *
 * @param locationId The ID of the location to display
 * @param onBackClick Callback when back button is clicked
 */
@Composable
fun LocationDetailScreen(
    locationId: Int,
    onBackClick: () -> Unit = {}
) {
    val viewModel: LocationDetailViewModel = koinViewModel(key = locationId.toString()) { parametersOf(locationId) }
    val locationState by viewModel.item.collectAsState()

    DetailScaffold(
        resourceState = locationState,
        onBackClick = onBackClick,
        onRetry = { viewModel.refresh() },
        errorMessage = "Failed to load location",
        actions = {
            // Share button
            val shareManager: ShareManager = koinInject()
            IconButton(onClick = {
                shareManager.share(
                    url = ShareUrlGenerator.generateLocationUrl(locationId, locationState.getOrNull()?.slug),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Location"
                )
            }
        },
        primaryContent = { location ->
            LocationInfoCard(location = location)
        },
        additionalContent = { _ ->
            // No additional content - schedule tabs have been removed
        }
    )
}

/**
 * Location information display using reusable DetailHeroCard
 */
@Composable
fun LocationInfoCard(location: Location) {
    // Build type badge with floor level if available
    val typeBadge = buildString {
        location.locationType?.let { append(it) }
        location.floorLevel?.let { floor ->
            if (isNotEmpty()) append(" • ")
            append("Floor $floor")
        }
    }.takeIf { it.isNotBlank() }

    DetailHeroCard(
        images = location.images,
        title = location.name,
        typeBadge = typeBadge,
        description = location.description,
        additionalContent = {
            // Capacity
            location.capacity?.let { capacity ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 Capacity:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = capacity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Created At
            location.createdAt?.let { createdAt ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Added: ${createdAt.toString().substringBefore('T')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}
