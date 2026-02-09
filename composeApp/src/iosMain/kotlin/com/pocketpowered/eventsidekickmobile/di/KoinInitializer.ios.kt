package com.district37.toastmasters.di

import org.koin.core.context.startKoin
import org.koin.core.KoinApplication

/**
 * iOS implementation of Koin initialization.
 * No special context needed for iOS.
 */
actual fun initializeKoin() {
    startKoin {
        modules(platformAuthModule, appModule)
    }
}
