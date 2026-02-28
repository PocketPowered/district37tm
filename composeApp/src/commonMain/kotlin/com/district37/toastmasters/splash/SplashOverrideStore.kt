package com.district37.toastmasters.splash

expect class SplashOverrideStore(context: Any? = null) {
    fun getSplashImageUrl(): String?
    fun setSplashImageUrl(url: String?)
}
