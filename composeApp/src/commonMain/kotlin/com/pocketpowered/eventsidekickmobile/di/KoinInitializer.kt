package com.district37.toastmasters.di

/**
 * Initialize Koin dependency injection.
 * Platform-specific implementations handle Context (Android) or no-op (iOS).
 */
expect fun initializeKoin()
