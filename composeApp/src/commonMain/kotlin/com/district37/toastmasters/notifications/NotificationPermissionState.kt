package com.district37.toastmasters.notifications

sealed class NotificationPermissionState {
    object Granted : NotificationPermissionState()
    object Denied : NotificationPermissionState()
    object NotDetermined : NotificationPermissionState()
} 