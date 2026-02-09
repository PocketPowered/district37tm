package com.district37.toastmasters.infra.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS implementation of GeolocationService using CLLocationManager.
 */
@OptIn(ExperimentalForeignApi::class)
private class GeolocationServiceImpl : GeolocationService {

    private companion object {
        const val LOCATION_TIMEOUT_MS = 5000L
    }

    private val locationManager = CLLocationManager()
    private val _permissionState = MutableStateFlow(mapAuthorizationStatus(CLLocationManager.authorizationStatus()))

    override val permissionState: Flow<LocationPermissionState> = _permissionState

    private var locationContinuation: ((DeviceLocation?) -> Unit)? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            locationContinuation?.invoke(
                location?.let { loc ->
                    loc.coordinate.useContents {
                        DeviceLocation(latitude, longitude)
                    }
                }
            )
            locationContinuation = null
            manager.stopUpdatingLocation()
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            locationContinuation?.invoke(null)
            locationContinuation = null
            manager.stopUpdatingLocation()
        }

        override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
            _permissionState.value = mapAuthorizationStatus(didChangeAuthorizationStatus)
        }
    }

    init {
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyKilometer
    }

    override suspend fun requestPermission(): LocationPermissionState {
        // For silent request, just check current status
        // The permission dialog will be shown by requestWhenInUseAuthorization if needed
        val currentStatus = CLLocationManager.authorizationStatus()
        val currentState = mapAuthorizationStatus(currentStatus)
        _permissionState.value = currentState
        return currentState
    }

    override suspend fun getCurrentLocation(): DeviceLocation? {
        val currentStatus = CLLocationManager.authorizationStatus()
        if (currentStatus != kCLAuthorizationStatusAuthorizedWhenInUse &&
            currentStatus != kCLAuthorizationStatusAuthorizedAlways
        ) {
            return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                locationContinuation = { location ->
                    if (cont.isActive) {
                        cont.resume(location)
                    }
                }
                locationManager.requestLocation()
            }
        }
    }

    private fun mapAuthorizationStatus(status: CLAuthorizationStatus): LocationPermissionState {
        return when (status) {
            kCLAuthorizationStatusNotDetermined -> LocationPermissionState.NotDetermined
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionState.Granted
            kCLAuthorizationStatusDenied -> LocationPermissionState.DeniedPermanently
            kCLAuthorizationStatusRestricted -> LocationPermissionState.Denied
            else -> LocationPermissionState.Denied
        }
    }
}

actual fun createGeolocationService(): GeolocationService = GeolocationServiceImpl()
