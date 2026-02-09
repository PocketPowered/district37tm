package com.district37.toastmasters.infra

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of DeveloperSettingsManager using UserDefaults.
 * Stores developer testing flags for debugging purposes.
 */
private class DeveloperSettingsManagerImpl : DeveloperSettingsManager {

    private companion object {
        const val KEY_TEST_ONBOARDING = "dev_test_onboarding"
        const val KEY_USE_LOCALHOST_SERVER = "dev_use_localhost_server"
    }

    override suspend fun setShouldShowOnboardingOnNextLaunch(value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_TEST_ONBOARDING)
    }

    override suspend fun getShouldShowOnboardingOnNextLaunch(): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(KEY_TEST_ONBOARDING)
    }

    override suspend fun clearOnboardingTestFlag() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_TEST_ONBOARDING)
    }

    override suspend fun setUseLocalhostServer(value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_USE_LOCALHOST_SERVER)
    }

    override suspend fun getUseLocalhostServer(): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(KEY_USE_LOCALHOST_SERVER)
    }
}

actual fun createDeveloperSettingsManager(): DeveloperSettingsManager {
    return DeveloperSettingsManagerImpl()
}
