package com.district37.toastmasters.infra

import com.district37.toastmasters.navigation.DeeplinkHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Bridge object to expose DeeplinkHandler functionality to platform-specific code.
 * This allows Swift code on iOS to handle deeplinks by calling into the common code.
 */
object DeeplinkHandlerBridge : KoinComponent {
    private val deeplinkHandler: DeeplinkHandler by inject()

    /**
     * Handle a deeplink from Swift/iOS
     * @param url The deeplink URL string
     */
    fun handleDeeplinkFromSwift(url: String) {
        deeplinkHandler.handleDeeplink(url)
    }
}
