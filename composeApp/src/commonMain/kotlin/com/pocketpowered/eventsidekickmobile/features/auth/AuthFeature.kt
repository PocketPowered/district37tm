package com.district37.toastmasters.features.auth

import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Composable feature for authentication state and login prompts.
 * Can be instantiated by any ViewModel that needs auth functionality.
 *
 * Usage:
 * ```kotlin
 * class MyViewModel(...) {
 *     val authFeature = AuthFeature(authRepository, viewModelScope)
 *
 *     // Expose to UI
 *     val showLoginPrompt = authFeature.showLoginPrompt
 *     val isAuthenticated get() = authFeature.isAuthenticated
 * }
 * ```
 */
class AuthFeature(
    private val authRepository: AuthRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _showLoginPrompt = MutableStateFlow(false)
    val showLoginPrompt: StateFlow<Boolean> = _showLoginPrompt.asStateFlow()

    /**
     * Whether user is currently authenticated
     */
    val isAuthenticated: Boolean
        get() = authRepository.authState.value is AuthState.Authenticated

    /**
     * Request login - triggers the login prompt.
     * Called by other features when auth is required.
     */
    fun requestLogin() {
        _showLoginPrompt.update { true }
    }

    /**
     * Dismiss the login prompt dialog
     */
    fun dismissLoginPrompt() {
        _showLoginPrompt.update { false }
    }
}
