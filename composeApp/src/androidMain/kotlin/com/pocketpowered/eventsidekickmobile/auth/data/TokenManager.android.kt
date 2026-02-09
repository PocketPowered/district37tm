package com.district37.toastmasters.auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.district37.toastmasters.auth.models.AuthTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of TokenManager using EncryptedSharedPreferences
 * Provides secure storage for authentication tokens with AES256 encryption
 */
actual class TokenManager(private val context: Context) {

    private companion object {
        const val PREFS_NAME = "eventsidekick_auth_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual suspend fun saveTokens(tokens: AuthTokens) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                .apply()
        }
    }

    actual suspend fun getTokens(): AuthTokens? {
        return withContext(Dispatchers.IO) {
            val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

            if (accessToken != null && refreshToken != null) {
                AuthTokens(accessToken, refreshToken)
            } else {
                null
            }
        }
    }

    actual suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
        }
    }

    actual suspend fun hasTokens(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.contains(KEY_ACCESS_TOKEN) &&
                    sharedPreferences.contains(KEY_REFRESH_TOKEN)
        }
    }
}
