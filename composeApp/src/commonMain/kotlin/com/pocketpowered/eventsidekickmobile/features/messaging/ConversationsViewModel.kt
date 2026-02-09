package com.district37.toastmasters.features.messaging

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.messaging.UnreadMessagesManager
import com.district37.toastmasters.models.Conversation
import com.district37.toastmasters.util.AppConstants
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the conversations list screen.
 * Manages conversation list loading, pagination, and refresh.
 */
class ConversationsViewModel(
    private val messagingRepository: MessagingRepository,
    private val authRepository: AuthRepository,
    private val unreadMessagesManager: UnreadMessagesManager
) : LoggingViewModel() {

    private val _conversations = MutableStateFlow<Resource<List<Conversation>>>(Resource.Loading)
    val conversations: StateFlow<Resource<List<Conversation>>> = _conversations.asStateFlow()

    private val _hasMoreConversations = MutableStateFlow(false)
    val hasMoreConversations: StateFlow<Boolean> = _hasMoreConversations.asStateFlow()

    private val _endCursor = MutableStateFlow<String?>(null)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentUserId: String?
        get() = (authRepository.authState.value as? AuthState.Authenticated)?.user?.id

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            _conversations.update { Resource.Loading }
            val result = messagingRepository.getMyConversations(
                first = AppConstants.Pagination.DEFAULT_PAGE_SIZE
            )
            _conversations.update { result.map { it.conversations } }

            if (result is Resource.Success) {
                _hasMoreConversations.update { result.data.hasNextPage }
                _endCursor.update { result.data.endCursor }
                // Update global unread state
                unreadMessagesManager.updateFromConversations(result.data.conversations)
            }
        }
    }

    fun loadMoreConversations() {
        val cursor = _endCursor.value ?: return
        if (_isLoadingMore.value || !_hasMoreConversations.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingMore.update { true }
            val result = messagingRepository.getMyConversations(
                first = AppConstants.Pagination.DEFAULT_PAGE_SIZE,
                after = cursor
            )

            if (result is Resource.Success) {
                val currentConversations = (_conversations.value as? Resource.Success)?.data ?: emptyList()
                _conversations.update { Resource.Success(currentConversations + result.data.conversations) }
                _hasMoreConversations.update { result.data.hasNextPage }
                _endCursor.update { result.data.endCursor }
            }
            _isLoadingMore.update { false }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.update { true }
            val result = messagingRepository.getMyConversations(
                first = AppConstants.Pagination.DEFAULT_PAGE_SIZE
            )
            _conversations.update { result.map { it.conversations } }

            if (result is Resource.Success) {
                _hasMoreConversations.update { result.data.hasNextPage }
                _endCursor.update { result.data.endCursor }
                // Update global unread state
                unreadMessagesManager.updateFromConversations(result.data.conversations)
            }
            _isRefreshing.update { false }
        }
    }

    /**
     * Create or get existing direct conversation with another user
     */
    fun startDirectConversation(otherUserId: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = messagingRepository.getOrCreateDirectConversation(otherUserId)
            if (result is Resource.Success) {
                onSuccess(result.data.id)
            }
        }
    }
}
