package com.district37.toastmasters.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.UIKit.UIApplication.Companion.sharedApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationPermissions {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val _permissionState = MutableStateFlow(getCurrentPermissionState())
    actual val permissionState: StateFlow<NotificationPermissionState> = _permissionState

    private fun getCurrentPermissionState(): NotificationPermissionState {
        var currentState: NotificationPermissionState = NotificationPermissionState.NotDetermined
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            currentState = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized -> NotificationPermissionState.Granted
                UNAuthorizationStatusDenied -> NotificationPermissionState.Denied
                UNAuthorizationStatusNotDetermined -> NotificationPermissionState.NotDetermined
                else -> NotificationPermissionState.NotDetermined
            }
        }
        return currentState
    }

    actual suspend fun requestPermission() {
        notificationCenter.requestAuthorizationWithOptions(
            options = 0u, // This is a bitmask of UNAuthorizationOptions
            completionHandler = { granted, error ->
                if (error != null) {
                    NSLog("Error requesting notification permission: ${error.localizedDescription}")
                }
                _permissionState.value = if (granted) NotificationPermissionState.Granted else NotificationPermissionState.Denied
            }
        )
    }

    actual fun openNotificationSettings() {
        val settingsUrl = UIApplicationOpenSettingsURLString
        val app = sharedApplication
        val url = NSURL.URLWithString(settingsUrl)
        if (url != null && app.canOpenURL(url)) {
            app.openURL(url)
        } else {
            NSLog("Failed to open notification settings")
        }
    }
} 