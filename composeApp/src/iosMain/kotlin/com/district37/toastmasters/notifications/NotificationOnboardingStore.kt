package com.district37.toastmasters.notifications

import platform.Foundation.NSUserDefaults

private const val ONBOARDING_COMPLETE_KEY = "notification_onboarding_complete"

actual class NotificationOnboardingStore actual constructor(context: Any?) {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun hasCompletedOnboarding(): Boolean {
        return userDefaults.boolForKey(ONBOARDING_COMPLETE_KEY)
    }

    actual fun setCompletedOnboarding(completed: Boolean) {
        userDefaults.setBool(completed, ONBOARDING_COMPLETE_KEY)
    }
}
