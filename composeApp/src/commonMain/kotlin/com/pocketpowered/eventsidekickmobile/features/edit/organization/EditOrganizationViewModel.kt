package com.district37.toastmasters.features.edit.organization

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.UpdateOrganizationInput
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.HasPermissions
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.models.Organization
import com.district37.toastmasters.models.OrganizationRole
import com.district37.toastmasters.util.DeleteOperationFeature
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.SingleImageHandlingFeature
import com.district37.toastmasters.viewmodel.BaseEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for editing an existing organization
 *
 * Extends BaseEditViewModel to handle loading the organization data,
 * and provides form fields for editing all organization properties.
 */
class EditOrganizationViewModel(
    private val organizationRepository: OrganizationRepository,
    imageUploadRepository: ImageUploadRepository,
    imageRepository: ImageRepository,
    organizationId: Int
) : BaseEditViewModel<Organization>(organizationId, organizationRepository) {

    override val tag = "EditOrganizationViewModel"

    companion object {
        // Tag validation regex - same rules as username: 3-18 chars, lowercase letters, numbers, underscores
        private val TAG_REGEX = Regex("^[a-z0-9_]{3,18}$")
    }

    // Image handling feature for logo
    private val imageHandler = SingleImageHandlingFeature(
        imageUploadRepository = imageUploadRepository,
        imageRepository = imageRepository,
        tag = tag
    )

    // Expose image state from feature
    val imageUrl = imageHandler.imageUrl
    val selectedImageBytes = imageHandler.selectedImageBytes
    val isUploadingImage = imageHandler.isUploadingImage
    val selectedFocusRegion = imageHandler.selectedFocusRegion

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _tag = MutableStateFlow("")
    val tag_: StateFlow<String> = _tag.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _website = MutableStateFlow("")
    val website: StateFlow<String> = _website.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _primaryColor = MutableStateFlow("")
    val primaryColor: StateFlow<String> = _primaryColor.asStateFlow()

    private val _secondaryColor = MutableStateFlow("")
    val secondaryColor: StateFlow<String> = _secondaryColor.asStateFlow()

    // Delete functionality using DeleteOperationFeature
    private val deleteHandler = DeleteOperationFeature(
        tag = tag,
        scope = viewModelScope
    )
    val isDeleting = deleteHandler.isDeleting
    val deleteSuccess = deleteHandler.deleteSuccess

    // Current user's role in the organization (for permission checks)
    private val _myRole = MutableStateFlow<OrganizationRole?>(null)
    val myRole: StateFlow<OrganizationRole?> = _myRole.asStateFlow()

    // Expose canDelete permission from the loaded entity
    val canDelete: StateFlow<Boolean> = loadedEntity.map { entity ->
        (entity as? HasPermissions)?.permissions?.canDelete == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI state management
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showImageWizard = MutableStateFlow(false)
    val showImageWizard: StateFlow<Boolean> = _showImageWizard.asStateFlow()

    init {
        // Load the user's role in this organization
        loadMyRole()
    }

    private fun loadMyRole() {
        viewModelScope.launch {
            when (val result = organizationRepository.getMyRoleInOrganization(entityId)) {
                is Resource.Success -> {
                    _myRole.value = result.data
                }
                is Resource.Error -> {
                    // User might not be a member - that's fine
                    _myRole.value = null
                }
                else -> {}
            }
        }
    }

    override fun mapEntityToFields(entity: Organization) {
        _name.value = entity.name
        _tag.value = entity.tag ?: ""
        _description.value = entity.description ?: ""
        _website.value = entity.website ?: ""
        _email.value = entity.email ?: ""
        _phone.value = entity.phone ?: ""
        _primaryColor.value = entity.primaryColor ?: ""
        _secondaryColor.value = entity.secondaryColor ?: ""

        // Set existing logo image in feature
        imageHandler.setExistingImage(entity.logoUrl)
    }

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateTag(value: String) {
        // Convert to lowercase and remove invalid characters
        val sanitized = value.lowercase().filter { it.isLetterOrDigit() || it == '_' }
        _tag.update { sanitized }
        clearFieldError("tag")
    }

    fun updateDescription(value: String) {
        _description.update { value }
    }

    fun updateWebsite(value: String) {
        _website.update { value }
    }

    fun updateEmail(value: String) {
        _email.update { value }
    }

    fun updatePhone(value: String) {
        _phone.update { value }
    }

    fun updatePrimaryColor(value: String) {
        _primaryColor.update { value }
    }

    fun updateSecondaryColor(value: String) {
        _secondaryColor.update { value }
    }

    // Image update functions - delegate to feature
    fun onImageSelected(bytes: ByteArray, focusRegion: FocusRegion? = null) {
        imageHandler.onImageSelected(bytes, focusRegion)
    }

    fun onImageUrlChanged(url: String) {
        imageHandler.onImageUrlChanged(url)
    }

    fun clearImage() {
        imageHandler.clearImage()
    }

    fun setImageSelection(result: ImageSelectionResult) {
        imageHandler.setImageSelection(result)
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            setFieldError("name", "Organization name is required")
            isValid = false
        }

        if (_tag.value.isBlank()) {
            setFieldError("tag", "Organization tag is required")
            isValid = false
        } else if (!TAG_REGEX.matches(_tag.value)) {
            setFieldError("tag", "Tag must be 3-18 characters: lowercase letters, numbers, or underscores")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Organization> {
        // Handle image upload via feature
        val imageResult = imageHandler.handleImageSubmission(
            entityId = entityId,
            entityType = "organization"
        )

        // If image upload failed, return error
        if (imageResult.error != null) {
            return imageResult.error
        }

        // Update the organization
        val input = UpdateOrganizationInput(
            name = Optional.presentIfNotNull(_name.value.trim().takeIf { it.isNotBlank() }),
            tag = Optional.presentIfNotNull(_tag.value.trim().takeIf { it.isNotBlank() }),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            logoUrl = Optional.presentIfNotNull(imageResult.imageUrl),
            website = Optional.presentIfNotNull(_website.value.trim().takeIf { it.isNotBlank() }),
            email = Optional.presentIfNotNull(_email.value.trim().takeIf { it.isNotBlank() }),
            phone = Optional.presentIfNotNull(_phone.value.trim().takeIf { it.isNotBlank() }),
            primaryColor = Optional.presentIfNotNull(_primaryColor.value.trim().takeIf { it.isNotBlank() }),
            secondaryColor = Optional.presentIfNotNull(_secondaryColor.value.trim().takeIf { it.isNotBlank() })
        )

        val result = organizationRepository.updateOrganization(entityId, input)

        // Create image record if we have a new image URL
        if (result is Resource.Success && imageResult.imageUrl != null) {
            imageHandler.createImageRecord(
                entityId = entityId,
                entityType = EntityType.ORGANIZATION,
                imageUrl = imageResult.imageUrl,
                altText = _name.value.trim()
            )
            // Don't fail the whole operation if image record creation fails
        }

        return result
    }

    /**
     * Delete the organization
     */
    fun deleteOrganization() {
        deleteHandler.performDelete {
            when (val result = organizationRepository.deleteOrganization(entityId)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.errorType, result.message)
                else -> result as Resource<Unit>
            }
        }
    }

    // Delete confirmation workflow
    fun requestDelete() {
        _showDeleteDialog.update { true }
    }

    fun confirmDelete() {
        _showDeleteDialog.update { false }
        deleteOrganization()
    }

    fun cancelDelete() {
        _showDeleteDialog.update { false }
    }

    // Image wizard state
    fun openImageWizard() {
        _showImageWizard.update { true }
    }

    fun closeImageWizard() {
        _showImageWizard.update { false }
    }
}
