package com.district37.toastmasters.infra

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Android implementation of UserPreferencesManager using SharedPreferences.
 * Stores user preferences that persist across app launches.
 */
private class UserPreferencesManagerImpl(private val context: Context) : UserPreferencesManager {

    private companion object {
        const val PREFS_NAME = "eventsidekick_user_prefs"
        const val KEY_MY_SCHEDULE_ONLY = "my_schedule_only"
        const val KEY_AUTO_SYNC_CALENDAR = "auto_sync_calendar"
        const val KEY_INSTALL_ID = "install_id"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override val myScheduleOnlyFlow: Flow<Boolean> = callbackFlow {
        // Emit current value immediately
        trySend(sharedPreferences.getBoolean(KEY_MY_SCHEDULE_ONLY, false))

        // Listen for changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_MY_SCHEDULE_ONLY) {
                trySend(sharedPreferences.getBoolean(KEY_MY_SCHEDULE_ONLY, false))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setMyScheduleOnly(value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_MY_SCHEDULE_ONLY, value)
                .apply()
        }
    }

    override suspend fun getMyScheduleOnly(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_MY_SCHEDULE_ONLY, false)
        }
    }

    override val autoSyncCalendarFlow: Flow<Boolean> = callbackFlow {
        // Emit current value immediately
        trySend(sharedPreferences.getBoolean(KEY_AUTO_SYNC_CALENDAR, false))

        // Listen for changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_AUTO_SYNC_CALENDAR) {
                trySend(sharedPreferences.getBoolean(KEY_AUTO_SYNC_CALENDAR, false))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setAutoSyncCalendar(value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_AUTO_SYNC_CALENDAR, value)
                .apply()
        }
    }

    override suspend fun getAutoSyncCalendar(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_AUTO_SYNC_CALENDAR, false)
        }
    }

    override suspend fun isFirstLaunchAfterInstall(): Boolean {
        return withContext(Dispatchers.IO) {
            val installId = sharedPreferences.getString(KEY_INSTALL_ID, null)
            installId == null
        }
    }

    override suspend fun markFirstLaunchCompleted() {
        withContext(Dispatchers.IO) {
            val installId = UUID.randomUUID().toString()
            sharedPreferences.edit()
                .putString(KEY_INSTALL_ID, installId)
                .apply()
        }
    }
}

actual fun createUserPreferencesManager(): UserPreferencesManager {
    return object : KoinComponent {
        val context: Context by inject()
    }.let { UserPreferencesManagerImpl(it.context) }
}
