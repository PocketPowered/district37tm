package com.district37.toastmasters.infra

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of PushNotificationService.
 *
 * This implementation bridges to Swift code that handles Firebase Cloud Messaging.
 * The Swift side must call the setter functions to provide the native implementations.
 *
 * In the iOS app's AppDelegate/SceneDelegate:
 * 1. Initialize Firebase
 * 2. Request notification permissions
 * 3. Get FCM token and store it
 * 4. Call PushNotificationBridge.setFcmToken() when token is available
 */
actual class PushNotificationService {

    companion object {
        // Callback holders for Swift bridge
        private var fcmTokenProvider: (() -> String?)? = null
        private var deleteTokenCallback: (() -> Unit)? = null
        private var notificationsEnabledChecker: (() -> Boolean)? = null
        private var requestPermissionCallback: (() -> Boolean)? = null

        /**
         * Called from Swift to set the FCM token provider
         */
        fun setFcmTokenProvider(provider: () -> String?) {
            fcmTokenProvider = provider
        }

        /**
         * Called from Swift to set the delete token callback
         */
        fun setDeleteTokenCallback(callback: () -> Unit) {
            deleteTokenCallback = callback
        }

        /**
         * Called from Swift to set the notifications enabled checker
         */
        fun setNotificationsEnabledChecker(checker: () -> Boolean) {
            notificationsEnabledChecker = checker
        }

        /**
         * Called from Swift to set the request permission callback
         */
        fun setRequestPermissionCallback(callback: () -> Boolean) {
            requestPermissionCallback = callback
        }
    }

    /**
     * Get the current FCM token from Swift bridge
     */
    actual suspend fun getFcmToken(): String? {
        return fcmTokenProvider?.invoke()
    }

    /**
     * Delete the current FCM token via Swift bridge
     */
    actual suspend fun deleteFcmToken() {
        deleteTokenCallback?.invoke()
    }

    /**
     * Check if push notifications are enabled via Swift bridge
     */
    actual suspend fun areNotificationsEnabled(): Boolean {
        return notificationsEnabledChecker?.invoke() ?: false
    }

    /**
     * Request notification permission via Swift bridge
     * Note: On iOS, this returns immediately with current state.
     * The actual permission request is handled by Swift.
     */
    actual suspend fun requestNotificationPermission(): Boolean {
        return requestPermissionCallback?.invoke() ?: false
    }
}
