package com.district37.toastmasters.splash

import android.content.Context

private const val PREFERENCES_NAME = "splash_override"
private const val SPLASH_IMAGE_URL_KEY = "splash_image_url"

actual class SplashOverrideStore actual constructor(context: Any?) {
    private val sharedPreferences = requireNotNull(context as? Context)
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    actual fun getSplashImageUrl(): String? {
        return sharedPreferences.getString(SPLASH_IMAGE_URL_KEY, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    actual fun setSplashImageUrl(url: String?) {
        sharedPreferences.edit().apply {
            if (url.isNullOrBlank()) {
                remove(SPLASH_IMAGE_URL_KEY)
            } else {
                putString(SPLASH_IMAGE_URL_KEY, url)
            }
        }.apply()
    }
}
