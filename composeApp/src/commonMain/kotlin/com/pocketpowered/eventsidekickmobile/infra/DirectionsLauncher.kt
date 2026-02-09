package com.district37.toastmasters.infra

/**
 * Platform-specific directions/navigation functionality.
 * Opens the native maps app with directions to a location.
 */
expect class DirectionsLauncher {
    /**
     * Open the native maps app with directions to the specified coordinates
     * @param latitude Destination latitude
     * @param longitude Destination longitude
     * @param label Optional label for the destination (e.g., venue name)
     */
    fun openDirections(latitude: Double, longitude: Double, label: String? = null)
}
