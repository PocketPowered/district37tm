package com.district37.toastmasters.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the onboarding wizard.
 * Manages a 4-step wizard flow: Display Name → Username → Bio → Theme Colors
 */
@OptIn(FlowPreview::class)
class OnboardingWizardViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingWizardViewModel"
        private const val USERNAME_REGEX = "^[a-z0-9_]{3,18}$"
        private const val DEBOUNCE_MILLIS = 500L
        const val TOTAL_STEPS = 4
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    val totalSteps = TOTAL_STEPS

    // Step 1: Display Name
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    // Step 2: Username
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _isCheckingUsername = MutableStateFlow(false)
    val isCheckingUsername: StateFlow<Boolean> = _isCheckingUsername.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable: StateFlow<Boolean?> = _isUsernameAvailable.asStateFlow()

    // Step 3: Bio
    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    // Step 4: Theme Colors (optional)
    private val _primaryColor = MutableStateFlow<String?>(null)
    val primaryColor: StateFlow<String?> = _primaryColor.asStateFlow()

    private val _secondaryColor = MutableStateFlow<String?>(null)
    val secondaryColor: StateFlow<String?> = _secondaryColor.asStateFlow()

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _completionResult = MutableStateFlow<Resource<User>?>(null)
    val completionResult: StateFlow<Resource<User>?> = _completionResult.asStateFlow()

    // Derived first name for personalization in step 2
    val firstName: StateFlow<String> = _displayName.map {
        it.trim().split(" ").firstOrNull()?.ifBlank { "there" } ?: "there"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "there"
    )

    // Per-step validation for "Continue" button
    // Combine username validation states into a single flow
    private val _isUsernameValid = combine(
        _username,
        _usernameError,
        _isCheckingUsername,
        _isUsernameAvailable
    ) { username, usernameError, isChecking, isAvailable ->
        username.trim().length >= 3 && usernameError == null && !isChecking && isAvailable == true
    }

    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _displayName,
        _isUsernameValid
    ) { step, displayName, isUsernameValid ->
        when (step) {
            0 -> displayName.trim().isNotBlank()  // Step 1: name required
            1 -> isUsernameValid  // Step 2: valid username
            2 -> true  // Step 3: bio optional, always can proceed
            3 -> true  // Step 4: colors optional, always can proceed
            else -> false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Set up debounced username availability checking
        viewModelScope.launch {
            _username
                .debounce(DEBOUNCE_MILLIS)
                .collect { username ->
                    if (username.isNotBlank()) {
                        validateAndCheckUsername(username)
                    } else {
                        _usernameError.value = null
                        _isUsernameAvailable.value = null
                    }
                }
        }
    }

    fun goToNextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.update { it + 1 }
        }
    }

    fun goToPreviousStep(): Boolean {
        return if (_currentStep.value > 0) {
            _currentStep.update { it - 1 }
            true
        } else {
            false  // Already at first step - signal cannot go back
        }
    }

    fun updateDisplayName(value: String) {
        _displayName.value = value
    }

    fun updateUsername(value: String) {
        // Convert to lowercase automatically
        val lowercased = value.lowercase()
        _username.value = lowercased

        // Reset states while user is typing
        _isUsernameAvailable.value = null
        _usernameError.value = null
    }

    fun updateBio(value: String) {
        _bio.value = value
    }

    fun updatePrimaryColor(value: String) {
        _primaryColor.value = value
    }

    fun updateSecondaryColor(value: String) {
        _secondaryColor.value = value
    }

    private suspend fun validateAndCheckUsername(username: String) {
        // Validate format first
        if (!username.matches(Regex(USERNAME_REGEX))) {
            _usernameError.value = when {
                username.length < 3 -> "Username must be at least 3 characters"
                username.length > 18 -> "Username must be at most 18 characters"
                else -> "Username can only contain lowercase letters, numbers, and underscores"
            }
            _isUsernameAvailable.value = false
            return
        }

        // Check availability
        _isCheckingUsername.value = true
        _usernameError.value = null

        when (val result = userProfileRepository.checkUsernameAvailability(username)) {
            is Resource.Success -> {
                if (result.data) {
                    _isUsernameAvailable.value = true
                    _usernameError.value = null
                } else {
                    _isUsernameAvailable.value = false
                    _usernameError.value = "Username is already taken"
                }
            }

            is Resource.Error -> {
                Logger.e(TAG, "Error checking username availability: ${result.message}")
                _usernameError.value = "Couldn't check username availability"
                _isUsernameAvailable.value = null
            }

            is Resource.Loading, Resource.NotLoading -> {}
        }

        _isCheckingUsername.value = false
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _isSubmitting.value = true

            val result = userProfileRepository.updateMyProfile(
                displayName = _displayName.value.trim(),
                username = _username.value.trim(),
                bio = _bio.value.trim().ifBlank { null },
                primaryColor = _primaryColor.value,
                secondaryColor = _secondaryColor.value
            )

            _completionResult.value = result

            when (result) {
                is Resource.Success -> {
                    Logger.d(TAG, "Onboarding completed successfully")
                    // Refresh auth state to update user with new profile data
                    authRepository.refreshAuthState()
                }

                is Resource.Error -> {
                    Logger.e(TAG, "Error completing onboarding: ${result.message}")
                }

                is Resource.Loading, Resource.NotLoading -> {}
            }

            _isSubmitting.value = false
        }
    }

    fun resetCompletionResult() {
        _completionResult.value = null
    }
}
