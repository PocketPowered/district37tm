package com.district37.toastmasters.infra.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume

/**
 * Android implementation of GeolocationService using FusedLocationProviderClient.
 * Uses coarse location (city-level) for privacy.
 */
private class GeolocationServiceImpl(private val context: Context) : GeolocationService {

    private companion object {
        const val LOCATION_TIMEOUT_MS = 5000L
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _permissionState = MutableStateFlow(checkCurrentPermissionState())
    override val permissionState: Flow<LocationPermissionState> = _permissionState

    private fun checkCurrentPermissionState(): LocationPermissionState {
        return when {
            hasLocationPermission() -> LocationPermissionState.Granted
            else -> LocationPermissionState.NotDetermined
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): LocationPermissionState {
        // For silent request, just return current state
        // The actual permission prompt is handled by the UI layer if needed
        val currentState = checkCurrentPermissionState()
        _permissionState.value = currentState
        return currentState
    }

    override suspend fun getCurrentLocation(): DeviceLocation? {
        if (!hasLocationPermission()) {
            return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            try {
                getLocationFromFused()
            } catch (e: SecurityException) {
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun getLocationFromFused(): DeviceLocation? = suspendCancellableCoroutine { cont ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (cont.isActive) {
                        cont.resume(location?.let { DeviceLocation(it.latitude, it.longitude) })
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
        } catch (e: SecurityException) {
            if (cont.isActive) {
                cont.resume(null)
            }
        }
    }

    /**
     * Call this when permission state changes externally (e.g., from settings).
     * This updates the internal state flow.
     */
    fun refreshPermissionState() {
        _permissionState.value = checkCurrentPermissionState()
    }
}

actual fun createGeolocationService(): GeolocationService {
    return object : KoinComponent {
        val context: Context by inject()
    }.let { GeolocationServiceImpl(it.context) }
}
