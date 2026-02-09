package com.district37.toastmasters.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.models.User
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Friends List screen (View All)
 */
class FriendsListViewModel(
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val tag = "FriendsListViewModel"

    private val _friends = MutableStateFlow<Resource<List<User>>>(Resource.NotLoading)
    val friends: StateFlow<Resource<List<User>>> = _friends.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreFriends = MutableStateFlow(false)
    val hasMoreFriends: StateFlow<Boolean> = _hasMoreFriends.asStateFlow()

    private var endCursor: String? = null

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _friends.update { Resource.Loading }
            endCursor = null
            val result = friendsRepository.getMyFriends(first = PAGE_SIZE)

            when (result) {
                is Resource.Success -> {
                    _friends.update { Resource.Success(result.data.friends) }
                    _hasMoreFriends.update { result.data.hasNextPage }
                    endCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _friends.update { Resource.Error(result.errorType, result.message) }
                    Logger.e(tag, "Failed to load friends: ${result.message}")
                }
                else -> {
                    // Loading or NotLoading states handled elsewhere
                }
            }
        }
    }

    fun loadMore() {
        val currentFriends = (_friends.value as? Resource.Success)?.data ?: return
        if (!_hasMoreFriends.value || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.update { true }
            val result = friendsRepository.getMyFriends(
                first = PAGE_SIZE,
                after = endCursor
            )

            if (result is Resource.Success) {
                _friends.update { Resource.Success(currentFriends + result.data.friends) }
                _hasMoreFriends.update { result.data.hasNextPage }
                endCursor = result.data.endCursor
            }
            _isLoadingMore.update { false }
        }
    }

    fun refresh() {
        loadFriends()
    }
}
