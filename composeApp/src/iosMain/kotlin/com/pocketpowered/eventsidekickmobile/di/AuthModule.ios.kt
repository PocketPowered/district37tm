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
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific auth module providing TokenManager, OAuthBrowser, ClipboardManager, ShareManager,
 * DeveloperSettingsManager, PushNotificationService, and Platform.
 * Uses Keychain for secure storage, ASWebAuthenticationSession for OAuth, UIPasteboard for clipboard,
 * UIActivityViewController for sharing, UserDefaults for developer settings, and Firebase for push.
 */
actual val platformAuthModule: Module = module {
    single { TokenManager() }
    single { OAuthBrowser() }
    single { ClipboardManager() }
    single { ShareManager() }
    single { DirectionsLauncher() }
    single { CalendarService() }
    single { CalendarLauncher(get()) }
    single { CalendarEventMatcher() }
    single<DeveloperSettingsManager> { createDeveloperSettingsManager() }
    single<PinnedEventManager> { createPinnedEventManager() }
    single { PushNotificationService() }
    single { Platform.IOS }
}
