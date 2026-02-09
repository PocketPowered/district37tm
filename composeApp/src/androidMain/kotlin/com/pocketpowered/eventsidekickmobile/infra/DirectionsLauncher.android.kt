package com.district37.toastmasters.infra

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.district37.toastmasters.util.Logger

/**
 * Android implementation of DirectionsLauncher using geo: URI scheme
 */
actual class DirectionsLauncher(private val context: Context) {
    private val TAG = "DirectionsLauncher"

    /**
     * Open the native maps app with directions to the specified coordinates
     * Uses geo: URI scheme with fallback to Google Maps web
     */
    actual fun openDirections(latitude: Double, longitude: Double, label: String?) {
        try {
            val encodedLabel = label?.let { Uri.encode(it) } ?: "Location"
            val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
                Logger.d(TAG, "Opened maps for directions to: $latitude,$longitude")
            } catch (e: Exception) {
                // Fallback to Google Maps web
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
                Logger.d(TAG, "Fallback to web maps for: $latitude,$longitude")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open directions: ${e.message}")
        }
    }
}
