package com.district37.toastmasters.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.DisplayFormatters
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the Edit Profile screen
 */
data class EditProfileState(
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val primaryColor: String? = null,
    val secondaryColor: String? = null,
    val selectedImageBytes: ByteArray? = null,
    val usernameError: String? = null,
    val isCheckingUsername: Boolean = false,
    val isUsernameAvailable: Boolean? = null,
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // UI state management (issues #1, #22)
    val showUrlInputSheet: Boolean = false,
    val tempImageUrl: String = "",
    val showImageOptionsSheet: Boolean = false
)

/**
 * ViewModel for the Edit Profile screen
 * Manages editing and saving user profile data
 */
@OptIn(FlowPreview::class)
class EditProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val authRepository: AuthRepository,
    user: User
) : ViewModel() {

    companion object {
        private const val USERNAME_REGEX = "^[a-z0-9_]{3,18}$"
        private const val DEBOUNCE_MILLIS = 500L
    }

    private val tag = "EditProfileViewModel"

    private val _state = MutableStateFlow(
        EditProfileState(
            displayName = user.displayName ?: "",
            username = user.username ?: "",
            bio = user.bio ?: "",
            profileImageUrl = user.profileImageUrl ?: "",
            primaryColor = user.primaryColor,
            secondaryColor = user.secondaryColor
        )
    )
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    // Derived states (issues #2, #17)
    val userInitials: StateFlow<String> = state.map { s ->
        DisplayFormatters.formatUserInitials(s.displayName.ifBlank { s.username })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Store initial values for change detection
    private val initialDisplayName = user.displayName ?: ""
    private val initialUsername = user.username ?: ""
    private val initialBio = user.bio ?: ""
    private val initialProfileImageUrl = user.profileImageUrl ?: ""
    private val initialPrimaryColor = user.primaryColor
    private val initialSecondaryColor = user.secondaryColor
    private val userId = user.id

    // For username change detection
    private val _usernameFlow = MutableStateFlow(user.username ?: "")

    init {
        // Set up debounced username availability checking
        viewModelScope.launch {
            _usernameFlow
                .debounce(DEBOUNCE_MILLIS)
                .collect { username ->
                    // Only check if username has changed from initial
                    if (username.isNotBlank() && username != initialUsername) {
                        validateAndCheckUsername(username)
                    } else {
                        _state.update { it.copy(usernameError = null, isUsernameAvailable = null) }
                    }
                }
        }
    }

    /**
     * Update the display name field
     */
    fun onDisplayNameChanged(value: String) {
        _state.update { it.copy(displayName = value, errorMessage = null) }
    }

    /**
     * Update the username field
     */
    fun onUsernameChanged(value: String) {
        // Convert to lowercase automatically
        val lowercased = value.lowercase()
        _state.update { it.copy(username = lowercased, usernameError = null, isUsernameAvailable = null) }
        _usernameFlow.value = lowercased
    }

    /**
     * Update the bio field
     */
    fun onBioChanged(value: String) {
        _state.update { it.copy(bio = value, errorMessage = null) }
    }

    /**
     * Update the profile image URL field
     */
    fun onProfileImageUrlChanged(value: String) {
        _state.update { it.copy(profileImageUrl = value, selectedImageBytes = null, errorMessage = null) }
    }

    /**
     * Handle image selection from gallery or camera
     */
    fun onImageSelected(bytes: ByteArray) {
        _state.update { it.copy(selectedImageBytes = bytes, profileImageUrl = "", errorMessage = null) }
    }

    /**
     * Clear the selected image
     */
    fun clearSelectedImage() {
        _state.update { it.copy(selectedImageBytes = null) }
    }

    /**
     * Update the primary (banner) color
     */
    fun onPrimaryColorChanged(value: String?) {
        _state.update { it.copy(primaryColor = value, errorMessage = null) }
    }

    /**
     * Update the secondary (background) color
     */
    fun onSecondaryColorChanged(value: String?) {
        _state.update { it.copy(secondaryColor = value, errorMessage = null) }
    }

    /**
     * Validate username format and check availability
     */
    private suspend fun validateAndCheckUsername(username: String) {
        // Validate format first
        if (!username.matches(Regex(USERNAME_REGEX))) {
            _state.update {
                it.copy(
                    usernameError = when {
                        username.length < 3 -> "Username must be at least 3 characters"
                        username.length > 18 -> "Username must be at most 18 characters"
                        else -> "Username can only contain lowercase letters, numbers, and underscores"
                    },
                    isUsernameAvailable = false,
                    isCheckingUsername = false
                )
            }
            return
        }

        // Check availability
        _state.update { it.copy(isCheckingUsername = true, usernameError = null) }

        when (val result = userProfileRepository.checkUsernameAvailability(username)) {
            is Resource.Success -> {
                _state.update {
                    it.copy(
                        isCheckingUsername = false,
                        isUsernameAvailable = result.data,
                        usernameError = if (!result.data) "Username is already taken" else null
                    )
                }
            }
            is Resource.Error -> {
                Logger.e(tag, "Error checking username availability: ${result.message}")
                _state.update {
                    it.copy(
                        isCheckingUsername = false,
                        usernameError = "Couldn't check username availability",
                        isUsernameAvailable = null
                    )
                }
            }
            is Resource.Loading -> { }
            is Resource.NotLoading -> { }
        }
    }

    /**
     * Save the profile changes
     */
    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }

            val currentState = _state.value

            // Handle image upload if device image was selected
            var finalProfileImageUrl: String? = null
            val imageBytes = currentState.selectedImageBytes

            if (imageBytes != null) {
                _state.update { it.copy(isUploadingImage = true) }
                Logger.d(tag, "Uploading profile image...")

                val uploadResult = imageUploadRepository.uploadImage(
                    imageBytes = imageBytes,
                    entityType = "profile",
                    entityId = userId,
                    filename = "profile_picture.jpg",
                    contentType = "image/jpeg"
                )

                _state.update { it.copy(isUploadingImage = false) }

                when (uploadResult) {
                    is Resource.Success -> {
                        Logger.d(tag, "Image uploaded successfully: ${uploadResult.data}")
                        finalProfileImageUrl = uploadResult.data
                    }
                    is Resource.Error -> {
                        Logger.e(tag, "Failed to upload image: ${uploadResult.message}")
                        _state.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = uploadResult.message ?: "Failed to upload image"
                            )
                        }
                        return@launch
                    }
                    else -> {}
                }
            } else if (currentState.profileImageUrl != initialProfileImageUrl) {
                // URL was changed directly (not via upload)
                finalProfileImageUrl = currentState.profileImageUrl
            }

            // Determine which fields have changed
            val displayNameChanged = currentState.displayName != initialDisplayName
            val usernameChanged = currentState.username != initialUsername
            val bioChanged = currentState.bio != initialBio
            val profileImageUrlChanged = finalProfileImageUrl != null
            val primaryColorChanged = currentState.primaryColor != initialPrimaryColor
            val secondaryColorChanged = currentState.secondaryColor != initialSecondaryColor

            // Only send changed fields
            val result = userProfileRepository.updateMyProfile(
                displayName = if (displayNameChanged) currentState.displayName else null,
                username = if (usernameChanged) currentState.username else null,
                bio = if (bioChanged) currentState.bio else null,
                profileImageUrl = if (profileImageUrlChanged) finalProfileImageUrl else null,
                primaryColor = if (primaryColorChanged) currentState.primaryColor else null,
                secondaryColor = if (secondaryColorChanged) currentState.secondaryColor else null
            )

            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isSaving = false, selectedImageBytes = null, saveSuccess = true) }

                    // Refresh auth state to get updated profile in AuthState
                    authRepository.refreshAuthState()
                    Logger.d(tag, "Profile saved and auth state refreshed")
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to save profile: ${result.message}")
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message ?: "Failed to save profile"
                        )
                    }
                }
                is Resource.Loading, is Resource.NotLoading -> {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Clear the error message
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Reset the save success flag
     */
    fun resetSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    // Image options sheet management (issue #22)
    fun openImageOptions() {
        _state.update { it.copy(showImageOptionsSheet = true) }
    }

    fun closeImageOptions() {
        _state.update { it.copy(showImageOptionsSheet = false) }
    }

    // Image URL input sheet management (issue #1)
    fun openUrlInput() {
        _state.update { it.copy(showUrlInputSheet = true, tempImageUrl = state.value.profileImageUrl) }
    }

    fun closeUrlInput() {
        _state.update { it.copy(showUrlInputSheet = false, tempImageUrl = "") }
    }

    fun onTempImageUrlChanged(value: String) {
        _state.update { it.copy(tempImageUrl = value) }
    }

    fun confirmImageUrl() {
        _state.update { it.copy(profileImageUrl = it.tempImageUrl, selectedImageBytes = null, showUrlInputSheet = false, tempImageUrl = "") }
    }
}
