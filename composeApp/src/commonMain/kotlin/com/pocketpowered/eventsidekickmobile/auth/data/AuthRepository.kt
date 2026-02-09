package com.district37.toastmasters.auth.data

import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.auth.models.AuthTokens
import com.district37.toastmasters.auth.models.TokenExpiredException
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.infra.PushNotificationService
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central repository for authentication state management.
 * Handles token storage, validation, and automatic refresh.
 */
class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val tokenManager: TokenManager,
    private val pushNotificationService: PushNotificationService,
    private val notificationRepositoryProvider: () -> NotificationRepository
) {
    private val TAG = "AuthRepository"

    private val authDisabled = true

    private val guestUser = User(
        id = "district37-guest",
        email = "guest@district37.toastmasters",
        displayName = "District 37",
        hasCompletedOnboarding = true
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val refreshMutex = Mutex()
    private val initMutex = Mutex()
    private var isInitialized = false

    /**
     * Initialize auth state by checking for stored tokens.
     * Should be called on app startup. Guarded to prevent duplicate initialization.
     */
    suspend fun initialize() {
        initMutex.withLock {
            if (isInitialized) {
                Logger.d(TAG, "Already initialized, skipping")
                return
            }

            Logger.d(TAG, "Initializing auth state...")

            if (authDisabled) {
                _authState.update { AuthState.Authenticated(guestUser) }
                isInitialized = true
                return
            }

            val tokens = tokenManager.getTokens()
            if (tokens != null) {
                Logger.d(TAG, "Found stored tokens, validating...")
                validateAndSetAuthState(tokens)
            } else {
                Logger.d(TAG, "No stored tokens, setting unauthenticated state")
                _authState.update { AuthState.Unauthenticated }
            }

            isInitialized = true
        }
    }

    /**
     * Get the OAuth URL for Google sign-in.
     */
    suspend fun getOAuthUrl(redirectUrl: String): Result<String> {
        if (authDisabled) {
            return Result.failure(IllegalStateException("Auth disabled"))
        }
        return authApiClient.getOAuthUrl(redirectUrl)
    }

    /**
     * Handle OAuth callback after user authenticates.
     * Stores tokens and fetches user info.
     */
    suspend fun handleOAuthCallback(accessToken: String, refreshToken: String): Result<User> {
        if (authDisabled) {
            return Result.failure(IllegalStateException("Auth disabled"))
        }
        Logger.d(TAG, "Handling OAuth callback...")

        val tokens = AuthTokens(accessToken, refreshToken)

        // Validate the token and get user info
        return authApiClient.getCurrentUser(accessToken)
            .onSuccess { user ->
                // Save tokens and update state
                tokenManager.saveTokens(tokens)
                _authState.update { AuthState.Authenticated(user) }
                Logger.d(TAG, "OAuth callback successful, user: ${user.email}")
                Logger.d(TAG, "Access token received: $accessToken")
            }
            .onFailure { error ->
                Logger.e(TAG, "OAuth callback failed: ${error.message}")
                _authState.update { AuthState.Unauthenticated }
            }
    }

    /**
     * Get a valid access token for API requests.
     * Will automatically refresh if the current token is expired.
     * Returns null if not authenticated or refresh fails.
     */
    suspend fun getValidAccessToken(): String? {
        if (authDisabled) {
            return null
        }
        val tokens = tokenManager.getTokens() ?: return null

        // Try to get current user to check if token is valid
        val result = authApiClient.getCurrentUser(tokens.accessToken)

        return when {
            result.isSuccess -> tokens.accessToken
            result.exceptionOrNull() is TokenExpiredException -> {
                // Token expired, try to refresh
                Logger.d(TAG, "Access token expired, attempting refresh...")
                refreshTokensIfNeeded()?.accessToken
            }
            else -> {
                Logger.e(TAG, "Token validation failed: ${result.exceptionOrNull()?.message}")
                null
            }
        }
    }

    /**
     * Refresh tokens if needed. Uses mutex to prevent concurrent refresh attempts.
     * Returns new tokens if refresh successful, null otherwise.
     */
    suspend fun refreshTokensIfNeeded(): AuthTokens? {
        if (authDisabled) {
            return null
        }
        return refreshMutex.withLock {
            val currentTokens = tokenManager.getTokens() ?: return@withLock null

            Logger.d(TAG, "Refreshing tokens...")

            authApiClient.refreshToken(currentTokens.refreshToken)
                .onSuccess { response ->
                    val newTokens = AuthTokens(response.accessToken, response.refreshToken)
                    tokenManager.saveTokens(newTokens)
                    _authState.update { AuthState.Authenticated(response.user) }
                    Logger.d(TAG, "Token refresh successful")
                    return@withLock newTokens
                }
                .onFailure { error ->
                    Logger.e(TAG, "Token refresh failed: ${error.message}")
                    // Refresh failed, logout user
                    logout()
                }

            null
        }
    }

    /**
     * Logout the current user.
     * Unregisters device token, invalidates session on server, then clears stored tokens and updates auth state.
     */
    suspend fun logout() {
        if (authDisabled) {
            _authState.update { AuthState.Authenticated(guestUser) }
            return
        }
        Logger.d(TAG, "Logging out user...")

        // Unregister device token to stop receiving push notifications
        try {
            val fcmToken = pushNotificationService.getFcmToken()
            if (fcmToken != null) {
                notificationRepositoryProvider().unregisterDeviceToken(fcmToken)
                Logger.d(TAG, "Device token unregistered")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to unregister device token: ${e.message}")
        }

        // Try to invalidate session on server first
        val tokens = tokenManager.getTokens()
        if (tokens != null) {
            authApiClient.logout(tokens.accessToken)
        }

        // Always clear local tokens regardless of server response
        tokenManager.clearTokens()
        _authState.update { AuthState.Unauthenticated }
    }

    /**
     * Validate tokens and set auth state accordingly.
     */
    internal suspend fun validateAndSetAuthState(tokens: AuthTokens) {
        if (authDisabled) {
            _authState.update { AuthState.Authenticated(guestUser) }
            return
        }
        authApiClient.getCurrentUser(tokens.accessToken)
            .onSuccess { user ->
                _authState.update { AuthState.Authenticated(user) }
                Logger.d(TAG, "Token validation successful, user: ${user.email}")
            }
            .onFailure { error ->
                if (error is TokenExpiredException) {
                    Logger.d(TAG, "Stored token expired, attempting refresh...")
                    // Try to refresh
                    val refreshResult = refreshTokensIfNeeded()
                    if (refreshResult == null) {
                        _authState.update { AuthState.Unauthenticated }
                    }
                } else {
                    Logger.e(TAG, "Token validation failed: ${error.message}")
                    tokenManager.clearTokens()
                    _authState.update { AuthState.Unauthenticated }
                }
            }
    }

    /**
     * Refresh the auth state by re-fetching user data.
     * Used after profile updates to ensure auth state reflects latest changes.
     */
    suspend fun refreshAuthState() {
        if (authDisabled) {
            _authState.update { AuthState.Authenticated(guestUser) }
            return
        }
        val tokens = tokenManager.getTokens() ?: return
        validateAndSetAuthState(tokens)
    }
}
