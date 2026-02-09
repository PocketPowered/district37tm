package com.district37.toastmasters.infra

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of DeveloperSettingsManager using SharedPreferences.
 * Stores developer testing flags for debugging purposes.
 */
private class DeveloperSettingsManagerImpl(private val context: Context) : DeveloperSettingsManager {

    private companion object {
        const val PREFS_NAME = "eventsidekick_dev_settings"
        const val KEY_TEST_ONBOARDING = "test_onboarding"
        const val KEY_USE_LOCALHOST_SERVER = "use_localhost_server"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun setShouldShowOnboardingOnNextLaunch(value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_TEST_ONBOARDING, value)
                .apply()
        }
    }

    override suspend fun getShouldShowOnboardingOnNextLaunch(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_TEST_ONBOARDING, false)
        }
    }

    override suspend fun clearOnboardingTestFlag() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(KEY_TEST_ONBOARDING)
                .apply()
        }
    }

    override suspend fun setUseLocalhostServer(value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(KEY_USE_LOCALHOST_SERVER, value)
                .apply()
        }
    }

    override suspend fun getUseLocalhostServer(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_USE_LOCALHOST_SERVER, false)
        }
    }
}

actual fun createDeveloperSettingsManager(): DeveloperSettingsManager {
    return object : KoinComponent {
        val context: Context by inject()
    }.let { DeveloperSettingsManagerImpl(it.context) }
}
