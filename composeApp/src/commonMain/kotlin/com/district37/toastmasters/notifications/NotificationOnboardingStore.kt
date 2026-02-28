package com.district37.toastmasters.notifications

expect class NotificationOnboardingStore(context: Any? = null) {
    fun hasCompletedOnboarding(): Boolean

    fun setCompletedOnboarding(completed: Boolean)
}
