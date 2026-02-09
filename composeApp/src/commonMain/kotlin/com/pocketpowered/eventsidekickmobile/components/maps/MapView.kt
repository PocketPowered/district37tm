package com.district37.toastmasters.components.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Data class representing a geographic location
 */
data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val title: String? = null
)

/**
 * Platform-specific interactive map view that displays a location with a marker.
 *
 * On Android: Uses Mapbox Maps SDK
 * On iOS: Uses Apple MapKit
 *
 * @param location The location to display and center the map on
 * @param modifier Modifier for the component
 * @param zoomLevel Initial zoom level (higher = more zoomed in)
 * @param onMapClick Optional callback when the map is clicked
 */
@Composable
expect fun MapView(
    location: MapLocation,
    modifier: Modifier = Modifier,
    zoomLevel: Double = 14.0,
    onMapClick: (() -> Unit)? = null
)
