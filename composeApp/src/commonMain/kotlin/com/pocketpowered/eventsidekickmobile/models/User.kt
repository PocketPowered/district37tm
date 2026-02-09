package com.district37.toastmasters.models

import com.district37.toastmasters.infra.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Unified user model combining authentication and profile data.
 * This replaces the previous AuthUser + UserProfileModel dual-model approach.
 */
@Serializable
data class User(
    // Core Auth Fields (always present from JWT)
    val id: String,
    val email: String,

    // Profile Fields (from user_profiles table - nullable for new users)
    val username: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val primaryColor: String? = null,
    val secondaryColor: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val preferences: UserPreferences? = null,

    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant? = null,

    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null
) {
    /**
     * Display name with fallback priority:
     * 1. Custom displayName (user-set)
     * 2. Username
     * 3. Email
     */
    val effectiveDisplayName: String
        get() = displayName ?: username ?: email

    /**
     * Avatar URL (user-uploaded profile image)
     */
    val effectiveAvatarUrl: String?
        get() = profileImageUrl

    /**
     * Whether user has customized their profile.
     * Useful for showing "Complete your profile" prompts.
     */
    val hasCustomProfile: Boolean
        get() = username != null || displayName != null ||
                bio != null || profileImageUrl != null

    /**
     * Whether user needs to complete onboarding.
     * Used to determine if onboarding screen should be shown.
     */
    val needsOnboarding: Boolean
        get() = !hasCompletedOnboarding
}

/**
 * User preferences for app settings
 */
@Serializable
data class UserPreferences(
    val theme: String? = null,
    val notificationsEnabled: Boolean? = null,
    val displayDensity: String? = null
)
