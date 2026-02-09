package com.district37.toastmasters.features.edit.event

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.graphql.type.UpdateEventInput
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.HasPermissions
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.models.ImageSelectionResult
import com.district37.toastmasters.util.ArchiveOperationFeature
import com.district37.toastmasters.util.DeleteOperationFeature
import com.district37.toastmasters.util.DisplayFormatters
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.MultiImageHandlingFeature
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * ViewModel for editing an existing event
 *
 * Extends BaseEditViewModel to handle loading the event data,
 * and provides form fields for editing event properties.
 */
class EditEventViewModel(
    private val eventRepository: EventRepository,
    private val venueRepository: VenueRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val imageRepository: ImageRepository,
    eventId: Int
) : BaseEditViewModel<Event>(eventId, eventRepository) {

    override val tag = "EditEventViewModel"

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _eventType = MutableStateFlow(EventType.CONFERENCE)
    val eventType: StateFlow<EventType> = _eventType.asStateFlow()

    // Original venue ID (used to detect if venue changed)
    private var originalVenueId: Int? = null

    private val _selectedVenueId = MutableStateFlow<Int?>(null)
    val selectedVenueId: StateFlow<Int?> = _selectedVenueId.asStateFlow()

    private val _selectedVenueName = MutableStateFlow<String?>(null)
    val selectedVenueName: StateFlow<String?> = _selectedVenueName.asStateFlow()

    private val _startDate = MutableStateFlow<Instant?>(null)
    val startDate: StateFlow<Instant?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Instant?>(null)
    val endDate: StateFlow<Instant?> = _endDate.asStateFlow()

    // Multi-image handling using reusable feature
    private val imageHandler = MultiImageHandlingFeature(
        imageUploadRepository = imageUploadRepository,
        imageRepository = imageRepository,
        tag = tag
    )

    // Expose image state from feature
    val existingImages = imageHandler.existingImages
    val pendingImages = imageHandler.pendingImages
    val imagesToDelete = imageHandler.imagesToDelete
    val isUploadingImages = imageHandler.isUploadingImages

    // Track if venue has changed (for showing confirmation dialog)
    val venueChanged: Boolean
        get() = originalVenueId != null && _selectedVenueId.value != originalVenueId

    // Available event types for dropdown
    val eventTypes: List<EventType> = EventType.knownEntries

    // Delete functionality using DeleteOperationFeature
    private val deleteHandler = DeleteOperationFeature(
        tag = tag,
        scope = viewModelScope
    )
    val isDeleting = deleteHandler.isDeleting
    val deleteSuccess = deleteHandler.deleteSuccess

    // Archive functionality using ArchiveOperationFeature
    private val archiveHandler = ArchiveOperationFeature(
        tag = tag,
        scope = viewModelScope
    )
    val isArchiving = archiveHandler.isArchiving
    val archiveSuccess = archiveHandler.archiveSuccess

    // Expose canDelete permission from the loaded entity
    val canDelete: StateFlow<Boolean> = loadedEntity.map { entity ->
        (entity as? HasPermissions)?.permissions?.canDelete == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Check if event is archived
    val isArchived: StateFlow<Boolean> = loadedEntity.map { entity ->
        entity?.isArchived == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Get the archived timestamp for display
    val archivedAt: StateFlow<Instant?> = loadedEntity.map { entity ->
        entity?.archivedAt
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI state management (issues #4, #5, #6)
    data class PendingVenueChange(val venueId: Int, val venueName: String)

    private val _showVenueChangeConfirmation = MutableStateFlow(false)
    val showVenueChangeConfirmation: StateFlow<Boolean> = _showVenueChangeConfirmation.asStateFlow()

    private val _pendingVenue = MutableStateFlow<PendingVenueChange?>(null)
    val pendingVenue: StateFlow<PendingVenueChange?> = _pendingVenue.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showArchiveDialog = MutableStateFlow(false)
    val showArchiveDialog: StateFlow<Boolean> = _showArchiveDialog.asStateFlow()

    private val _showImageWizard = MutableStateFlow(false)
    val showImageWizard: StateFlow<Boolean> = _showImageWizard.asStateFlow()

    private val _selectedImageForEdit = MutableStateFlow<Image?>(null)
    val selectedImageForEdit: StateFlow<Image?> = _selectedImageForEdit.asStateFlow()

    // Derived state (issue #19)
    val eventTypeFormatted: StateFlow<String> = _eventType.map { type ->
        DisplayFormatters.formatEventType(type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    override fun mapEntityToFields(entity: Event) {
        _name.value = entity.name
        _description.value = entity.description ?: ""
        _eventType.value = entity.eventType ?: EventType.CONFERENCE
        originalVenueId = entity.venueId
        _selectedVenueId.value = entity.venueId
        _selectedVenueName.value = entity.venue?.name
        _startDate.value = entity.startDate?.instant
        _endDate.value = entity.endDate?.instant
        // Load existing images
        imageHandler.setExistingImages(entity.images)
    }

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateDescription(value: String) {
        _description.update { value }
    }

    fun updateEventType(value: EventType) {
        _eventType.update { value }
    }

    fun selectVenue(venueId: Int, venueName: String) {
        _selectedVenueId.update { venueId }
        _selectedVenueName.update { venueName }
        clearFieldError("venue")
    }

    fun clearVenue() {
        _selectedVenueId.update { null }
        _selectedVenueName.update { null }
    }

    fun updateStartDate(value: Instant) {
        _startDate.update { value }
        clearFieldError("startDate")
    }

    fun updateEndDate(value: Instant) {
        _endDate.update { value }
        clearFieldError("endDate")
    }

    // Multi-image management functions - delegated to imageHandler
    fun addPendingImage(result: ImageSelectionResult) = imageHandler.addPendingImage(result)
    fun deletePendingImage(imageId: String) = imageHandler.deletePendingImage(imageId)
    fun deleteExistingImage(imageId: Int) = imageHandler.deleteExistingImage(imageId)
    fun reorderImages(reorderedList: List<Any>) = imageHandler.reorderImages(reorderedList)

    fun updateExistingImageFocusRegion(imageId: Int, focusRegion: FocusRegion) {
        viewModelScope.launch {
            imageHandler.updateExistingImageFocusRegion(imageId, focusRegion)
        }
    }

    /**
     * Load and select a venue by ID (used when returning from nested creation flow)
     */
    suspend fun loadAndSelectVenue(venueId: Int) {
        val result = venueRepository.getVenue(venueId)
        if (result is Resource.Success) {
            selectVenue(result.data.id, result.data.name)
        } else {
            Logger.e(tag, "Failed to load venue $venueId")
        }
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            setFieldError("name", "Event name is required")
            isValid = false
        }

        if (_selectedVenueId.value == null) {
            setFieldError("venue", "Venue is required")
            isValid = false
        }

        if (_startDate.value == null) {
            setFieldError("startDate", "Start date is required")
            isValid = false
        }

        if (_endDate.value == null) {
            setFieldError("endDate", "End date is required")
            isValid = false
        }

        // Validate that end date is after start date
        val start = _startDate.value
        val end = _endDate.value
        if (start != null && end != null && end < start) {
            setFieldError("endDate", "End date must be after start date")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Event> {
        // 1. Handle image operations (delete marked, upload pending) using feature
        val imageResult = imageHandler.handleImageSubmission(entityId, "event")
        if (imageResult.error != null) {
            return imageResult.error
        }

        // 2. Update the event
        val input = UpdateEventInput(
            name = Optional.presentIfNotNull(_name.value.trim().takeIf { it.isNotBlank() }),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            eventType = Optional.presentIfNotNull(_eventType.value),
            venueId = Optional.presentIfNotNull(_selectedVenueId.value),
            startDate = Optional.presentIfNotNull(_startDate.value),
            endDate = Optional.presentIfNotNull(_endDate.value)
        )

        val result = eventRepository.updateEvent(entityId, input)

        // 3. Create image records for uploaded images
        if (result is Resource.Success) {
            imageHandler.createImageRecords(entityId, EntityType.EVENT, imageResult.uploadedImages)
            imageHandler.clearPendingState()
        }

        return result
    }

    /**
     * Delete the event
     */
    fun deleteEvent() {
        deleteHandler.performDelete {
            when (val result = eventRepository.deleteEvent(entityId)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.errorType, result.message)
                else -> result as Resource<Unit>
            }
        }
    }

    // Venue change confirmation workflow (issue #4)
    fun handleVenueSelection(venueId: Int, venueName: String) {
        if (originalVenueId != null && venueId != originalVenueId) {
            // Venue changed, show confirmation
            _pendingVenue.update { PendingVenueChange(venueId, venueName) }
            _showVenueChangeConfirmation.update { true }
        } else {
            // No change or no original venue, apply directly
            selectVenue(venueId, venueName)
        }
    }

    fun confirmVenueChange() {
        _pendingVenue.value?.let { pending ->
            selectVenue(pending.venueId, pending.venueName)
        }
        _showVenueChangeConfirmation.update { false }
        _pendingVenue.update { null }
    }

    fun cancelVenueChange() {
        _showVenueChangeConfirmation.update { false }
        _pendingVenue.update { null }
    }

    // Delete confirmation (issue #5)
    fun requestDelete() {
        _showDeleteDialog.update { true }
    }

    fun confirmDelete() {
        _showDeleteDialog.update { false }
        deleteEvent()
    }

    fun cancelDelete() {
        _showDeleteDialog.update { false }
    }

    // Archive confirmation workflow
    fun requestArchive() {
        _showArchiveDialog.update { true }
    }

    fun confirmArchive() {
        _showArchiveDialog.update { false }
        archiveEvent()
    }

    fun cancelArchive() {
        _showArchiveDialog.update { false }
    }

    /**
     * Archive the event
     */
    private fun archiveEvent() {
        archiveHandler.performArchive {
            when (val result = eventRepository.archiveEvent(entityId)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.errorType, result.message)
                else -> result as Resource<Unit>
            }
        }
    }

    // Image wizard state (issue #6)
    fun openImageWizard(image: Image? = null) {
        _selectedImageForEdit.update { image }
        _showImageWizard.update { true }
    }

    fun closeImageWizard() {
        _showImageWizard.update { false }
        _selectedImageForEdit.update { null }
    }

    // Navigation result handling (issue #8)
    fun handleNavigationResult(key: String, value: Any?) {
        when (key) {
            "venue_id" -> {
                if (value is Int) {
                    viewModelScope.launch {
                        loadAndSelectVenue(value)
                    }
                }
            }
            "venue_name" -> {
                // Already handled via venue_id
            }
        }
    }
}
