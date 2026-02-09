package com.district37.toastmasters.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

private var application: Application? = null

/**
 * Set the Application instance for Koin initialization.
 * Must be called before initializeKoin().
 */
fun setApplication(app: Application) {
    application = app
}

/**
 * Android implementation of Koin initialization.
 * Includes androidContext for platform-specific dependencies like TokenManager.
 */
actual fun initializeKoin() {
    val app = application ?: throw IllegalStateException(
        "Application not set. Call setApplication() from Application.onCreate() before initializeKoin()"
    )

    // Only start Koin if not already started (prevents double init)
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            androidLogger()
            androidContext(app)
            modules(platformAuthModule, appModule)
        }
    }
}
