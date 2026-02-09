package com.district37.toastmasters.infra

import kotlinx.coroutines.flow.Flow

/**
 * Manager for user preferences that should be persisted locally.
 * These are user-facing settings that persist across app launches.
 */
interface UserPreferencesManager {
    /**
     * Flow of the "My Schedule" filter preference.
     * When true, the agenda view shows only items the user has RSVP'd to.
     */
    val myScheduleOnlyFlow: Flow<Boolean>

    /**
     * Sets the "My Schedule" filter preference.
     */
    suspend fun setMyScheduleOnly(value: Boolean)

    /**
     * Gets the current "My Schedule" filter preference.
     */
    suspend fun getMyScheduleOnly(): Boolean

    /**
     * Flow of the "Auto Sync Calendar" preference.
     * When true, RSVP'd agenda items are automatically synced to device calendar.
     */
    val autoSyncCalendarFlow: Flow<Boolean>

    /**
     * Sets the "Auto Sync Calendar" preference.
     */
    suspend fun setAutoSyncCalendar(value: Boolean)

    /**
     * Gets the current "Auto Sync Calendar" preference.
     */
    suspend fun getAutoSyncCalendar(): Boolean

    /**
     * Check if this is the first launch after a fresh install/reinstall.
     * Returns true if no install ID is found (indicating new install).
     */
    suspend fun isFirstLaunchAfterInstall(): Boolean

    /**
     * Mark that the first launch setup has been completed.
     * Generates and stores a unique install ID to track this installation.
     */
    suspend fun markFirstLaunchCompleted()
}

/**
 * Platform-specific factory for creating UserPreferencesManager.
 * Android uses SharedPreferences, iOS uses UserDefaults.
 */
expect fun createUserPreferencesManager(): UserPreferencesManager
