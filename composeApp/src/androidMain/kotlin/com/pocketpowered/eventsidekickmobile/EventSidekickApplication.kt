package com.district37.toastmasters

import android.app.Application as AndroidApplication
import com.district37.toastmasters.di.initializeKoin
import com.district37.toastmasters.di.setApplication

/**
 * Application class for EventSidekick
 * Initializes Koin dependency injection once at app startup
 */
class Application : AndroidApplication() {
    override fun onCreate() {
        super.onCreate()
        setApplication(this)
        initializeKoin()
    }
}
