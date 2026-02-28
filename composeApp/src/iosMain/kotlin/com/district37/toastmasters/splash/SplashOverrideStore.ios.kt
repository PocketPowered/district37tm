package com.district37.toastmasters.splash

import platform.Foundation.NSUserDefaults

private const val SPLASH_IMAGE_URL_KEY = "splash_image_url"

actual class SplashOverrideStore actual constructor(context: Any?) {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun getSplashImageUrl(): String? {
        return userDefaults.stringForKey(SPLASH_IMAGE_URL_KEY)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    actual fun setSplashImageUrl(url: String?) {
        if (url.isNullOrBlank()) {
            userDefaults.removeObjectForKey(SPLASH_IMAGE_URL_KEY)
            return
        }
        userDefaults.setObject(url, SPLASH_IMAGE_URL_KEY)
    }
}
