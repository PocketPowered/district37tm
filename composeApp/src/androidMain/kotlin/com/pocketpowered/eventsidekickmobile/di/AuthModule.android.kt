package com.district37.toastmasters.di

import com.district37.toastmasters.auth.data.TokenManager
import com.district37.toastmasters.infra.ClipboardManager
import com.district37.toastmasters.infra.DeveloperSettingsManager
import com.district37.toastmasters.infra.createDeveloperSettingsManager
import com.district37.toastmasters.infra.PinnedEventManager
import com.district37.toastmasters.infra.createPinnedEventManager
import com.district37.toastmasters.infra.OAuthBrowser
import com.district37.toastmasters.infra.PushNotificationService
import com.district37.toastmasters.infra.DirectionsLauncher
import com.district37.toastmasters.infra.ShareManager
import com.district37.toastmasters.infra.calendar.CalendarEventMatcher
import com.district37.toastmasters.infra.calendar.CalendarLauncher
import com.district37.toastmasters.infra.calendar.CalendarService
import com.district37.toastmasters.models.Platform
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific auth module providing TokenManager, OAuthBrowser, ClipboardManager, ShareManager,
 * DeveloperSettingsManager, PushNotificationService, and Platform.
 * Uses Android Context for EncryptedSharedPreferences, Custom Tabs, clipboard access, sharing,
 * developer settings storage, and push notifications.
 */
actual val platformAuthModule: Module = module {
    single { TokenManager(androidContext()) }
    single { OAuthBrowser(androidContext()) }
    single { ClipboardManager(androidContext()) }
    single { ShareManager(androidContext()) }
    single { DirectionsLauncher(androidContext()) }
    single { CalendarService(androidContext()) }
    single { CalendarLauncher(androidContext()) }
    single { CalendarEventMatcher(androidContext()) }
    single<DeveloperSettingsManager> { createDeveloperSettingsManager() }
    single<PinnedEventManager> { createPinnedEventManager() }
    single { PushNotificationService(androidContext()) }
    single { Platform.ANDROID }
}
