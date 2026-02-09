package com.district37.toastmasters.navigation

import com.district37.toastmasters.features.account.components.ProfileTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state for navigating to a specific tab on the profile screen.
 * Used when deep linking to the profile with a specific tab selected (e.g., Requests tab).
 */
class ProfileTabNavigationState {
    private val _pendingTab = MutableStateFlow<ProfileTab?>(null)
    val pendingTab: StateFlow<ProfileTab?> = _pendingTab.asStateFlow()

    /**
     * Set the tab that should be selected when the profile screen is displayed.
     * Call this before navigating to the profile.
     */
    fun setPendingTab(tab: ProfileTab) {
        _pendingTab.value = tab
    }

    /**
     * Clear the pending tab after it has been consumed.
     * Should be called by the profile screen after applying the tab selection.
     */
    fun clearPendingTab() {
        _pendingTab.value = null
    }

    /**
     * Consume and return the pending tab if one is set.
     * This atomically clears the pending state.
     */
    fun consumePendingTab(): ProfileTab? {
        val tab = _pendingTab.value
        _pendingTab.value = null
        return tab
    }
}
