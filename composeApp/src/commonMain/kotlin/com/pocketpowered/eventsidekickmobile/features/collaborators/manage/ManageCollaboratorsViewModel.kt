package com.district37.toastmasters.features.collaborators.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.CollaboratorsRepository
import com.district37.toastmasters.models.CollaborationRequest
import com.district37.toastmasters.models.Collaborator
import com.district37.toastmasters.models.PermissionLevel
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing collaborators on an entity
 */
class ManageCollaboratorsViewModel(
    private val collaboratorsRepository: CollaboratorsRepository,
    private val entityType: String,
    private val entityId: Int
) : ViewModel() {

    private val _collaborators = MutableStateFlow<List<Collaborator>>(emptyList())
    val collaborators: StateFlow<List<Collaborator>> = _collaborators.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _myPermission = MutableStateFlow<Collaborator?>(null)
    val myPermission: StateFlow<Collaborator?> = _myPermission.asStateFlow()

    // Pending outgoing requests
    private val _pendingRequests = MutableStateFlow<List<CollaborationRequest>>(emptyList())
    val pendingRequests: StateFlow<List<CollaborationRequest>> = _pendingRequests.asStateFlow()

    private val _pendingRequestsLoading = MutableStateFlow(false)
    val pendingRequestsLoading: StateFlow<Boolean> = _pendingRequestsLoading.asStateFlow()

    // Action states
    private val _actionInProgress = MutableStateFlow<ActionState?>(null)
    val actionInProgress: StateFlow<ActionState?> = _actionInProgress.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    // Dialog states
    private val _showPermissionDialog = MutableStateFlow<Collaborator?>(null)
    val showPermissionDialog: StateFlow<Collaborator?> = _showPermissionDialog.asStateFlow()

    private val _showTransferOwnershipDialog = MutableStateFlow<Collaborator?>(null)
    val showTransferOwnershipDialog: StateFlow<Collaborator?> = _showTransferOwnershipDialog.asStateFlow()

    private val _showRemoveDialog = MutableStateFlow<Collaborator?>(null)
    val showRemoveDialog: StateFlow<Collaborator?> = _showRemoveDialog.asStateFlow()

    private val _showCancelRequestDialog = MutableStateFlow<CollaborationRequest?>(null)
    val showCancelRequestDialog: StateFlow<CollaborationRequest?> = _showCancelRequestDialog.asStateFlow()

    private var currentCursor: String? = null

    init {
        loadCollaborators()
        loadMyPermission()
        loadPendingRequests()
    }

    private fun loadMyPermission() {
        viewModelScope.launch {
            when (val result = collaboratorsRepository.getMyPermissionForEntity(entityType, entityId)) {
                is Resource.Success -> {
                    _myPermission.value = result.data
                }
                is Resource.Error -> {
                    // Non-critical - just means user can't manage
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }
        }
    }

    fun loadCollaborators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            currentCursor = null

            when (val result = collaboratorsRepository.getCollaboratorsForEntity(
                entityType = entityType,
                entityId = entityId
            )) {
                is Resource.Success -> {
                    _collaborators.value = result.data.collaborators
                    _hasMore.value = result.data.hasNextPage
                    currentCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Failed to load collaborators"
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = collaboratorsRepository.getCollaboratorsForEntity(
                entityType = entityType,
                entityId = entityId,
                after = currentCursor
            )) {
                is Resource.Success -> {
                    _collaborators.value = _collaborators.value + result.data.collaborators
                    _hasMore.value = result.data.hasNextPage
                    currentCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _isLoading.value = false
        }
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            _pendingRequestsLoading.value = true

            when (val result = collaboratorsRepository.getMyOutgoingCollaborationRequestsForEntity(
                entityType = entityType,
                entityId = entityId
            )) {
                is Resource.Success -> {
                    _pendingRequests.value = result.data
                }
                is Resource.Error -> {
                    // Non-critical - don't show error, just empty list
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _pendingRequestsLoading.value = false
        }
    }

    fun refreshPendingRequests() {
        loadPendingRequests()
    }

    /**
     * Refresh all data on the screen
     */
    fun refresh() {
        loadCollaborators()
        loadMyPermission()
        loadPendingRequests()
    }

    // Dialog actions
    fun showChangePermissionDialog(collaborator: Collaborator) {
        _showPermissionDialog.value = collaborator
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = null
    }

    fun showTransferOwnershipDialog(collaborator: Collaborator) {
        _showTransferOwnershipDialog.value = collaborator
    }

    fun dismissTransferOwnershipDialog() {
        _showTransferOwnershipDialog.value = null
    }

    fun showRemoveDialog(collaborator: Collaborator) {
        _showRemoveDialog.value = collaborator
    }

    fun dismissRemoveDialog() {
        _showRemoveDialog.value = null
    }

    fun showCancelRequestDialog(request: CollaborationRequest) {
        _showCancelRequestDialog.value = request
    }

    fun dismissCancelRequestDialog() {
        _showCancelRequestDialog.value = null
    }

    // Actions
    fun updatePermission(collaboratorId: String, newLevel: PermissionLevel) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.UpdatingPermission
            dismissPermissionDialog()

            when (val result = collaboratorsRepository.updateCollaboratorPermission(
                collaboratorId = collaboratorId,
                newPermissionLevel = newLevel.value
            )) {
                is Resource.Success -> {
                    _actionResult.value = ActionResult.Success("Permission updated")
                    // Update local state
                    _collaborators.value = _collaborators.value.map { collab ->
                        if (collab.id == collaboratorId) result.data else collab
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to update permission")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _actionInProgress.value = null
        }
    }

    fun removeCollaborator(collaboratorId: String) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.Removing
            dismissRemoveDialog()

            when (val result = collaboratorsRepository.removeCollaborator(collaboratorId)) {
                is Resource.Success -> {
                    if (result.data) {
                        _actionResult.value = ActionResult.Success("Collaborator removed")
                        // Update local state
                        _collaborators.value = _collaborators.value.filter { it.id != collaboratorId }
                    } else {
                        _actionResult.value = ActionResult.Error("Failed to remove collaborator")
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to remove collaborator")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _actionInProgress.value = null
        }
    }

    fun transferOwnership(newOwnerId: String) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.TransferringOwnership
            dismissTransferOwnershipDialog()

            when (val result = collaboratorsRepository.transferOwnership(
                entityType = entityType,
                entityId = entityId,
                newOwnerId = newOwnerId
            )) {
                is Resource.Success -> {
                    if (result.data) {
                        _actionResult.value = ActionResult.Success("Ownership transferred")
                        // Reload to reflect changes
                        loadCollaborators()
                        loadMyPermission()
                    } else {
                        _actionResult.value = ActionResult.Error("Failed to transfer ownership")
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to transfer ownership")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _actionInProgress.value = null
        }
    }

    fun cancelPendingRequest(requestId: Int) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.CancellingRequest
            dismissCancelRequestDialog()

            when (val result = collaboratorsRepository.cancelCollaborationRequest(requestId)) {
                is Resource.Success -> {
                    if (result.data) {
                        _actionResult.value = ActionResult.Success("Request cancelled")
                        // Update local state
                        _pendingRequests.value = _pendingRequests.value.filter { it.id != requestId }
                    } else {
                        _actionResult.value = ActionResult.Error("Failed to cancel request")
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to cancel request")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _actionInProgress.value = null
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }

    sealed class ActionState {
        data object UpdatingPermission : ActionState()
        data object Removing : ActionState()
        data object TransferringOwnership : ActionState()
        data object CancellingRequest : ActionState()
    }

    sealed class ActionResult {
        data class Success(val message: String) : ActionResult()
        data class Error(val message: String) : ActionResult()
    }
}
