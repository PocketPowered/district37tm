package com.district37.toastmasters.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.data.OtpAuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.auth.models.LoginState
import com.district37.toastmasters.auth.models.OtpCredentialType
import com.district37.toastmasters.auth.models.OtpLoginState
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.infra.OAuthBrowser
import com.district37.toastmasters.infra.OAUTH_REDIRECT_URL
import com.district37.toastmasters.infra.PushNotificationService
import com.district37.toastmasters.messaging.UnreadMessagesManager
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication UI state.
 * Handles login flow, OAuth callbacks, and logout.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val otpAuthRepository: OtpAuthRepository,
    private val oAuthBrowser: OAuthBrowser,
    private val pushNotificationService: PushNotificationService,
    private val notificationRepository: NotificationRepository,
    private val platform: Platform,
    private val unreadMessagesManager: UnreadMessagesManager
) : ViewModel() {

    private val TAG = "AuthViewModel"

    /**
     * Current authentication state
     */
    val authState: StateFlow<AuthState> = authRepository.authState

    /**
     * Login flow state for UI (Google OAuth)
     */
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    /**
     * OTP login flow state for UI (Email/Phone)
     */
    private val _otpLoginState = MutableStateFlow<OtpLoginState>(OtpLoginState.EnteringCredential)
    val otpLoginState: StateFlow<OtpLoginState> = _otpLoginState.asStateFlow()

    // Track current OTP credential for resend functionality
    private var currentOtpCredential: String? = null
    private var currentOtpCredentialType: OtpCredentialType? = null

    init {
        // Initialize auth state on creation
        viewModelScope.launch {
            authRepository.initialize()
        }

        // Start/stop unread messages manager based on auth state
        viewModelScope.launch {
            authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        Logger.d(TAG, "User authenticated, starting unread messages manager")
                        unreadMessagesManager.start()
                    }
                    is AuthState.Unauthenticated -> {
                        Logger.d(TAG, "User logged out, stopping unread messages manager")
                        unreadMessagesManager.stop()
                    }
                    else -> {}
                }
            }
        }

    }

    /**
     * Start Google login flow.
     * Opens browser with OAuth URL and waits for callback.
     */
    fun startGoogleLogin() {
        if (_loginState.value == LoginState.Loading) {
            Logger.d(TAG, "Login already in progress, ignoring")
            return
        }

        viewModelScope.launch {
            _loginState.update { LoginState.Loading }

            try {
                // Get OAuth URL from server
                val oauthUrlResult = authRepository.getOAuthUrl(OAUTH_REDIRECT_URL)

                oauthUrlResult
                    .onSuccess { oauthUrl ->
                        Logger.d(TAG, "Got OAuth URL, launching browser...")

                        // Launch OAuth browser and wait for callback
                        val callbackResult = oAuthBrowser.launchOAuth(oauthUrl)

                        callbackResult
                            .onSuccess { callbackUrl ->
                                Logger.d(TAG, "Got callback URL, processing...")
                                handleOAuthCallback(callbackUrl)
                            }
                            .onFailure { error ->
                                Logger.e(TAG, "OAuth browser failed: ${error.message}")
                                _loginState.update { LoginState.Error(error.message ?: "Login cancelled") }
                            }
                    }
                    .onFailure { error ->
                        Logger.e(TAG, "Failed to get OAuth URL: ${error.message}")
                        _loginState.update { LoginState.Error(error.message ?: "Failed to start login") }
                    }

            } catch (e: Exception) {
                Logger.e(TAG, "Login error: ${e.message}")
                _loginState.update { LoginState.Error(e.message ?: "Login failed") }
            }
        }
    }

    /**
     * Handle OAuth callback URL from browser.
     * Parses tokens from URL and completes authentication.
     */
    fun handleOAuthCallback(callbackUrl: String) {
        viewModelScope.launch {
            Logger.d(TAG, "Processing OAuth callback...")

            val tokens = parseTokensFromUrl(callbackUrl)
            if (tokens != null) {
                val (accessToken, refreshToken) = tokens

                authRepository.handleOAuthCallback(accessToken, refreshToken)
                    .onSuccess {
                        Logger.d(TAG, "Login successful!")
                        _loginState.update { LoginState.Success }
                        // Register device token for push notifications
                        registerDeviceTokenAfterLogin()
                    }
                    .onFailure { error ->
                        Logger.e(TAG, "Failed to process tokens: ${error.message}")
                        _loginState.update { LoginState.Error(error.message ?: "Failed to complete login") }
                    }
            } else {
                Logger.e(TAG, "Failed to parse tokens from callback URL")
                _loginState.update { LoginState.Error("Failed to parse authentication response") }
            }
        }
    }

    /**
     * Logout the current user.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.update { LoginState.Idle }
        }
    }

    /**
     * Reset login state (e.g., after dismissing error).
     */
    fun resetLoginState() {
        _loginState.update { LoginState.Idle }
    }

    /**
     * Register device token for push notifications after successful login.
     * Only registers if notifications are already enabled.
     */
    private fun registerDeviceTokenAfterLogin() {
        viewModelScope.launch {
            try {
                // Only register if notifications are already enabled
                // We don't prompt for permission here - that's done in the notification screen
                val notificationsEnabled = pushNotificationService.areNotificationsEnabled()
                if (!notificationsEnabled) {
                    Logger.d(TAG, "Notifications not enabled, skipping token registration")
                    return@launch
                }

                val fcmToken = pushNotificationService.getFcmToken()
                if (fcmToken == null) {
                    Logger.d(TAG, "No FCM token available, skipping registration")
                    return@launch
                }

                notificationRepository.registerDeviceToken(
                    fcmToken = fcmToken,
                    platform = platform,
                    deviceName = null
                )
                Logger.d(TAG, "Device token registered successfully after login")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to register device token: ${e.message}")
            }
        }
    }

    /**
     * Parse access token and refresh token from OAuth callback URL.
     * Tokens are in the URL fragment (hash): #access_token=...&refresh_token=...
     */
    private fun parseTokensFromUrl(url: String): Pair<String, String>? {
        return try {
            // URL format: eventsidekick://auth-callback#access_token=xxx&refresh_token=yyy&...
            val fragment = url.substringAfter("#", "")
            if (fragment.isEmpty()) {
                Logger.e(TAG, "No fragment in callback URL")
                return null
            }

            val params = fragment.split("&").associate { param ->
                val (key, value) = param.split("=", limit = 2)
                key to value
            }

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]

            if (accessToken != null && refreshToken != null) {
                Pair(accessToken, refreshToken)
            } else {
                Logger.e(TAG, "Missing tokens in callback URL. Access: ${accessToken != null}, Refresh: ${refreshToken != null}")
                null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error parsing tokens from URL: ${e.message}")
            null
        }
    }

    // =====================
    // OTP Authentication Methods
    // =====================

    /**
     * Send OTP to email address.
     */
    fun sendEmailOtp(email: String) {
        if (_otpLoginState.value is OtpLoginState.SendingOtp) {
            Logger.d(TAG, "OTP send already in progress, ignoring")
            return
        }

        viewModelScope.launch {
            _otpLoginState.update { OtpLoginState.SendingOtp }
            currentOtpCredential = email
            currentOtpCredentialType = OtpCredentialType.EMAIL

            try {
                otpAuthRepository.sendEmailOtp(email)
                    .onSuccess {
                        Logger.d(TAG, "Email OTP sent successfully to $email")
                        _otpLoginState.update {
                            OtpLoginState.AwaitingOtpVerification(
                                credential = email,
                                credentialType = OtpCredentialType.EMAIL
                            )
                        }
                    }
                    .onFailure { error ->
                        Logger.e(TAG, "Failed to send email OTP: ${error.message}")
                        _otpLoginState.update {
                            OtpLoginState.Error(
                                message = error.message ?: "Failed to send verification code",
                                previousState = OtpLoginState.EnteringCredential
                            )
                        }
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Error sending email OTP: ${e.message}")
                _otpLoginState.update {
                    OtpLoginState.Error(
                        message = e.message ?: "Failed to send verification code",
                        previousState = OtpLoginState.EnteringCredential
                    )
                }
            }
        }
    }

    /**
     * Send OTP to phone number.
     */
    fun sendPhoneOtp(phoneNumber: String) {
        if (_otpLoginState.value is OtpLoginState.SendingOtp) {
            Logger.d(TAG, "OTP send already in progress, ignoring")
            return
        }

        viewModelScope.launch {
            _otpLoginState.update { OtpLoginState.SendingOtp }
            currentOtpCredential = phoneNumber
            currentOtpCredentialType = OtpCredentialType.PHONE

            try {
                otpAuthRepository.sendPhoneOtp(phoneNumber)
                    .onSuccess {
                        Logger.d(TAG, "Phone OTP sent successfully to $phoneNumber")
                        _otpLoginState.update {
                            OtpLoginState.AwaitingOtpVerification(
                                credential = phoneNumber,
                                credentialType = OtpCredentialType.PHONE
                            )
                        }
                    }
                    .onFailure { error ->
                        Logger.e(TAG, "Failed to send phone OTP: ${error.message}")
                        _otpLoginState.update {
                            OtpLoginState.Error(
                                message = error.message ?: "Failed to send verification code",
                                previousState = OtpLoginState.EnteringCredential
                            )
                        }
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Error sending phone OTP: ${e.message}")
                _otpLoginState.update {
                    OtpLoginState.Error(
                        message = e.message ?: "Failed to send verification code",
                        previousState = OtpLoginState.EnteringCredential
                    )
                }
            }
        }
    }

    /**
     * Verify OTP code entered by user.
     */
    fun verifyOtp(otp: String) {
        val credential = currentOtpCredential
        val credentialType = currentOtpCredentialType

        if (credential == null || credentialType == null) {
            Logger.e(TAG, "No credential to verify OTP against")
            return
        }

        val currentState = _otpLoginState.value
        if (currentState !is OtpLoginState.AwaitingOtpVerification &&
            currentState !is OtpLoginState.Error) {
            Logger.d(TAG, "Not in a state to verify OTP, ignoring")
            return
        }

        viewModelScope.launch {
            val previousState = if (currentState is OtpLoginState.AwaitingOtpVerification) {
                currentState
            } else {
                (currentState as OtpLoginState.Error).previousState as? OtpLoginState.AwaitingOtpVerification
            }

            _otpLoginState.update { OtpLoginState.VerifyingOtp }

            try {
                val result = when (credentialType) {
                    OtpCredentialType.EMAIL -> otpAuthRepository.verifyEmailOtp(credential, otp)
                    OtpCredentialType.PHONE -> otpAuthRepository.verifyPhoneOtp(credential, otp)
                }

                result
                    .onSuccess { verificationResult ->
                        Logger.d(TAG, "OTP verified successfully")
                        // Handle the tokens from verification
                        authRepository.handleOAuthCallback(
                            verificationResult.accessToken,
                            verificationResult.refreshToken
                        ).onSuccess {
                            _otpLoginState.update { OtpLoginState.Success }
                            // Register device token for push notifications
                            registerDeviceTokenAfterLogin()
                        }.onFailure { error ->
                            Logger.e(TAG, "Failed to process OTP tokens: ${error.message}")
                            _otpLoginState.update {
                                OtpLoginState.Error(
                                    message = error.message ?: "Failed to complete login",
                                    previousState = previousState ?: OtpLoginState.EnteringCredential
                                )
                            }
                        }
                    }
                    .onFailure { error ->
                        Logger.e(TAG, "Failed to verify OTP: ${error.message}")
                        _otpLoginState.update {
                            OtpLoginState.Error(
                                message = error.message ?: "Invalid verification code",
                                previousState = previousState ?: OtpLoginState.EnteringCredential
                            )
                        }
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Error verifying OTP: ${e.message}")
                _otpLoginState.update {
                    OtpLoginState.Error(
                        message = e.message ?: "Verification failed",
                        previousState = previousState ?: OtpLoginState.EnteringCredential
                    )
                }
            }
        }
    }

    /**
     * Resend OTP to the current credential.
     */
    fun resendOtp() {
        val credential = currentOtpCredential
        val credentialType = currentOtpCredentialType

        if (credential == null || credentialType == null) {
            Logger.e(TAG, "No credential to resend OTP to")
            return
        }

        when (credentialType) {
            OtpCredentialType.EMAIL -> sendEmailOtp(credential)
            OtpCredentialType.PHONE -> sendPhoneOtp(credential)
        }
    }

    /**
     * Reset OTP login state back to entering credential.
     */
    fun resetOtpState() {
        currentOtpCredential = null
        currentOtpCredentialType = null
        _otpLoginState.update { OtpLoginState.EnteringCredential }
    }
}
