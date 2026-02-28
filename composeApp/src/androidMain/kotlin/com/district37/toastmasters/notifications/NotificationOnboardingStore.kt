package com.district37.toastmasters.notifications

import android.content.Context

private const val PREFERENCES_NAME = "notification_onboarding"
private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"

actual class NotificationOnboardingStore actual constructor(context: Any?) {
    private val sharedPreferences = requireNotNull(context as? Context)
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    actual fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean(ONBOARDING_COMPLETE_KEY, false)
    }

    actual fun setCompletedOnboarding(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ONBOARDING_COMPLETE_KEY, completed)
            .apply()
    }
}
