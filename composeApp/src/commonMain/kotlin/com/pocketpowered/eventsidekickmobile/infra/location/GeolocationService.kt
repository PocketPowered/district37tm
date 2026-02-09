package com.district37.toastmasters.infra.location

import kotlinx.coroutines.flow.Flow

/**
 * Device location data
 */
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double
)

/**
 * Permission state for location access
 */
sealed class LocationPermissionState {
    data object NotDetermined : LocationPermissionState()
    data object Granted : LocationPermissionState()
    data object Denied : LocationPermissionState()
    data object DeniedPermanently : LocationPermissionState()
}

/**
 * Service for accessing device geolocation.
 * Implementations are platform-specific (Android/iOS).
 */
interface GeolocationService {
    /**
     * Observable permission state
     */
    val permissionState: Flow<LocationPermissionState>

    /**
     * Request location permission from the user.
     * For silent requests, this may return immediately with current state.
     */
    suspend fun requestPermission(): LocationPermissionState

    /**
     * Get the current device location.
     * Returns null if permission is denied or location unavailable.
     * This is a non-blocking operation with internal timeout.
     */
    suspend fun getCurrentLocation(): DeviceLocation?
}

/**
 * Platform-specific factory function
 */
expect fun createGeolocationService(): GeolocationService
