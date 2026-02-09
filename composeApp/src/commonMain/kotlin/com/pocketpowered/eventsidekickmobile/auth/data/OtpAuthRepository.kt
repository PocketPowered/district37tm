package com.district37.toastmasters.auth.data

import com.apollographql.apollo.ApolloClient
import com.district37.toastmasters.auth.models.AuthTokens
import com.district37.toastmasters.graphql.SendEmailOtpMutation
import com.district37.toastmasters.graphql.SendPhoneOtpMutation
import com.district37.toastmasters.graphql.VerifyEmailOtpMutation
import com.district37.toastmasters.graphql.VerifyPhoneOtpMutation
import com.district37.toastmasters.graphql.type.SendEmailOtpInput
import com.district37.toastmasters.graphql.type.SendPhoneOtpInput
import com.district37.toastmasters.graphql.type.VerifyEmailOtpInput
import com.district37.toastmasters.graphql.type.VerifyPhoneOtpInput
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Logger

/**
 * Result of OTP verification containing auth tokens and user info
 */
data class OtpVerificationResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val isNewUser: Boolean,
    val user: User
)

/**
 * Repository for OTP-based authentication (email and phone).
 * Handles sending OTP codes and verifying them via GraphQL mutations.
 */
class OtpAuthRepository(
    private val apolloClient: ApolloClient,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    private val TAG = "OtpAuthRepository"

    /**
     * Send OTP code to email address
     */
    suspend fun sendEmailOtp(email: String): Result<Unit> {
        return try {
            Logger.d(TAG, "Sending email OTP to: $email")
            val response = apolloClient.mutation(
                SendEmailOtpMutation(SendEmailOtpInput(email = email))
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown error"
                Logger.e(TAG, "sendEmailOtp error: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }

            val data = response.data?.sendEmailOtp
            if (data == null) {
                Logger.e(TAG, "sendEmailOtp: No data returned")
                return Result.failure(Exception("No data returned"))
            }

            if (data.success) {
                Logger.d(TAG, "sendEmailOtp successful: ${data.message}")
                Result.success(Unit)
            } else {
                Logger.e(TAG, "sendEmailOtp failed: ${data.message}")
                Result.failure(Exception(data.message))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error sending email OTP: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Send OTP code to phone number (E.164 format)
     */
    suspend fun sendPhoneOtp(phoneNumber: String): Result<Unit> {
        return try {
            Logger.d(TAG, "Sending phone OTP to: $phoneNumber")
            val response = apolloClient.mutation(
                SendPhoneOtpMutation(SendPhoneOtpInput(phoneNumber = phoneNumber))
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown error"
                Logger.e(TAG, "sendPhoneOtp error: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }

            val data = response.data?.sendPhoneOtp
            if (data == null) {
                Logger.e(TAG, "sendPhoneOtp: No data returned")
                return Result.failure(Exception("No data returned"))
            }

            if (data.success) {
                Logger.d(TAG, "sendPhoneOtp successful: ${data.message}")
                Result.success(Unit)
            } else {
                Logger.e(TAG, "sendPhoneOtp failed: ${data.message}")
                Result.failure(Exception(data.message))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error sending phone OTP: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Verify email OTP code and authenticate user
     */
    suspend fun verifyEmailOtp(email: String, otp: String): Result<OtpVerificationResult> {
        return try {
            Logger.d(TAG, "Verifying email OTP for: $email")
            val response = apolloClient.mutation(
                VerifyEmailOtpMutation(VerifyEmailOtpInput(email = email, otp = otp))
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown error"
                Logger.e(TAG, "verifyEmailOtp error: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }

            val data = response.data?.verifyEmailOtp
            if (data == null) {
                Logger.e(TAG, "verifyEmailOtp: No data returned")
                return Result.failure(Exception("No data returned"))
            }

            // Save tokens
            val tokens = AuthTokens(data.accessToken, data.refreshToken)
            tokenManager.saveTokens(tokens)

            // Create user from response
            val user = User(
                id = data.user.id,
                email = data.user.email ?: "",
                username = data.user.username,
                displayName = data.user.displayName,
                bio = data.user.bio,
                profileImageUrl = data.user.profileImageUrl
            )

            // Refresh auth state to update the UI
            authRepository.refreshAuthState()

            Logger.d(TAG, "verifyEmailOtp successful, user: ${user.email}, isNewUser: ${data.isNewUser}")

            Result.success(OtpVerificationResult(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                expiresIn = data.expiresIn,
                isNewUser = data.isNewUser,
                user = user
            ))
        } catch (e: Exception) {
            Logger.e(TAG, "Error verifying email OTP: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Verify phone OTP code and authenticate user
     */
    suspend fun verifyPhoneOtp(phoneNumber: String, otp: String): Result<OtpVerificationResult> {
        return try {
            Logger.d(TAG, "Verifying phone OTP for: $phoneNumber")
            val response = apolloClient.mutation(
                VerifyPhoneOtpMutation(VerifyPhoneOtpInput(phoneNumber = phoneNumber, otp = otp))
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown error"
                Logger.e(TAG, "verifyPhoneOtp error: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }

            val data = response.data?.verifyPhoneOtp
            if (data == null) {
                Logger.e(TAG, "verifyPhoneOtp: No data returned")
                return Result.failure(Exception("No data returned"))
            }

            // Save tokens
            val tokens = AuthTokens(data.accessToken, data.refreshToken)
            tokenManager.saveTokens(tokens)

            // Create user from response
            val user = User(
                id = data.user.id,
                email = data.user.email ?: "",
                username = data.user.username,
                displayName = data.user.displayName,
                bio = data.user.bio,
                profileImageUrl = data.user.profileImageUrl
            )

            // Refresh auth state to update the UI
            authRepository.refreshAuthState()

            Logger.d(TAG, "verifyPhoneOtp successful, user: ${user.email}, isNewUser: ${data.isNewUser}")

            Result.success(OtpVerificationResult(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                expiresIn = data.expiresIn,
                isNewUser = data.isNewUser,
                user = user
            ))
        } catch (e: Exception) {
            Logger.e(TAG, "Error verifying phone OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
}
