package com.district37.toastmasters.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.UIKit.UIApplication.Companion.sharedApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationPermissions {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val _permissionState =
        MutableStateFlow<NotificationPermissionState>(NotificationPermissionState.NotDetermined)
    actual val permissionState: StateFlow<NotificationPermissionState> = _permissionState

    init {
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            _permissionState.value = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> NotificationPermissionState.Granted
                UNAuthorizationStatusDenied -> NotificationPermissionState.Denied
                UNAuthorizationStatusNotDetermined -> NotificationPermissionState.NotDetermined
                else -> NotificationPermissionState.NotDetermined
            }
        }
    }

    actual suspend fun requestPermission() {
        notificationCenter.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
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
