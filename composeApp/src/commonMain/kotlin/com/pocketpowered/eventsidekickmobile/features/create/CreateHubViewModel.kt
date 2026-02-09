package com.district37.toastmasters.features.create

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.models.CreateHub
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Create Hub screen
 * Manages state for user's editable entities (owned + collaborated events, venues, performers)
 */
class CreateHubViewModel(
    private val createHubRepository: CreateHubRepository
) : LoggingViewModel() {

    private val _createHubData = MutableStateFlow<Resource<CreateHub>>(Resource.Loading)
    val createHubData: StateFlow<Resource<CreateHub>> = _createHubData.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadCreateHub()
    }

    private fun loadCreateHub() {
        viewModelScope.launch {
            _createHubData.update { Resource.Loading }
            val result = createHubRepository.getCreateHub()
            _createHubData.update { result }
        }
    }

    /**
     * Refresh the Create Hub data
     * Used for pull-to-refresh functionality
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.update { true }
            val result = createHubRepository.getCreateHub()
            _createHubData.update { result }
            _isRefreshing.update { false }
        }
    }
}
