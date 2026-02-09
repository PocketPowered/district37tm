package com.district37.toastmasters.infra

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

/**
 * iOS implementation of UserPreferencesManager using UserDefaults.
 * Stores user preferences that persist across app launches.
 */
private class UserPreferencesManagerImpl : UserPreferencesManager {

    private companion object {
        const val KEY_MY_SCHEDULE_ONLY = "user_my_schedule_only"
        const val KEY_AUTO_SYNC_CALENDAR = "user_auto_sync_calendar"
        const val KEY_INSTALL_ID = "user_install_id"
    }

    // StateFlow to track the current value and emit updates
    private val _myScheduleOnlyFlow = MutableStateFlow(
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_MY_SCHEDULE_ONLY)
    )

    override val myScheduleOnlyFlow: Flow<Boolean> = _myScheduleOnlyFlow.asStateFlow()

    override suspend fun setMyScheduleOnly(value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_MY_SCHEDULE_ONLY)
        _myScheduleOnlyFlow.value = value
    }

    override suspend fun getMyScheduleOnly(): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(KEY_MY_SCHEDULE_ONLY)
    }

    // StateFlow for auto sync calendar preference
    private val _autoSyncCalendarFlow = MutableStateFlow(
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_AUTO_SYNC_CALENDAR)
    )

    override val autoSyncCalendarFlow: Flow<Boolean> = _autoSyncCalendarFlow.asStateFlow()

    override suspend fun setAutoSyncCalendar(value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_AUTO_SYNC_CALENDAR)
        _autoSyncCalendarFlow.value = value
    }

    override suspend fun getAutoSyncCalendar(): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(KEY_AUTO_SYNC_CALENDAR)
    }

    override suspend fun isFirstLaunchAfterInstall(): Boolean {
        val installId = NSUserDefaults.standardUserDefaults.stringForKey(KEY_INSTALL_ID)
        return installId == null
    }

    override suspend fun markFirstLaunchCompleted() {
        val installId = NSUUID().UUIDString
        NSUserDefaults.standardUserDefaults.setObject(installId, forKey = KEY_INSTALL_ID)
    }
}

actual fun createUserPreferencesManager(): UserPreferencesManager {
    return UserPreferencesManagerImpl()
}
