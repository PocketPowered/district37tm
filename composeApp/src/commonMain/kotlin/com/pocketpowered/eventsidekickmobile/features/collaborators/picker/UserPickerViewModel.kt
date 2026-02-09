package com.district37.toastmasters.features.collaborators.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.CollaboratorsRepository
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.data.repository.SearchRepository
import com.district37.toastmasters.models.OmnisearchResultItem
import com.district37.toastmasters.models.PermissionLevel
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * ViewModel for selecting a user to invite as a collaborator
 */
@OptIn(FlowPreview::class)
class UserPickerViewModel(
    private val searchRepository: SearchRepository,
    private val collaboratorsRepository: CollaboratorsRepository,
    private val friendsRepository: FriendsRepository,
    private val authRepository: AuthRepository,
    private val entityType: String,
    private val entityId: Int
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingFriends = MutableStateFlow(false)
    val isLoadingFriends: StateFlow<Boolean> = _isLoadingFriends.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendResult = MutableStateFlow<SendResult?>(null)
    val sendResult: StateFlow<SendResult?> = _sendResult.asStateFlow()

    // Permission level selection
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    init {
        // Load friends on initialization
        viewModelScope.launch {
            loadFriends()
        }

        // Set up search query listener
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 2) {
                        search(query)
                    } else {
                        _users.value = emptyList()
                    }
                }
        }
    }

    private suspend fun loadFriends() {
        _isLoadingFriends.value = true

        when (val result = friendsRepository.getMyFriends(first = 50)) {
            is Resource.Success -> {
                _friends.value = result.data.friends
            }
            is Resource.Error -> {
                // Silently fail - friends are optional
                _friends.value = emptyList()
            }
            is Resource.Loading, Resource.NotLoading -> {}
        }

        _isLoadingFriends.value = false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private suspend fun search(query: String) {
        _isLoading.value = true
        _error.value = null

        when (val result = searchRepository.omnisearch(query, limit = 30)) {
            is Resource.Success -> {
                // Filter to only users from omnisearch results
                val userResults = result.data.results.filterIsInstance<OmnisearchResultItem.UserResult>()
                // Exclude current user from results
                val currentUserId = (authRepository.authState.value as? AuthState.Authenticated)?.user?.id
                _users.value = userResults
                    .map { it.user }
                    .filter { it.id != currentUserId }
            }
            is Resource.Error -> {
                _error.value = result.message ?: "Failed to search users"
            }
            is Resource.Loading, Resource.NotLoading -> {}
        }

        _isLoading.value = false
    }

    /**
     * Called when a user is selected - shows permission level dialog
     */
    fun onUserSelected(user: User) {
        _selectedUser.value = user
        _showPermissionDialog.value = true
    }

    /**
     * Dismiss the permission dialog
     */
    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
        _selectedUser.value = null
    }

    /**
     * Send collaboration request with the selected permission level
     */
    fun sendCollaborationRequest(permissionLevel: PermissionLevel) {
        val user = _selectedUser.value ?: return

        viewModelScope.launch {
            _isSending.value = true
            _showPermissionDialog.value = false

            when (val result = collaboratorsRepository.sendCollaborationRequest(
                receiverId = user.id,
                entityType = entityType,
                entityId = entityId,
                permissionLevel = permissionLevel.value
            )) {
                is Resource.Success -> {
                    _sendResult.value = SendResult.Success(user.displayName ?: user.username ?: "User")
                }
                is Resource.Error -> {
                    _sendResult.value = SendResult.Error(result.message ?: "Failed to send request")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _isSending.value = false
            _selectedUser.value = null
        }
    }

    /**
     * Clear the send result
     */
    fun clearSendResult() {
        _sendResult.value = null
    }

    sealed class SendResult {
        data class Success(val userName: String) : SendResult()
        data class Error(val message: String) : SendResult()
    }
}
