package com.district37.toastmasters.features.users

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.models.UserProfile
import com.district37.toastmasters.models.UserRelationshipStatus
import com.district37.toastmasters.features.users.components.OtherUserProfileTab
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for viewing another user's profile.
 *
 * Uses server-side unified UserProfile approach:
 * - Single API call to getUserProfile() loads everything
 * - Server handles all permission checking
 * - Server conditionally populates attending events and activity feed based on friendship
 * - Client just renders what it receives
 *
 * This simplifies the client by removing:
 * - Separate API calls for relationship status, attending events, activity feed
 * - Client-side permission checking
 * - Multiple state flows
 */
class ViewOtherUserProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val friendsRepository: FriendsRepository,
    private val userId: String
) : LoggingViewModel() {

    private val _profile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val profile: StateFlow<Resource<UserProfile>> = _profile.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    // Tab state for profile content navigation
    private val _selectedTab = MutableStateFlow(OtherUserProfileTab.EVENTS)
    val selectedTab: StateFlow<OtherUserProfileTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: OtherUserProfileTab) {
        _selectedTab.update { tab }
    }

    init {
        loadProfile()
    }

    /**
     * Load the complete user profile (includes user, relationship status, attending events, activity feed).
     * Server handles all permission checking and conditionally populates data based on friendship.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profile.update { Resource.Loading }
            val result = userProfileRepository.getUserProfile(userId)
            _profile.update { result }
        }
    }

    /**
     * Refresh the profile data
     */
    fun refresh() {
        loadProfile()
    }

    // ========== Friend Actions ==========
    // These actions modify the friendship relationship and trigger a profile reload

    fun sendFriendRequest() {
        viewModelScope.launch {
            _actionInProgress.update { true }
            val result = friendsRepository.sendFriendRequest(userId)
            if (result is Resource.Success) {
                // Reload profile to reflect the new pending request
                loadProfile()
            }
            _actionInProgress.update { false }
        }
    }

    fun cancelFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _actionInProgress.update { true }
            val result = friendsRepository.cancelFriendRequest(requestId)
            if (result is Resource.Success) {
                loadProfile()
            }
            _actionInProgress.update { false }
        }
    }

    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _actionInProgress.update { true }
            val result = friendsRepository.acceptFriendRequest(requestId)
            if (result is Resource.Success) {
                // Reload profile - now they're friends, so attending events and activity feed will be populated
                loadProfile()
            }
            _actionInProgress.update { false }
        }
    }

    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _actionInProgress.update { true }
            val result = friendsRepository.rejectFriendRequest(requestId)
            if (result is Resource.Success) {
                loadProfile()
            }
            _actionInProgress.update { false }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            _actionInProgress.update { true }
            val result = friendsRepository.removeFriend(userId)
            if (result is Resource.Success) {
                // Reload profile - no longer friends, so attending events and activity feed will be null
                loadProfile()
            }
            _actionInProgress.update { false }
        }
    }
}
