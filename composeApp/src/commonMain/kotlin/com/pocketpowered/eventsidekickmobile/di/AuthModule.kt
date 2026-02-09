package com.district37.toastmasters.di

import org.koin.core.module.Module

/**
 * Platform-specific auth module that provides TokenManager and OAuthBrowser.
 * These require platform-specific implementations (Context on Android, etc.)
 */
expect val platformAuthModule: Module
