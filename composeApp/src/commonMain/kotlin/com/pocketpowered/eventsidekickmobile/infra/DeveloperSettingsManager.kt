package com.district37.toastmasters.infra

/**
 * Manager for developer-only testing flags and settings.
 * Only available in debug builds.
 */
interface DeveloperSettingsManager {
    /**
     * Sets a flag to trigger the onboarding wizard on next app launch.
     * Used for testing the onboarding flow without creating a new account.
     */
    suspend fun setShouldShowOnboardingOnNextLaunch(value: Boolean)

    /**
     * Checks if the onboarding wizard should be shown on this launch.
     */
    suspend fun getShouldShowOnboardingOnNextLaunch(): Boolean

    /**
     * Clears the onboarding test flag.
     * Called after the onboarding wizard is completed during testing.
     */
    suspend fun clearOnboardingTestFlag()

    /**
     * Sets whether to use localhost server instead of production.
     * Requires app restart to take effect.
     */
    suspend fun setUseLocalhostServer(value: Boolean)

    /**
     * Checks if localhost server should be used.
     */
    suspend fun getUseLocalhostServer(): Boolean
}

/**
 * Platform-specific factory for creating DeveloperSettingsManager.
 * Android uses SharedPreferences, iOS uses UserDefaults.
 */
expect fun createDeveloperSettingsManager(): DeveloperSettingsManager
