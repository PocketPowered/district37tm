package com.district37.toastmasters.auth.data

import com.district37.toastmasters.auth.models.AuthTokens

/**
 * Platform-specific secure token storage
 * - Android: Uses EncryptedSharedPreferences
 * - iOS: Uses Keychain Services
 */
expect class TokenManager {
    /**
     * Save authentication tokens securely
     */
    suspend fun saveTokens(tokens: AuthTokens)

    /**
     * Retrieve stored authentication tokens
     * @return AuthTokens if available, null otherwise
     */
    suspend fun getTokens(): AuthTokens?

    /**
     * Clear all stored authentication tokens
     */
    suspend fun clearTokens()

    /**
     * Check if tokens are stored
     */
    suspend fun hasTokens(): Boolean
}
