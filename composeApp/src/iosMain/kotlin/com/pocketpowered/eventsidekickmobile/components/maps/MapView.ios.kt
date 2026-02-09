package com.district37.toastmasters.components.maps

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
/**
 * iOS implementation of MapView using Apple MapKit
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    location: MapLocation,
    modifier: Modifier,
    zoomLevel: Double,
    onMapClick: (() -> Unit)?
) {
    // Convert zoom level to meters for region span
    // Higher zoom = less meters (more zoomed in)
    // Use simple calculation: base meters divided by 2^zoom
    var metersPerZoom = 40000.0
    repeat(zoomLevel.toInt()) { metersPerZoom /= 2.0 }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        UIKitView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            factory = {
                MKMapView().apply {
                    // Set the region centered on the location
                    val coordinate = CLLocationCoordinate2DMake(
                        location.latitude,
                        location.longitude
                    )
                    val region = MKCoordinateRegionMakeWithDistance(
                        coordinate,
                        metersPerZoom,
                        metersPerZoom
                    )
                    setRegion(region, animated = false)

                    // Add a pin annotation at the location
                    val annotation = MKPointAnnotation().apply {
                        setCoordinate(coordinate)
                        location.title?.let { setTitle(it) }
                    }
                    addAnnotation(annotation)

                    // Map settings
                    setScrollEnabled(true)
                    setZoomEnabled(true)
                    setRotateEnabled(false)
                    setPitchEnabled(false)
                }
            },
            update = { mapView ->
                // Update map when location changes
                val coordinate = CLLocationCoordinate2DMake(
                    location.latitude,
                    location.longitude
                )
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    metersPerZoom,
                    metersPerZoom
                )
                mapView.setRegion(region, animated = true)

                // Update annotation
                mapView.removeAnnotations(mapView.annotations)
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    location.title?.let { setTitle(it) }
                }
                mapView.addAnnotation(annotation)
            }
        )
    }
}
