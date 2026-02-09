package com.district37.toastmasters.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.infra.DirectionsLauncher
import org.koin.compose.koinInject

/**
 * "Get Directions" button that opens the native maps app
 * with directions to the specified coordinates.
 *
 * @param latitude Destination latitude
 * @param longitude Destination longitude
 * @param label Optional label for the destination (e.g., venue name)
 * @param modifier Modifier for the button
 */
@Composable
fun GetDirectionsButton(
    latitude: Double,
    longitude: Double,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val directionsLauncher: DirectionsLauncher = koinInject()

    OutlinedButton(
        onClick = { directionsLauncher.openDirections(latitude, longitude, label) },
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text("Get Directions")
        }
    }
}
