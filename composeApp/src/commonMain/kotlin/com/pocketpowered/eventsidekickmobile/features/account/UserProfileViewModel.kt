package com.district37.toastmasters.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.ActivityFeedRepository
import com.district37.toastmasters.data.repository.CollaboratorsRepository
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.models.ActivityFeedConnection
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.models.CollaborationRequestConnection
import com.district37.toastmasters.models.FriendRequestConnection
import com.district37.toastmasters.models.MyProfile
import com.district37.toastmasters.models.UserEventsConnection
import com.district37.toastmasters.util.DisplayFormatters
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.features.account.components.ProfileTab
import com.district37.toastmasters.navigation.ProfileTabNavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the User Profile screen.
 * Uses a single unified API call to fetch all profile data (subscribed events, attending events,
 * activity feed, and friend requests).
 * Maintains backward compatibility with existing UI through derived state flows.
 */
class UserProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val friendsRepository: FriendsRepository,
    private val activityFeedRepository: ActivityFeedRepository,
    private val collaboratorsRepository: CollaboratorsRepository,
    private val profileTabNavigationState: ProfileTabNavigationState
) : ViewModel() {

    private val tag = "UserProfileViewModel"

    // Unified profile data from single API call
    private val _profileData = MutableStateFlow<Resource<MyProfile>>(Resource.NotLoading)
    val profileData: StateFlow<Resource<MyProfile>> = _profileData.asStateFlow()

    // Derived state flows for UI compatibility
    private val _friendRequests = MutableStateFlow<Resource<FriendRequestConnection>>(Resource.NotLoading)
    val friendRequests: StateFlow<Resource<FriendRequestConnection>> = _friendRequests.asStateFlow()

    // Collaboration requests (loaded separately)
    private val _collaborationRequests = MutableStateFlow<Resource<CollaborationRequestConnection>>(Resource.NotLoading)
    val collaborationRequests: StateFlow<Resource<CollaborationRequestConnection>> = _collaborationRequests.asStateFlow()

    // Track which collaboration requests are currently being processed
    private val _processingCollaborationRequests = MutableStateFlow<Set<Int>>(emptySet())
    val processingCollaborationRequests: StateFlow<Set<Int>> = _processingCollaborationRequests.asStateFlow()

    private val _subscribedEvents = MutableStateFlow<Resource<UserEventsConnection>>(Resource.NotLoading)
    val subscribedEvents: StateFlow<Resource<UserEventsConnection>> = _subscribedEvents.asStateFlow()

    private val _attendingEvents = MutableStateFlow<Resource<UserEventsConnection>>(Resource.NotLoading)
    val attendingEvents: StateFlow<Resource<UserEventsConnection>> = _attendingEvents.asStateFlow()

    private val _activityFeedConnection = MutableStateFlow<Resource<ActivityFeedConnection>>(Resource.NotLoading)

    // Expose as List<ActivityFeedItem> for compatibility with activityFeedSection
    val activityFeed: StateFlow<Resource<List<ActivityFeedItem>>> = _activityFeedConnection.map { resource ->
        resource.map { connection -> connection.items }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.NotLoading)

    private val _isLoadingMoreActivity = MutableStateFlow(false)
    val isLoadingMoreActivity: StateFlow<Boolean> = _isLoadingMoreActivity.asStateFlow()

    private var activityEndCursor: String? = null

    // Derived states (issues #15, #16)
    val memberSinceFormatted: StateFlow<String> = profileData.map { resource ->
        (resource as? Resource.Success)?.data?.user?.createdAt?.let { instant ->
            DisplayFormatters.formatMemberSince(instant)
        } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userInitials: StateFlow<String> = profileData.map { resource ->
        (resource as? Resource.Success)?.data?.user?.displayName?.let { displayName ->
            DisplayFormatters.formatUserInitials(displayName)
        } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Tab state for profile content navigation
    private val _selectedTab = MutableStateFlow(ProfileTab.EVENTS)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    // Badge count for requests tab (friend + collaboration requests)
    val requestsBadgeCount: StateFlow<Int> = combine(
        friendRequests,
        collaborationRequests
    ) { friends, collabs ->
        val friendCount = (friends as? Resource.Success)?.data?.requests?.size ?: 0
        val collabCount = (collabs as? Resource.Success)?.data?.requests?.size ?: 0
        friendCount + collabCount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectTab(tab: ProfileTab) {
        _selectedTab.update { tab }
    }

    companion object {
        private const val CAROUSEL_PAGE_SIZE = 10
    }

    init {
        // Check for pending tab navigation (e.g., from deep link)
        profileTabNavigationState.consumePendingTab()?.let { pendingTab ->
            _selectedTab.value = pendingTab
        }

        loadProfile()
        loadCollaborationRequests()
    }

    /**
     * Load unified profile data with a single API call
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profileData.update { Resource.Loading }
            _friendRequests.update { Resource.Loading }
            _subscribedEvents.update { Resource.Loading }
            _attendingEvents.update { Resource.Loading }
            _activityFeedConnection.update { Resource.Loading }

            activityEndCursor = null

            val result = userProfileRepository.getMyProfile(
                subscribedEventsFirst = CAROUSEL_PAGE_SIZE,
                attendingEventsFirst = CAROUSEL_PAGE_SIZE,
                activityFeedFirst = CAROUSEL_PAGE_SIZE,
                friendRequestsFirst = CAROUSEL_PAGE_SIZE
            )

            _profileData.update { result }

            when (result) {
                is Resource.Success -> {
                    val profile = result.data
                    _friendRequests.update { Resource.Success(profile.incomingFriendRequests) }
                    _subscribedEvents.update { Resource.Success(profile.subscribedEvents) }
                    _attendingEvents.update { Resource.Success(profile.attendingEvents) }
                    _activityFeedConnection.update { Resource.Success(profile.activityFeed) }
                    activityEndCursor = profile.activityFeed.endCursor
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to load profile: ${result.message}")
                    _friendRequests.update { Resource.Error(result.errorType, result.message) }
                    _subscribedEvents.update { Resource.Error(result.errorType, result.message) }
                    _attendingEvents.update { Resource.Error(result.errorType, result.message) }
                    _activityFeedConnection.update { Resource.Error(result.errorType, result.message) }
                }
                else -> {}
            }
        }
    }

    /**
     * Load more activity feed items (pagination).
     * Uses separate API call since we need cursor-based pagination.
     */
    fun loadMoreActivity() {
        val currentFeed = (_activityFeedConnection.value as? Resource.Success)?.data ?: return
        if (!currentFeed.hasNextPage || _isLoadingMoreActivity.value) return

        viewModelScope.launch {
            _isLoadingMoreActivity.update { true }
            val result = activityFeedRepository.getMyActivityFeed(after = activityEndCursor)

            if (result is Resource.Success) {
                activityEndCursor = result.data.endCursor
                val updatedFeed = currentFeed.copy(
                    items = currentFeed.items + result.data.items,
                    hasNextPage = result.data.hasNextPage,
                    endCursor = result.data.endCursor
                )
                _activityFeedConnection.update { Resource.Success(updatedFeed) }
            }
            _isLoadingMoreActivity.update { false }
        }
    }

    /**
     * Accept a friend request with optimistic UI update
     */
    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            // Save current state for potential rollback
            val previousState = _friendRequests.value

            // Optimistically remove from UI immediately
            removeFriendRequestFromUI(requestId)

            // Make API call
            val result = friendsRepository.acceptFriendRequest(requestId)
            if (result is Resource.Error) {
                // Rollback on failure
                Logger.e(tag, "Failed to accept friend request: ${result.message}")
                _friendRequests.update { previousState }
            }
        }
    }

    /**
     * Reject a friend request with optimistic UI update
     */
    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            // Save current state for potential rollback
            val previousState = _friendRequests.value

            // Optimistically remove from UI immediately
            removeFriendRequestFromUI(requestId)

            // Make API call
            val result = friendsRepository.rejectFriendRequest(requestId)
            if (result is Resource.Error) {
                // Rollback on failure
                Logger.e(tag, "Failed to reject friend request: ${result.message}")
                _friendRequests.update { previousState }
            }
        }
    }

    /**
     * Remove a friend request from UI (used for optimistic updates)
     */
    private fun removeFriendRequestFromUI(requestId: Int) {
        val current = (_friendRequests.value as? Resource.Success)?.data ?: return
        val updated = current.copy(
            requests = current.requests.filter { it.id != requestId },
            totalCount = current.totalCount - 1
        )
        _friendRequests.update { Resource.Success(updated) }
    }

    // ========== Collaboration Requests ==========

    /**
     * Load collaboration requests separately from main profile
     */
    private fun loadCollaborationRequests() {
        viewModelScope.launch {
            _collaborationRequests.update { Resource.Loading }
            val result = collaboratorsRepository.getMyIncomingCollaborationRequests()
            _collaborationRequests.update { result }
            if (result is Resource.Error) {
                Logger.e(tag, "Failed to load collaboration requests: ${result.message}")
            }
        }
    }

    /**
     * Accept a collaboration request
     */
    fun acceptCollaborationRequest(requestId: Int) {
        viewModelScope.launch {
            _processingCollaborationRequests.update { it + requestId }
            val result = collaboratorsRepository.acceptCollaborationRequest(requestId)
            if (result is Resource.Success) {
                updateCollaborationRequestsAfterAction(requestId)
            } else if (result is Resource.Error) {
                Logger.e(tag, "Failed to accept collaboration request: ${result.message}")
            }
            _processingCollaborationRequests.update { it - requestId }
        }
    }

    /**
     * Reject a collaboration request
     */
    fun rejectCollaborationRequest(requestId: Int) {
        viewModelScope.launch {
            _processingCollaborationRequests.update { it + requestId }
            val result = collaboratorsRepository.rejectCollaborationRequest(requestId)
            if (result is Resource.Success) {
                updateCollaborationRequestsAfterAction(requestId)
            } else if (result is Resource.Error) {
                Logger.e(tag, "Failed to reject collaboration request: ${result.message}")
            }
            _processingCollaborationRequests.update { it - requestId }
        }
    }

    /**
     * Optimistically update collaboration requests after accept/reject
     */
    private fun updateCollaborationRequestsAfterAction(requestId: Int) {
        val current = (_collaborationRequests.value as? Resource.Success)?.data ?: return
        val updated = current.copy(
            requests = current.requests.filter { it.id != requestId },
            totalCount = current.totalCount - 1
        )
        _collaborationRequests.update { Resource.Success(updated) }
    }

    /**
     * Refresh all profile data
     */
    fun refresh() {
        loadProfile()
        loadCollaborationRequests()
    }
}
