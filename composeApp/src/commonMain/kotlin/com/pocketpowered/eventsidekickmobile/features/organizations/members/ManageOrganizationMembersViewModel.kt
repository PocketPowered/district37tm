package com.district37.toastmasters.features.organizations.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.graphql.type.UpdateOrganizationMemberInput
import com.district37.toastmasters.graphql.type.OrganizationRole as GraphQLOrganizationRole
import com.district37.toastmasters.models.OrganizationMember
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing members of an organization
 */
class ManageOrganizationMembersViewModel(
    private val organizationRepository: OrganizationRepository,
    private val organizationId: Int
) : ViewModel() {

    private val tag = "ManageOrganizationMembersViewModel"

    // Members state
    private val _members = MutableStateFlow<List<OrganizationMember>>(emptyList())
    val members: StateFlow<List<OrganizationMember>> = _members.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Current user's role in the organization
    private val _myRole = MutableStateFlow<OrganizationRole?>(null)
    val myRole: StateFlow<OrganizationRole?> = _myRole.asStateFlow()

    // Action states
    private val _actionInProgress = MutableStateFlow<ActionState?>(null)
    val actionInProgress: StateFlow<ActionState?> = _actionInProgress.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    // Dialog states
    private val _showRoleDialog = MutableStateFlow<OrganizationMember?>(null)
    val showRoleDialog: StateFlow<OrganizationMember?> = _showRoleDialog.asStateFlow()

    private val _showRemoveDialog = MutableStateFlow<OrganizationMember?>(null)
    val showRemoveDialog: StateFlow<OrganizationMember?> = _showRemoveDialog.asStateFlow()

    init {
        loadMembers()
        loadMyRole()
    }

    private fun loadMyRole() {
        viewModelScope.launch {
            when (val result = organizationRepository.getMyRoleInOrganization(organizationId)) {
                is Resource.Success -> {
                    _myRole.value = result.data
                }
                is Resource.Error -> {
                    // Non-critical - just means user can't manage
                    Logger.e(tag, "Failed to load my role: ${result.message}")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }
        }
    }

    fun loadMembers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = organizationRepository.getOrganization(organizationId)) {
                is Resource.Success -> {
                    _members.value = result.data.members.items
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Failed to load members"
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Refresh all data on the screen
     */
    fun refresh() {
        loadMembers()
        loadMyRole()
    }

    /**
     * Check if current user can manage members (is admin or owner)
     */
    fun canManageMembers(): Boolean {
        return _myRole.value?.canManageMembers() == true
    }

    /**
     * Check if current user can change a member's role
     */
    fun canChangeRole(member: OrganizationMember): Boolean {
        val myRole = _myRole.value ?: return false
        // Can't change own role
        // Owners can change anyone's role (except other owners to owner)
        // Admins can change members' roles (but not other admins or owners)
        return myRole.canManageMembers() && member.role != OrganizationRole.OWNER
    }

    /**
     * Check if current user can remove a member
     */
    fun canRemoveMember(member: OrganizationMember): Boolean {
        val myRole = _myRole.value ?: return false
        // Can't remove yourself (must transfer ownership first if owner)
        // Owners can remove anyone except other owners
        // Admins can remove members only
        return myRole.canManageMembers() &&
               member.role != OrganizationRole.OWNER &&
               (myRole == OrganizationRole.OWNER || member.role == OrganizationRole.MEMBER)
    }

    // Dialog actions
    fun showChangeRoleDialog(member: OrganizationMember) {
        _showRoleDialog.value = member
    }

    fun dismissRoleDialog() {
        _showRoleDialog.value = null
    }

    fun showRemoveDialog(member: OrganizationMember) {
        _showRemoveDialog.value = member
    }

    fun dismissRemoveDialog() {
        _showRemoveDialog.value = null
    }

    /**
     * Update a member's role
     */
    fun updateMemberRole(userId: String, newRole: OrganizationRole) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.UpdatingRole
            dismissRoleDialog()

            val input = UpdateOrganizationMemberInput(
                organizationId = organizationId,
                userId = userId,
                role = newRole.toGraphQL()
            )

            when (val result = organizationRepository.updateOrganizationMemberRole(input)) {
                is Resource.Success -> {
                    _actionResult.value = ActionResult.Success("Role updated")
                    // Update local state
                    _members.value = _members.value.map { member ->
                        if (member.userId == userId) result.data else member
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to update role")
                }
                is Resource.Loading, Resource.NotLoading -> {}
            }

            _actionInProgress.value = null
        }
    }

    /**
     * Remove a member from the organization
     */
    fun removeMember(userId: String) {
        viewModelScope.launch {
            _actionInProgress.value = ActionState.Removing
            dismissRemoveDialog()

            when (val result = organizationRepository.removeOrganizationMember(organizationId, userId)) {
                is Resource.Success -> {
                    if (result.data) {
                        _actionResult.value = ActionResult.Success("Member removed")
                        // Update local state
                        _members.value = _members.value.filter { it.userId != userId }
                    } else {
                        _actionResult.value = ActionResult.Error("Failed to remove member")
                    }
                }
                is Resource.Error -> {
                    _actionResult.value = ActionResult.Error(result.message ?: "Failed to remove member")
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
        data object UpdatingRole : ActionState()
        data object Removing : ActionState()
    }

    sealed class ActionResult {
        data class Success(val message: String) : ActionResult()
        data class Error(val message: String) : ActionResult()
    }
}

/**
 * Extension to convert domain role to GraphQL role
 */
private fun OrganizationRole.toGraphQL(): GraphQLOrganizationRole {
    return when (this) {
        OrganizationRole.OWNER -> GraphQLOrganizationRole.OWNER
        OrganizationRole.ADMIN -> GraphQLOrganizationRole.ADMIN
        OrganizationRole.MEMBER -> GraphQLOrganizationRole.MEMBER
    }
}
