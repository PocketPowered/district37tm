package com.district37.toastmasters.auth.data

import com.district37.toastmasters.auth.models.AuthTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of TokenManager using NSUserDefaults
 * TODO: Migrate to Keychain for production - NSUserDefaults is not secure for tokens
 */
actual class TokenManager {

    private companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }

    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun saveTokens(tokens: AuthTokens) {
        withContext(Dispatchers.IO) {
            userDefaults.setObject(tokens.accessToken, forKey = KEY_ACCESS_TOKEN)
            userDefaults.setObject(tokens.refreshToken, forKey = KEY_REFRESH_TOKEN)
            userDefaults.synchronize()
        }
    }

    actual suspend fun getTokens(): AuthTokens? {
        return withContext(Dispatchers.IO) {
            val accessToken = userDefaults.stringForKey(KEY_ACCESS_TOKEN)
            val refreshToken = userDefaults.stringForKey(KEY_REFRESH_TOKEN)

            if (accessToken != null && refreshToken != null) {
                AuthTokens(accessToken, refreshToken)
            } else {
                null
            }
        }
    }

    actual suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN)
            userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
            userDefaults.synchronize()
        }
    }

    actual suspend fun hasTokens(): Boolean {
        return withContext(Dispatchers.IO) {
            userDefaults.stringForKey(KEY_ACCESS_TOKEN) != null &&
                userDefaults.stringForKey(KEY_REFRESH_TOKEN) != null
        }
    }
}
