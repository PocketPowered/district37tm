package com.district37.toastmasters.features.messaging

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.data.repository.SearchRepository
import com.district37.toastmasters.models.Conversation
import com.district37.toastmasters.models.OmnisearchResultItem
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the new conversation screen.
 * Shows friends as suggestions and allows searching for any user.
 */
class NewConversationViewModel(
    private val friendsRepository: FriendsRepository,
    private val searchRepository: SearchRepository,
    private val messagingRepository: MessagingRepository,
    private val authRepository: AuthRepository
) : LoggingViewModel() {

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Friends (suggestions when no search query)
    private val _friends = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val friends: StateFlow<Resource<List<User>>> = _friends.asStateFlow()

    // Search results (when searching)
    private val _searchResults = MutableStateFlow<Resource<List<User>>>(Resource.NotLoading)
    val searchResults: StateFlow<Resource<List<User>>> = _searchResults.asStateFlow()

    // Conversation creation state
    private val _creatingConversation = MutableStateFlow(false)
    val creatingConversation: StateFlow<Boolean> = _creatingConversation.asStateFlow()

    private val _createdConversation = MutableStateFlow<Conversation?>(null)
    val createdConversation: StateFlow<Conversation?> = _createdConversation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Current user ID for filtering
    val currentUserId: String?
        get() = (authRepository.authState.value as? AuthState.Authenticated)?.user?.id

    private var searchJob: Job? = null

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            _friends.value = Resource.Loading
            when (val result = friendsRepository.getMyFriends()) {
                is Resource.Success -> _friends.value = Resource.Success(result.data.friends)
                is Resource.Error -> _friends.value = Resource.Error(result.errorType, result.message)
                is Resource.Loading -> _friends.value = Resource.Loading
                is Resource.NotLoading -> _friends.value = Resource.NotLoading
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _error.value = null

        // Cancel any pending search
        searchJob?.cancel()

        if (query.isBlank()) {
            // Clear search results when query is empty
            _searchResults.value = Resource.NotLoading
            return
        }

        // Debounce search
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(300) // Wait 300ms before searching
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        _searchResults.value = Resource.Loading

        val result = searchRepository.omnisearch(query = query, limit = 20)

        _searchResults.value = when (result) {
            is Resource.Success -> {
                // Filter to only user results and exclude current user
                val users = result.data.results
                    .filterIsInstance<OmnisearchResultItem.UserResult>()
                    .map { it.user }
                    .filter { it.id != currentUserId }
                Resource.Success(users)
            }
            is Resource.Error -> Resource.Error(result.errorType, result.message)
            is Resource.Loading -> Resource.Loading
            is Resource.NotLoading -> Resource.NotLoading
        }
    }

    fun startConversation(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _creatingConversation.value = true
            _error.value = null

            when (val result = messagingRepository.getOrCreateDirectConversation(user.id)) {
                is Resource.Success -> {
                    _createdConversation.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Failed to start conversation"
                }
                else -> {}
            }

            _creatingConversation.value = false
        }
    }

    fun clearCreatedConversation() {
        _createdConversation.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
