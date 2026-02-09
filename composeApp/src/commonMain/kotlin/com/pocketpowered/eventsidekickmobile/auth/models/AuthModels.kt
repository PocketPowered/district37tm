package com.district37.toastmasters.auth.models

import com.district37.toastmasters.models.User
import kotlinx.serialization.Serializable

/**
 * OAuth tokens for authentication
 */
@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

/**
 * Response from the /auth/refresh endpoint
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)

/**
 * Request body for /auth/oauth-url endpoint
 */
@Serializable
data class OAuthUrlRequest(
    val redirectUrl: String
)

/**
 * Response from /auth/oauth-url endpoint
 */
@Serializable
data class OAuthUrlResponse(
    val url: String
)

/**
 * Request body for /auth/refresh endpoint
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * Error response from auth endpoints
 */
@Serializable
data class AuthErrorResponse(
    val error: String,
    val message: String
)

/**
 * Authentication state for the app
 */
sealed class AuthState {
    /** Initial loading state while checking stored tokens */
    data object Loading : AuthState()

    /** User is not authenticated */
    data object Unauthenticated : AuthState()

    /** User is authenticated with valid credentials */
    data class Authenticated(val user: User) : AuthState()
}

/**
 * Login flow state
 */
sealed class LoginState {
    /** No login in progress */
    data object Idle : LoginState()

    /** Login in progress */
    data object Loading : LoginState()

    /** Login successful */
    data object Success : LoginState()

    /** Login failed with error message */
    data class Error(val message: String) : LoginState()
}

/**
 * Exception thrown when access token has expired
 */
class TokenExpiredException(message: String = "Access token has expired") : Exception(message)

/**
 * Exception thrown for general authentication errors
 */
class AuthenticationException(message: String) : Exception(message)

/**
 * Type of credential being used for OTP login
 */
enum class OtpCredentialType {
    EMAIL,
    PHONE
}

/**
 * OTP login flow state
 */
sealed class OtpLoginState {
    /** Initial input state - user entering email/phone */
    data object EnteringCredential : OtpLoginState()

    /** Sending OTP request */
    data object SendingOtp : OtpLoginState()

    /** OTP sent, waiting for user to enter code */
    data class AwaitingOtpVerification(
        val credential: String,
        val credentialType: OtpCredentialType
    ) : OtpLoginState()

    /** Verifying OTP code */
    data object VerifyingOtp : OtpLoginState()

    /** OTP verification succeeded */
    data object Success : OtpLoginState()

    /** Error occurred */
    data class Error(val message: String, val previousState: OtpLoginState) : OtpLoginState()
}
