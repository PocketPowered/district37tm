package com.district37.toastmasters.infra

import com.district37.toastmasters.util.Logger
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation of DirectionsLauncher using Apple Maps URL scheme
 */
actual class DirectionsLauncher {
    private val TAG = "DirectionsLauncher"

    /**
     * Open Apple Maps with directions to the specified coordinates
     */
    actual fun openDirections(latitude: Double, longitude: Double, label: String?) {
        try {
            // Apple Maps URL with directions mode
            // daddr = destination address
            val encodedLabel = label?.replace(" ", "+") ?: ""
            val urlString = "http://maps.apple.com/?daddr=$latitude,$longitude&q=$encodedLabel"

            val url = NSURL.URLWithString(urlString)
            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
                Logger.d(TAG, "Opened Apple Maps for directions to: $latitude,$longitude")
            } else {
                Logger.e(TAG, "Cannot open Apple Maps URL")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open directions: ${e.message}")
        }
    }
}
